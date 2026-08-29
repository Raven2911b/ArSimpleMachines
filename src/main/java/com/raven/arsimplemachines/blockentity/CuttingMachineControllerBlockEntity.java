package com.raven.arsimplemachines.blockentity;

import ARLib.ARLibRegistry;
import ARLib.blockentities.EntityItemInputBlock;
import ARLib.blockentities.EntityItemOutputBlock;
import ARLib.multiblockCore.BlockMultiblockMaster;
import ARLib.multiblockCore.EntityMultiblockMachineMaster;
import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;

import com.raven.arsimplemachines.menu.CuttingMachineMenu;
import com.raven.arsimplemachines.recipe.MachineRecipeInput;
import com.raven.arsimplemachines.recipe.MachineRecipeMatcher;
import com.raven.arsimplemachines.recipe.TagInput;
import com.raven.arsimplemachines.recipe.cutter.CuttingMachineRecipe;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import com.raven.arsimplemachines.registry.ModBlocks;
import com.raven.arsimplemachines.registry.ModRecipeTypes;

import com.raven.arsimplemachines.util.PatternScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class CuttingMachineControllerBlockEntity
        extends EntityMultiblockMachineMaster
        implements INetworkTagReceiver, MenuProvider {

    // ---------------------------------------------------------
    // Rendering / Animation
    // ---------------------------------------------------------
    public static class RenderData {
        public float sawRotation = 0f;
        public boolean running = false;
    }
    private Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }
    private int minX() {
        return switch (getFacing()) {
            case NORTH -> -1;
            case SOUTH -> -1;
            case EAST  -> -1;
            case WEST  -> 0;
            default -> 0;
        };
    }

    private int maxX() {
        return switch (getFacing()) {
            case NORTH -> 1;
            case SOUTH -> 1;
            case EAST  -> 0;
            case WEST  -> 1;
            default -> 0;
        };
    }
    private int minY() { return 0; }
    private int maxY() { return 1; }

    private int minZ() {
        return switch (getFacing()) {
            case NORTH -> 0;
            case SOUTH -> -1;
            case EAST  -> -1;
            case WEST  -> -1;
            default -> 0;
        };
    }

    private int maxZ() {
        return switch (getFacing()) {
            case NORTH -> 1;
            case SOUTH -> 0;
            case EAST  -> 1;
            case WEST  -> 1;
            default -> 0;
        };
    }



    public RenderData renderData = new RenderData();

    // ---------------------------------------------------------
    // Recipe State
    // ---------------------------------------------------------
    private CuttingMachineRecipe currentRecipe = null;
    private boolean recipeRunning = false;
    private int recipeProgress = 0;
    private int recipeMaxProgress = 0;

    private int clientEnergyStored = 0;
    private int clientEnergyMax = 0;

    private final Map<Block, List<BlockPos>> blockCache = new HashMap<>();
    private boolean cacheValid = false;

    public CuttingMachineControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CUTTING_MACHINE_CONTROLLER.get(), pos, state);
    }

    // ---------------------------------------------------------
    // Multiblock Structure
    // ---------------------------------------------------------
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
            'S', BuiltInRegistries.BLOCK.stream().toList(),
            'I', List.of(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get()),
            'O', List.of(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get()),
            'M', BuiltInRegistries.BLOCK.stream().toList(),
            'C', List.of(ModBlocks.CUTTING_MACHINE_CONTROLLER.get())
    );

    @Override
    public HashMap<Character, List<Block>> getCharMapping() {
        return new HashMap<>(MAPPING);
    }

    @Override
    public Vec3i getControllerOffset(Object[][][] structure) {
        for (int y = 0; y < structure.length; y++)
            for (int z = 0; z < structure[y].length; z++)
                for (int x = 0; x < structure[y][z].length; x++)
                    if (structure[y][z][x] instanceof Character ch && (ch == 'C' || ch == 'c'))
                        return new Vec3i(x, y, z);

        return new Vec3i(0, 0, 0);
    }

    // ---------------------------------------------------------
    // Cache
    // ---------------------------------------------------------
    private void rebuildBlockCache() {
        blockCache.clear();

        for (int dx = minX(); dx <= maxX(); dx++)
            for (int dy = minY(); dy <= maxY(); dy++)
                for (int dz = minZ(); dz <= maxZ(); dz++) {

                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    Block b = level.getBlockState(p).getBlock();
                    blockCache.computeIfAbsent(b, k -> new ArrayList<>()).add(p);
                }

        cacheValid = true;
    }

    private BlockPos findBlock(Block blockType) {
        if (!cacheValid) rebuildBlockCache();
        List<BlockPos> list = blockCache.get(blockType);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    // ---------------------------------------------------------
    // Multiblock Callbacks
    // ---------------------------------------------------------
    @Override
    public void onStructureComplete() {
        rebuildBlockCache();
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
        currentRecipe = null;
        renderData.running = false;
        sendUpdatePacket(null);
    }

    // ---------------------------------------------------------
    // Energy
    // ---------------------------------------------------------
    private IEnergyStorage getEnergyStorage() {
        BlockPos pos = findBlock(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get());
        if (pos == null) return null;

        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return null;

        return level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                pos,
                level.getBlockState(pos),
                be,
                Direction.DOWN
        );
    }

    public int getClientEnergyStored() {
        return clientEnergyStored;
    }

    public int getClientEnergyMax() {
        return clientEnergyMax;
    }

    // ---------------------------------------------------------
    // Tick Logic
    // ---------------------------------------------------------
    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean formed = getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED);
        if (!formed) {
            recipeRunning = false;
            currentRecipe = null;
            return;
        }

        IEnergyStorage storage = getEnergyStorage();
        if (storage != null) {
            clientEnergyStored = storage.getEnergyStored();
            clientEnergyMax = storage.getMaxEnergyStored();
        }

        sendUpdatePacket(null);

        if (!recipeRunning) {
            tryStartRecipe();
            return;
        }

        if (storage == null) return;
        if (storage.getEnergyStored() < currentRecipe.getEnergyPerTick()) return;

        storage.extractEnergy(currentRecipe.getEnergyPerTick(), false);
        recipeProgress++;
        sendUpdatePacket(null);

        if (recipeProgress >= recipeMaxProgress) {
            finishRecipe();
        }
    }

    // ---------------------------------------------------------
    // Recipe Start
    // ---------------------------------------------------------
    private void tryStartRecipe() {
        BlockPos inputPos = findBlock(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get());
        if (inputPos == null) return;

        BlockEntity be = level.getBlockEntity(inputPos);
        if (!(be instanceof EntityItemInputBlock input)) return;

        MachineRecipeInput recipeInput = new MachineRecipeInput();

        for (int slot = 0; slot < input.inventory.getSlots(); slot++) {
            ItemStack stack = input.inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) recipeInput.addItem(stack);
        }

        CuttingMachineRecipe recipe = MachineRecipeMatcher.findMatch(
                level,
                ModRecipeTypes.CUTTING_MACHINE_TYPE,
                recipeInput
        );

        if (recipe == null) return;

        IEnergyStorage storage = getEnergyStorage();
        if (storage == null) return;
        if (storage.getEnergyStored() < recipe.getEnergyPerTick()) return;

        consumeTagInputs(recipe, input.inventory);

        currentRecipe = recipe;
        recipeRunning = true;
        recipeProgress = 0;
        recipeMaxProgress = recipe.getProcessingTime();
        renderData.running = true;

        sendUpdatePacket(null);
    }

    private void consumeTagInputs(CuttingMachineRecipe recipe, IItemHandler handler) {
        for (TagInput tagInput : recipe.getItemTags()) {
            TagKey<Item> tagKey = TagKey.create(
                    BuiltInRegistries.ITEM.key(),
                    tagInput.tag()
            );

            int remaining = tagInput.count();

            for (int slot = 0; slot < handler.getSlots(); slot++) {
                if (remaining <= 0) break;

                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty() && stack.is(tagKey)) {
                    int take = Math.min(stack.getCount(), remaining);
                    handler.extractItem(slot, take, false);
                    remaining -= take;
                }
            }
        }
    }

    // ---------------------------------------------------------
    // Recipe Finish
    // ---------------------------------------------------------
    private void finishRecipe() {
        recipeRunning = false;
        renderData.running = false;

        if (currentRecipe != null) {
            List<ItemStack> outputs = currentRecipe.getItemOutputs();

            BlockPos outputPos = findBlock(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get());
            if (outputPos != null) {
                BlockEntity be = level.getBlockEntity(outputPos);
                if (be instanceof EntityItemOutputBlock out) {
                    for (ItemStack outStack : outputs) {
                        ItemStack remainder = outStack.copy();
                        for (int slot = 0; slot < out.inventory.getSlots(); slot++) {
                            remainder = out.inventory.insertItem(slot, remainder, false);
                            if (remainder.isEmpty()) break;
                        }
                    }
                }
            }
        }

        currentRecipe = null;
        recipeProgress = 0;
        recipeMaxProgress = 0;

        sendUpdatePacket(null);
    }

        // ---------------------------------------------------------
    // Client Tick
    // ---------------------------------------------------------
    public void clientTick() {
        if (level == null || !level.isClientSide) return;
//        PatternScanner.drawScanBox(
//                level,
//                worldPosition,
//                minX(), maxX(),
//                minY(), maxY(),
//                minZ(), maxZ()
//        );


        if (renderData.running) {
            renderData.sawRotation = (renderData.sawRotation + 12f) % 360f;
        } else {
            renderData.sawRotation = 0f;
        }
    }

    // ---------------------------------------------------------
    // MenuProvider
    // ---------------------------------------------------------
    @Override
    public Component getDisplayName() {
        return Component.literal("Cutting Machine");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new CuttingMachineMenu(windowId, inv, this.getBlockPos());
    }

    public int getRecipeProgress() {
        return recipeProgress;
    }

    public int getRecipeMaxProgress() {
        return recipeMaxProgress;
    }

    public IItemHandler getInputHandler() {
        BlockPos pos = findBlock(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get());
        if (pos == null) return null;

        BlockEntity be = level.getBlockEntity(pos);
        return (be instanceof EntityItemInputBlock input) ? input.inventory : null;
    }

    public IItemHandler getOutputHandler() {
        BlockPos pos = findBlock(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get());
        if (pos == null) return null;

        BlockEntity be = level.getBlockEntity(pos);
        return (be instanceof EntityItemOutputBlock out) ? out.inventory : null;
    }

    // ---------------------------------------------------------
    // Networking
    // ---------------------------------------------------------
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
