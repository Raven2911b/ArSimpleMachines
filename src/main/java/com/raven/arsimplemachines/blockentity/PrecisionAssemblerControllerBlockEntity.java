package com.raven.arsimplemachines.blockentity;

import ARLib.ARLibRegistry;
import ARLib.blockentities.EntityItemInputBlock;
import ARLib.blockentities.EntityItemOutputBlock;
import ARLib.multiblockCore.EntityMultiblockMachineMaster;
import ARLib.multiblockCore.BlockMultiblockMaster;
import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;

import com.raven.arsimplemachines.block.PrecisionAssemblerControllerBlock;
import com.raven.arsimplemachines.menu.PrecisionAssemblerMenu;
import com.raven.arsimplemachines.recipe.MachineRecipeInput;
import com.raven.arsimplemachines.recipe.precision.PrecisionAssemblerRecipeInput;
import com.raven.arsimplemachines.recipe.precision.PrecisionAssemblerRecipe;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import com.raven.arsimplemachines.registry.ModBlocks;
import com.raven.arsimplemachines.registry.ModRecipeTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

import static com.raven.arsimplemachines.block.PrecisionAssemblerControllerBlock.RUNNING;

public class PrecisionAssemblerControllerBlockEntity extends EntityMultiblockMachineMaster
        implements INetworkTagReceiver, MenuProvider {

    public static class RenderData {
        public boolean running = false;
        public float anim = 0f;
    }

    public RenderData renderData = new RenderData();
    public PrecisionAssemblerRecipe currentRecipe;

    public boolean recipeRunning = false;
    public int recipeProgress = 0;
    public int recipeMaxProgress = 0;

    private int clientEnergyStored = 0;
    private int clientEnergyMax = 0;
    public boolean processCSwingStarted = false;
    public boolean processCSwingFinished = false;

    private boolean clientHasInputItems = false;
    public boolean getClientHasInputItems() { return clientHasInputItems; }

    public PrecisionAssemblerControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRECISION_ASSEMBLER_CONTROLLER.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Precision Assembler");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new PrecisionAssemblerMenu(windowId, inv, this.getBlockPos());
    }

    // ------------------------------------------------------------
    // MULTIBLOCK STRUCTURE (3 × 4 × 3)
    // ------------------------------------------------------------
    @Override
    public Object[][][] getStructure() {
        return new Object[][][]{

                // Y = 0 (top)
                {
                        { 'S', 'S', 'S', 'S' },
                        { 'S', 'S', 'S', 'S' },
                        { 'S', 'S', 'S', 'S' }
                },


                // Y = 1 (middle)
                {

                        { 'S', 'G', 'G', 'S' },
                        { 'S', null, null, 'S' },
                        { 'S', 'S', 'S', 'S' }
                },

                // Y = 2 (botton)
                {
                        { 'C', 'S', 'S', 'S' },
                        { 'I', 'X', 'X', 'O' },
                        { 'E', 'M', 'M', 'E' }
                }
        };
    }

    public static final Map<Character, List<Block>> MAPPING = Map.of(
            'S', List.of(ARLibRegistry.BLOCK_STRUCTURE.get()),
            'I', List.of(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get()),
            'O', List.of(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get()),
            'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
            'M', List.of(ARLibRegistry.BLOCK_MOTOR.get()),
            'X', List.of(ARLibRegistry.BLOCK_COIL_COPPER.get()),
            'G', List.of(Blocks.GLASS),
            'C', List.of(ModBlocks.PRECISION_ASSEMBLER_CONTROLLER.get())
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
                    if (structure[y][z][x] instanceof Character ch && (ch == 'C'))
                        return new Vec3i(x, y, z);

        return new Vec3i(1, 1, 1);
    }

    @Override
    public void onStructureInvalid() {
        recipeRunning = false;
        renderData.running = false;
        sendUpdatePacket(null);
    }


    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 3,
                worldPosition.getY() - 1,
                worldPosition.getZ() - 3,
                worldPosition.getX() + 3,
                worldPosition.getY() + 5,
                worldPosition.getZ() + 3
        );
    }

    public boolean shouldRenderOffScreen() {
        return true;
    }

    // ------------------------------------------------------------
    // DEBUG STRUCTURE CHECK
    // ------------------------------------------------------------
    public void debugStructureCheck() {
        Object[][][] structure = getStructure();
        Vec3i offset = getControllerOffset(structure);

        for (int y = 0; y < structure.length; y++) {
            for (int z = 0; z < structure[y].length; z++) {
                for (int x = 0; x < structure[y][z].length; x++) {

                    Object o = structure[y][z][x];
                    if (!(o instanceof Character ch)) continue;

                    BlockPos p = worldPosition.offset(
                            x - offset.getX(),
                            y - offset.getY(),
                            z - offset.getZ()
                    );

                    BlockState state = level.getBlockState(p);
                    Block expected = MAPPING.get(ch).get(0);
                    Block actual = state.getBlock();

                    if (actual != expected) {
                        System.out.println("### MISMATCH at " + p +
                                " expected=" + expected +
                                " actual=" + actual +
                                " char=" + ch);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------
    // MACHINE LOGIC
    // ------------------------------------------------------------
    public boolean hasAnyInputItems() {
        for (IItemHandler handler : findAllItemInputs()) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                if (!handler.getStackInSlot(slot).isEmpty()) return true;
            }
        }
        return false;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (!getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) {
            resetMachineState();        // ✔ stops animation
            return;
        }

        syncEnergy();

        if (!recipeRunning) {
            if (currentRecipe == null) tryStartRecipe();
            if (!recipeRunning || currentRecipe == null) return;  // ✔ no animation
        }

        List<IEnergyStorage> storages = getAllEnergyStorages();
        if (storages.isEmpty()) {
            resetMachineState();        // ✔ stops animation
            return;
        }

        int totalEnergy = storages.stream().mapToInt(IEnergyStorage::getEnergyStored).sum();
        int energyPerTick = currentRecipe.getEnergyPerTick();

        if (totalEnergy < energyPerTick) {
            resetMachineState();        // ✔ stops animation
            return;
        }

        // ✔ Even energy drain
        int blocks = storages.size();
        int perBlock = energyPerTick / blocks;
        int remainder = energyPerTick % blocks;

        for (IEnergyStorage es : storages)
            es.extractEnergy(Math.min(es.getEnergyStored(), perBlock), false);

        int remaining = remainder;
        for (IEnergyStorage es : storages) {
            if (remaining <= 0) break;
            int take = Math.min(es.getEnergyStored(), remaining);
            es.extractEnergy(take, false);
            remaining -= take;
        }

        // ⭐ Animation progress
        recipeProgress++;

        // ⭐ Optional looping animation value
        renderData.anim = (recipeProgress % 20) / 20f;

        sendUpdatePacket(null);

        if (recipeProgress >= recipeMaxProgress) finishRecipe();
    }


    private void syncEnergy() {
        List<IEnergyStorage> storages = getAllEnergyStorages();
        if (storages.isEmpty()) return;

        clientEnergyStored = storages.stream().mapToInt(IEnergyStorage::getEnergyStored).sum();
        clientEnergyMax = storages.stream().mapToInt(IEnergyStorage::getMaxEnergyStored).sum();
        clientHasInputItems = hasAnyInputItems();
        sendUpdatePacket(null);
    }

    private void resetMachineState() {
        recipeRunning = false;
        currentRecipe = null;
        recipeProgress = 0;
        recipeMaxProgress = 0;
        renderData.running = false;
        renderData.anim = 0f;

        BlockState state = level.getBlockState(worldPosition);
        if (state.hasProperty(RUNNING))
            level.setBlock(worldPosition, state.setValue(RUNNING, false), 3);

        sendUpdatePacket(null);
    }

    private List<IEnergyStorage> getAllEnergyStorages() {
        List<IEnergyStorage> list = new ArrayList<>();

        for (BlockPos pos : findAllBlocks(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null) continue;

            IEnergyStorage storage = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    pos,
                    level.getBlockState(pos),
                    be,
                    null
            );

            if (storage != null) list.add(storage);
        }

        return list;
    }


    private void tryStartRecipe() {
        List<IItemHandler> itemInputs = findAllItemInputs();
        if (itemInputs.isEmpty()) return;

        List<IEnergyStorage> storages = getAllEnergyStorages();
        if (storages.isEmpty()) return;

        int totalEnergy = storages.stream().mapToInt(IEnergyStorage::getEnergyStored).sum();

        // Collect all input items
        List<ItemStack> collected = new ArrayList<>();
        for (IItemHandler handler : itemInputs) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    collected.add(stack.copy());
                }
            }
        }

        PrecisionAssemblerRecipeInput inputWrapper = new PrecisionAssemblerRecipeInput(collected);

        RecipeHolder<PrecisionAssemblerRecipe> holder = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.PRECISION_ASSEMBLER_TYPE)
                .stream()
                .filter(h -> h.value().matches(inputWrapper, level))
                .findFirst()
                .orElse(null);

        PrecisionAssemblerRecipe recipe = holder != null ? holder.value() : null;

        if (recipe == null) return;
        if (totalEnergy < recipe.getEnergyPerTick()) {
            resetMachineState();
            return;
        }

        if (!hasRequiredItems(recipe, itemInputs)) return;

        consumeRequiredItems(recipe, itemInputs);

        // ⭐ Animation fields
        currentRecipe = recipe;
        recipeRunning = true;
        recipeProgress = 0;
        recipeMaxProgress = recipe.getProcessingTime();

        // ⭐ Renderer flag
        renderData.running = true;

        // Blockstate flag
        BlockState state = level.getBlockState(worldPosition);
        if (state.hasProperty(RUNNING))
            level.setBlock(worldPosition, state.setValue(RUNNING, true), 3);

        sendUpdatePacket(null);
    }



    private boolean hasRequiredItems(PrecisionAssemblerRecipe recipe, List<IItemHandler> handlers) {
        Map<IItemHandler, Map<Integer, Integer>> usedCounts = new HashMap<>();

        for (ItemStack req : recipe.getItemInputs()) {
            int needed = req.getCount();
            int found = 0;

            for (IItemHandler handler : handlers) {
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    if (stack.isEmpty()) continue;
                    if (stack.is(req.getItem())) {
                        found += stack.getCount();
                        if (found >= needed) break;
                    }
                }
                if (found >= needed) break;
            }

            if (found < needed) return false;
        }

        return true;
    }

    private void consumeRequiredItems(PrecisionAssemblerRecipe recipe, List<IItemHandler> handlers) {
        for (ItemStack req : recipe.getItemInputs()) {
            int needed = req.getCount();

            for (IItemHandler handler : handlers) {
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    if (stack.isEmpty()) continue;
                    if (stack.is(req.getItem())) {
                        int take = Math.min(stack.getCount(), needed);
                        handler.extractItem(slot, take, false);
                        needed -= take;
                        if (needed <= 0) break;
                    }
                }
                if (needed <= 0) break;
            }
        }
    }

    private List<BlockPos> findAllBlocks(Block blockType) {
        List<BlockPos> list = new ArrayList<>();
        for (int dx = -4; dx <= 4; dx++)
            for (int dy = -2; dy <= 6; dy++)
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    if (level.getBlockState(p).getBlock() == blockType)
                        list.add(p);
                }
        return list;
    }

    public List<IItemHandler> findAllItemInputs() {
        List<IItemHandler> list = new ArrayList<>();

        for (BlockPos pos : findAllBlocks(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EntityItemInputBlock input) {
                list.add(input.inventory);
            }
        }

        return list;
    }



    private List<IItemHandler> findAllItemOutputs() {
        List<IItemHandler> list = new ArrayList<>();
        for (BlockPos pos : findAllBlocks(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EntityItemOutputBlock out)
                list.add(out.inventory);
        }
        return list;
    }

    private void finishRecipe() {
        PrecisionAssemblerRecipe finished = currentRecipe;

        recipeRunning = false;
        renderData.running = false;
        renderData.anim = 0f;

        if (finished != null) {
            List<IItemHandler> outputs = findAllItemOutputs();

            for (ItemStack out : finished.getItemOutputs()) {
                ItemStack remainder = out.copy();

                for (IItemHandler handler : outputs) {
                    for (int slot = 0; slot < handler.getSlots(); slot++) {
                        remainder = handler.insertItem(slot, remainder, false);
                        if (remainder.isEmpty()) break;
                    }
                    if (remainder.isEmpty()) break;
                }
            }
        }

        currentRecipe = null;

        BlockState state = level.getBlockState(worldPosition);
        if (state.hasProperty(RUNNING))
            level.setBlock(worldPosition, state.setValue(RUNNING, false), 3);

        sendUpdatePacket(null);
    }


    public void clientTick() {
        if (level == null || !level.isClientSide) return;

        // -----------------------------
        // MAIN MACHINE ANIMATION (your existing sine wave)
        // -----------------------------
        if (renderData.running)
            renderData.anim = (float) Math.abs(Math.sin(level.getGameTime() * 0.25f));
        else
            renderData.anim = 0f;

    }


    public int getRecipeProgress() { return recipeProgress; }
    public int getRecipeMaxProgress() { return recipeMaxProgress; }
    public int getClientEnergyStored() { return clientEnergyStored; }
    public int getClientEnergyMax() { return clientEnergyMax; }

    public void sendUpdatePacket(ServerPlayer specificPlayer) {
        if (level == null || level.isClientSide) return;

        CompoundTag tag = new CompoundTag();
        tag.putBoolean("running", renderData.running);
        tag.putBoolean("recipeRunning", recipeRunning);
        tag.putFloat("anim", renderData.anim);
        tag.putInt("energyStored", clientEnergyStored);
        tag.putInt("energyMax", clientEnergyMax);
        tag.putInt("recipeProgress", recipeProgress);
        tag.putInt("recipeMaxProgress", recipeMaxProgress);
        tag.putBoolean("hasInputItems", clientHasInputItems);
        tag.putBoolean("processCSwingStarted", processCSwingStarted);
        tag.putBoolean("processCSwingFinished", processCSwingFinished);

        PacketBlockEntity packet = PacketBlockEntity.getBlockEntityPacket(this, tag);

        if (specificPlayer != null)
            PacketDistributor.sendToPlayer(specificPlayer, packet);
        else
            PacketDistributor.sendToPlayersTrackingChunk(
                    (net.minecraft.server.level.ServerLevel) level,
                    new net.minecraft.world.level.ChunkPos(worldPosition),
                    packet
            );
    }

    public void readClient(CompoundTag tag) {
        if (tag.contains("running")) renderData.running = tag.getBoolean("running");
        if (tag.contains("recipeRunning")) recipeRunning = tag.getBoolean("recipeRunning");
        if (tag.contains("anim")) renderData.anim = tag.getFloat("anim");
        clientEnergyStored = tag.getInt("energyStored");
        clientEnergyMax = tag.getInt("energyMax");
        recipeProgress = tag.getInt("recipeProgress");
        recipeMaxProgress = tag.getInt("recipeMaxProgress");
        if (tag.contains("hasInputItems"))
            clientHasInputItems = tag.getBoolean("hasInputItems");
        if (tag.contains("processCSwingStarted"))
            processCSwingStarted = tag.getBoolean("processCSwingStarted");

        if (tag.contains("processCSwingFinished"))
            processCSwingFinished = tag.getBoolean("processCSwingFinished");


    }
}
