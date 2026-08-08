package com.raven.arsimplemachines.blockentity;

import ARLib.ARLibRegistry;
import ARLib.blockentities.EntityEnergyInputBlock;
import ARLib.multiblockCore.EntityMultiblockMachineMaster;
import ARLib.multiblockCore.BlockMultiblockMaster;
import com.raven.arsimplemachines.menu.CuttingMachineMenu;
import com.raven.arsimplemachines.recipe.cutter.CuttingMachineRecipe;
import com.raven.arsimplemachines.recipe.cutter.CuttingMachineRecipeInput;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import com.raven.arsimplemachines.registry.ModBlocks;
import com.raven.arsimplemachines.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.Block;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import ARLib.blockentities.EntityItemInputBlock;
import ARLib.blockentities.EntityItemOutputBlock;
import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CuttingMachineControllerBlockEntity extends EntityMultiblockMachineMaster implements INetworkTagReceiver, MenuProvider {

    // -------------------------
    // Rendering / Animation Data
    // -------------------------
    public static class RenderData {
        public float sawRotation = 0f;
        public boolean running = false;
    }

    public RenderData renderData = new RenderData();

    // -------------------------
    // Recipe State
    // -------------------------
    private static final int ENERGY_PER_TICK = 20;
    private boolean recipeRunning = false;
    private int recipeProgress = 0;
    private int recipeMaxProgress = 0;
    private ItemStack processingInput = ItemStack.EMPTY;
    private Map<Block, List<BlockPos>> blockCache = new HashMap<>();
    private boolean cacheValid = false;

    public CuttingMachineControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CUTTING_MACHINE_CONTROLLER.get(), pos, state);
    }

    // -------------------------
    // ARLib Multiblock Definition
    // -------------------------

    @Override
    public Object[][][] getStructure() {
        return new Object[][][]{
                {
                        { 'I', 'C', 'O' },
                        { 'M', 'S', 'E' }
                }
        };
    }
    public static final Map<Character, List<Block>> MAPPING = Map.of(
            'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
            'S', List.of(ModBlocks.SAW_BLADE_ASSEMBLY.get()),
            'I', List.of(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get()),
            'O', List.of(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get()),
            'M', List.of(ARLibRegistry.BLOCK_MOTOR.get()),
            'C', List.of(ModBlocks.CUTTING_MACHINE_CONTROLLER.get())
    );

    @Override
    public HashMap<Character, List<Block>> getCharMapping() {
        return new HashMap<>(MAPPING);
    }

    @Override
    public Vec3i getControllerOffset(Object[][][] structure) {
        if (structure == null) return new Vec3i(0, 0, 0);

        for (int y = 0; y < structure.length; y++) {
            for (int z = 0; z < structure[y].length; z++) {
                for (int x = 0; x < structure[y][z].length; x++) {
                    Object v = structure[y][z][x];
                    if (v instanceof Character ch) {
                        if (ch == 'c' || ch == 'C') {
                            return new Vec3i(x, y, z);
                        }
                    }
                }
            }
        }

        int cy = structure.length / 2;
        int cz = structure[0].length / 2;
        int cx = structure[0][0].length / 2;
        return new Vec3i(cx, cy, cz);
    }

    // -------------------------
    // Multiblock Callbacks
    // -------------------------
    private void rebuildBlockCache() {
        blockCache.clear();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    Block b = level.getBlockState(p).getBlock();
                    blockCache.computeIfAbsent(b, k -> new ArrayList<>()).add(p);
                }
            }
        }
        cacheValid = true;
    }

    @Override
    public void onStructureComplete() {
        rebuildBlockCache();   // ⭐ REQUIRED

        renderData.running = false;

        IEnergyStorage storage = getEnergyStorage();
        if (storage != null) {
            clientEnergyStored = storage.getEnergyStored();
            clientEnergyMax = storage.getMaxEnergyStored();
        }

        sendUpdatePacket(null);
    }



    @Override
    public void onStructureInvalid() {
        recipeRunning = false;
        renderData.running = false;
        sendUpdatePacket(null);
    }

    @Override
    public void readServer(CompoundTag tag, ServerPlayer sender) {
        // No server-only fields
    }
    @Override
    public void onLoad() {
        super.onLoad();

        // Delay TWO ticks so ARLib can finish capability registration
        if (!level.isClientSide) {
            level.scheduleTick(worldPosition, getBlockState().getBlock(), 2);
        }
    }


    // -------------------------
    // Server Tick (Gameplay Logic)
    // -------------------------

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean formed = getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED);
        if (!formed) {
            recipeRunning = false;
            return;
        }

        IEnergyStorage storage = getEnergyStorage();
        if (storage != null) {
            clientEnergyStored = storage.getEnergyStored();
            clientEnergyMax = storage.getMaxEnergyStored();
        }
        boolean energyChanged = false;

        if (storage != null) {
            int newStored = storage.getEnergyStored();
            int newMax = storage.getMaxEnergyStored();

            if (newStored != clientEnergyStored || newMax != clientEnergyMax) {
                clientEnergyStored = newStored;
                clientEnergyMax = newMax;
                energyChanged = true;
            }
        }

        if (energyChanged && !recipeRunning) {
            sendUpdatePacket(null);
        }

        if (!recipeRunning) {
            tryStartRecipe();
        } else {
            if (storage == null) return;

            if (storage.getEnergyStored() < ENERGY_PER_TICK) return;

            storage.extractEnergy(ENERGY_PER_TICK, false);
            recipeProgress++;
            sendUpdatePacket(null);

            if (recipeProgress >= recipeMaxProgress) {
                finishRecipe();
            }
        }
    }
    private BlockPos findSpecificBlock(Block blockType) {
        if (!cacheValid) rebuildBlockCache();
        List<BlockPos> list = blockCache.get(blockType);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    public IEnergyStorage getEnergyStorage() {
        BlockPos pos = findSpecificBlock(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get());
        if (pos == null) return null;

        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return null;

        // ARLib energy input exposes FE ONLY on DOWN
        IEnergyStorage storage = level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                pos,
                level.getBlockState(pos),
                be,
                Direction.DOWN
        );

        return storage;
    }

    private void tryStartRecipe() {
        if (!getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) return;

        IEnergyStorage storage = getEnergyStorage();
        if (storage == null) return;
        if (storage.getEnergyStored() < ENERGY_PER_TICK) return;

        BlockPos inputPos = findInputBlock();
        if (inputPos == null) return;

        BlockEntity be = level.getBlockEntity(inputPos);
        if (!(be instanceof EntityItemInputBlock input)) return;

        for (int slot = 0; slot < input.inventory.getSlots(); slot++) {
            ItemStack stack = input.inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            var recipeOpt = level.getRecipeManager().getRecipeFor(
                    ModRecipeTypes.CUTTING_MACHINE_TYPE,
                    new CuttingMachineRecipeInput(stack),
                    level
            );

            if (recipeOpt.isEmpty()) continue;

            CuttingMachineRecipe recipe = recipeOpt.get().value();

            input.inventory.extractItem(slot, 1, false);

            recipeRunning = true;
            recipeProgress = 0;
            recipeMaxProgress = recipe.processingTime;

            processingInput = stack.copy();
            processingInput.setCount(1);

            renderData.running = true;
            sendUpdatePacket(null);
            return;
        }
    }

    private void finishRecipe() {
        recipeRunning = false;
        renderData.running = false;

        var recipeOpt = level.getRecipeManager().getRecipeFor(
                ModRecipeTypes.CUTTING_MACHINE_TYPE,
                new CuttingMachineRecipeInput(processingInput),
                level
        );

        if (recipeOpt.isEmpty()) {
            processingInput = ItemStack.EMPTY;
            sendUpdatePacket(null);
            return;
        }

        CuttingMachineRecipe recipe = recipeOpt.get().value();

        ItemStack output = new ItemStack(recipe.getOutputItem(), recipe.getOutputCount());

        BlockPos outputPos = findOutputBlock();

        if (outputPos != null) {
            BlockEntity be = level.getBlockEntity(outputPos);
            if (be instanceof EntityItemOutputBlock out) {
                ItemStack remainder = output.copy();
                for (int slot = 0; slot < out.inventory.getSlots(); slot++) {
                    remainder = out.inventory.insertItem(slot, remainder, false);
                    if (remainder.isEmpty()) break;
                }
            }
        }

        processingInput = ItemStack.EMPTY;
        sendUpdatePacket(null);
    }

    private BlockPos findInputBlock() {
        return findBlock(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get());
    }

    private BlockPos findOutputBlock() {
        return findBlock(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get());
    }


    private BlockPos findBlock(Block blockType) {
        for (int dx = -3; dx <= 3; dx++)
            for (int dy = -2; dy <= 2; dy++)
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    if (level.getBlockState(p).getBlock() == blockType)
                        return p;
                }
        return null;
    }

    private int clientEnergyStored;
    private int clientEnergyMax;

    public int getClientEnergyStored() {
        return clientEnergyStored;
    }

    public int getClientEnergyMax() {
        return clientEnergyMax;
    }

    // -------------------------
    // Client Tick (Animation)
    // -------------------------

    public void clientTick() {
        if (level == null || !level.isClientSide) return;

        if (renderData.running) {
            renderData.sawRotation = (renderData.sawRotation + 12f) % 360f;
        } else {
            renderData.sawRotation = 0f;
        }
    }

    // -------------------------
    // MenuProvider
    // -------------------------

    @Override
    public Component getDisplayName() {
        return Component.literal("Cutting Machine");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new CuttingMachineMenu(windowId, inv, this.getBlockPos());
    }

    // -------------------------
    // REQUIRED BY CuttingMachineMenu
    // -------------------------

    public int getRecipeProgress() {
        return recipeProgress;
    }

    public int getRecipeMaxProgress() {
        return recipeMaxProgress;
    }

    public net.neoforged.neoforge.items.IItemHandler getInputHandler() {
        BlockPos inputPos = findInputBlock();
        if (inputPos == null) return null;

        BlockEntity be = level.getBlockEntity(inputPos);
        if (be instanceof EntityItemInputBlock input)
            return input.inventory;

        return null;
    }

    public net.neoforged.neoforge.items.IItemHandler getOutputHandler() {
        BlockPos outputPos = findOutputBlock();
        if (outputPos == null) return null;

        BlockEntity be = level.getBlockEntity(outputPos);
        if (be instanceof EntityItemOutputBlock out)
            return out.inventory;

        return null;
    }

    // -------------------------
    // Networking
    // -------------------------

    public void sendUpdatePacket(ServerPlayer specificPlayer) {
        if (level == null || level.isClientSide) return;

        CompoundTag tag = new CompoundTag();
        tag.putBoolean("recipeRunning", recipeRunning);
        tag.putInt("recipeProgress", recipeProgress);
        tag.putInt("recipeMaxProgress", recipeMaxProgress);

        tag.putBoolean("running", renderData.running);
        tag.putInt("energyStored", clientEnergyStored);
        tag.putInt("energyMax", clientEnergyMax);

        PacketBlockEntity packet = PacketBlockEntity.getBlockEntityPacket(this, tag);

        if (specificPlayer != null)
            PacketDistributor.sendToPlayer(specificPlayer, packet);
        else
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(worldPosition), packet);
    }

    @Override
    public void readClient(CompoundTag tag) {
        if (tag.contains("recipeRunning")) recipeRunning = tag.getBoolean("recipeRunning");
        if (tag.contains("recipeProgress")) recipeProgress = tag.getInt("recipeProgress");
        if (tag.contains("recipeMaxProgress")) recipeMaxProgress = tag.getInt("recipeMaxProgress");

        if (tag.contains("energyStored")) clientEnergyStored = tag.getInt("energyStored");
        if (tag.contains("energyMax")) clientEnergyMax = tag.getInt("energyMax");
        if (tag.contains("running")) renderData.running = tag.getBoolean("running");
    }
}
