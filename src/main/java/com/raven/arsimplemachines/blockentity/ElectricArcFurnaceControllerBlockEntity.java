package com.raven.arsimplemachines.blockentity;

import ARLib.ARLibRegistry;

import ARLib.blockentities.EntityItemInputBlock;
import ARLib.blockentities.EntityItemOutputBlock;
import ARLib.multiblockCore.EntityMultiblockMachineMaster;
import ARLib.multiblockCore.BlockMultiblockMaster;
import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import com.raven.arsimplemachines.block.ElectricArcFurnaceControllerBlock;
import com.raven.arsimplemachines.menu.ElectricArcFurnaceMenu;
import com.raven.arsimplemachines.recipe.MachineRecipeInput;
import com.raven.arsimplemachines.recipe.MachineRecipeMatcher;
import com.raven.arsimplemachines.recipe.eaf.ElectricArcFurnaceRecipe;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;              // ✅ ADDED
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

import static com.raven.arsimplemachines.block.ElectricArcFurnaceControllerBlock.RUNNING;

public class ElectricArcFurnaceControllerBlockEntity extends EntityMultiblockMachineMaster
        implements INetworkTagReceiver, MenuProvider {

    public static class RenderData {
        public boolean running = false;
        public float arcIntensity = 0f;
    }
    private Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    private int minX() {
        return switch (getFacing()) {
            case NORTH -> -2;
            case SOUTH -> -2;
            case EAST  -> -4;
            case WEST  -> 0;
            default -> 0;
        };
    }

    private int maxX() {
        return switch (getFacing()) {
            case NORTH -> 2;
            case SOUTH -> 2;
            case EAST  -> 0;
            case WEST  -> 4;
            default -> 0;
        };
    }

    private int minY() { return -1; }
    private int maxY() { return 3; }

    private int minZ() {
        return switch (getFacing()) {
            case NORTH -> 0;
            case SOUTH -> -4;
            case EAST  -> -2;
            case WEST  -> -2;
            default -> 0;
        };
    }

    private int maxZ() {
        return switch (getFacing()) {
            case NORTH -> 4;
            case SOUTH -> 0;
            case EAST  -> 2;
            case WEST  -> 2;
            default -> 0;
        };
    }

    public RenderData renderData = new RenderData();
    public ElectricArcFurnaceRecipe currentRecipe;
    private int restoreDelay = 0;

    public boolean recipeRunning = false;
    private int recipeProgress = 0;
    private int recipeMaxProgress = 0;
    private int clientEnergyStored = 0;
    private int clientEnergyMax = 0;

    private boolean clientHasInputItems = false;
    public boolean getClientHasInputItems() { return clientHasInputItems; }

    public ElectricArcFurnaceControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_ARC_FURNACE_CONTROLLER.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Electric Arc Furnace");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new ElectricArcFurnaceMenu(windowId, inv, this.getBlockPos());
    }

    @Override
    public Object[][][] getStructure() {
        return new Object[][][]{
                // Layer 4
                {
                        {null,null,null,null,null},
                        {null,'E','B','E',null},
                        {null,'B','B','B',null},
                        {null,'B','E','B',null},
                        {null,null,null,null,null}
                },
                // Layer 3
                {
                        {null,'B','B','B',null},
                        {'B', 'X', null, 'X', 'B'},
                        {'B', null,  null,  null, 'B'},
                        {'B', null, 'X', null, 'B'},
                        {null,'B','B','B',null}
                },


                // Layer 2
                {
                        {'B', 'B',  'B',  'B', 'B'},
                        {'B', null, null, null, 'B'},
                        {'B', null, null, null, 'B'},
                        {'B', null, null, null, 'B'},
                        {'B', 'B',  'B',  'B', 'B'}
                },
                // Layer 1
                {
                        {'B', 'B',  'C',  'B', 'B'},
                        {'I', null, null, null, 'O'},
                        {'I', null, null, null, 'O'},
                        {'I', null, null, null, 'O'},
                        {'B', 'B',  'B',  'B', 'B'}
                },

                // Layer 0
                {
                        {'B','B','B','B','B'},
                        {'B','B','B','B','B'},
                        {'B','B','B','B','B'},
                        {'B','B','B','B','B'},
                        {'B','B','B','B','B'}
                }
        };
    }

    public static final Map<Character, List<Block>> MAPPING = Map.of(
            'B', List.of(ModBlocks.BLAST_BRICK.get()),
            'I', List.of(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get()),
            'O', List.of(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get()),
            'X', List.of(ARLibRegistry.BLOCK_COIL_COPPER.get()),
            'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
            'C', List.of(ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get())
    );

    @Override
    public HashMap<Character, List<Block>> getCharMapping() {
        return new HashMap<>(MAPPING);
    }
    @Override
    public boolean shouldHideBlock(int y, int z, int x, BlockState stateInWorld) {

        Block b = stateInWorld.getBlock();

        // Always keep casing visible
        if (b == ModBlocks.BLAST_BRICK.get()) return false;

        // Keep controller visible
        if (b == ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get()) return false;

        // Keep ARLib functional blocks visible
        if (b == ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get()) return false;
        if (b == ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get()) return false;
        if (b == ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()) return false;
        if (b == ARLibRegistry.BLOCK_COIL_COPPER.get()) return false;

        // Everything else (internal air/cavity blocks) should be hidden
        return true;
    }


    @Override
    public Vec3i getControllerOffset(Object[][][] structure) {
        for (int y = 0; y < structure.length; y++)
            for (int z = 0; z < structure[y].length; z++)
                for (int x = 0; x < structure[y][z].length; x++)
                    if (structure[y][z][x] instanceof Character ch && (ch == 'c' || ch == 'C'))
                        return new Vec3i(x, y, z);

        return new Vec3i(structure[0][0].length / 2, structure.length / 2, structure[0].length / 2);
    }

    @Override
    public void onStructureComplete() {
        super.onStructureComplete();
        restoreDelay = 2; // Delay restoration by 1 second (20 ticks)
    }

    @Override
    public void onStructureInvalid() {
        recipeRunning = false;
        renderData.running = false;
        sendUpdatePacket(null);
    }
//    @Override
//    public boolean[][][] hideBlocks() {
//        Object[][][] structure = getStructure();
//        boolean[][][] result = new boolean[structure.length][structure[0].length][structure[0][0].length];
//        return result;
//    }

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
    public boolean hasAnyInputItems() {
        for (IItemHandler handler : findAllItemInputs()) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                if (!handler.getStackInSlot(slot).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        // Multiblock not formed
        if (!getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) {
            System.out.println("### ARLIB: BLOCKSTATE STILL NOT FORMED at " + worldPosition);
            resetMachineState();
            return;
        }

        syncEnergy();

        // Try starting recipe
        if (!recipeRunning) {
            if (currentRecipe == null) {
                tryStartRecipe();
            }
            if (!recipeRunning || currentRecipe == null) {
                return;
            }
        }

        // Get ALL energy storages
        List<IEnergyStorage> storages = getAllEnergyStorages();
        if (storages.isEmpty()) {
            resetMachineState();
            return;
        }

        // Sum total energy across all blocks
        int totalEnergy = 0;
        for (IEnergyStorage es : storages) {
            totalEnergy += es.getEnergyStored();
        }

        int energyPerTick = currentRecipe.getEnergyPerTick();

        // Stop only when TOTAL energy is insufficient
        if (totalEnergy < energyPerTick) {

            // Stop the recipe cleanly
            recipeRunning = false;
            renderData.running = false;
            currentRecipe = null;
            recipeProgress = 0;
            recipeMaxProgress = 0;

            // Update blockstate
            if (getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) {
                BlockState state = level.getBlockState(worldPosition);
                if (state.hasProperty(RUNNING)) {
                    level.setBlock(worldPosition, state.setValue(RUNNING, false), 3);
                }
            }
            syncEnergy();
            clientHasInputItems = hasAnyInputItems();
            sendUpdatePacket(null);
            return;
        }

        // ---------------------------------------------------------
        // EVEN ENERGY DISTRIBUTION ACROSS ALL BLOCKS
        // ---------------------------------------------------------
        int blocks = storages.size();

        // Equal share per block
        int perBlock = energyPerTick / blocks;
        int remainder = energyPerTick % blocks;

        // First pass: drain equal amounts
        for (IEnergyStorage es : storages) {
            int take = Math.min(es.getEnergyStored(), perBlock);
            es.extractEnergy(take, false);
        }

        // Second pass: drain remainder from blocks that still have energy
        int remaining = remainder;
        for (IEnergyStorage es : storages) {
            if (remaining <= 0) break;
            int take = Math.min(es.getEnergyStored(), remaining);
            es.extractEnergy(take, false);
            remaining -= take;
        }

        // Continue recipe
        recipeProgress++;
        renderData.arcIntensity = (recipeProgress % 20) / 20.0f;
        sendUpdatePacket(null);

        if (recipeProgress >= recipeMaxProgress) {
            finishRecipe();
        }

    }

    private void syncEnergy() {
        List<IEnergyStorage> storages = getAllEnergyStorages();
        if (storages.isEmpty()) return;

        int totalStored = 0;
        int totalMax = 0;

        for (IEnergyStorage es : storages) {
            totalStored += es.getEnergyStored();
            totalMax += es.getMaxEnergyStored();
        }

        clientEnergyStored = totalStored;
        clientEnergyMax = totalMax;
        clientHasInputItems = hasAnyInputItems();
        sendUpdatePacket(null);
    }


    private void resetMachineState() {
        recipeRunning = false;
        currentRecipe = null;
        recipeProgress = 0;
        recipeMaxProgress = 0;
        renderData.running = false;
        renderData.arcIntensity = 0f;
        if (getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.hasProperty(RUNNING)) {
                level.setBlock(worldPosition, state.setValue(RUNNING, false), 3);
            }
        }
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

            if (storage != null) {
                list.add(storage);
            }
        }

        return list;
    }

    private IEnergyStorage getEnergyStorage() {
        BlockPos energyPos = findSpecificBlock(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get());
        if (energyPos == null) return null;

        BlockEntity be = level.getBlockEntity(energyPos);
        if (be == null) return null;

        return level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                energyPos,
                level.getBlockState(energyPos),
                be,
                null
        );
    }

    private void tryStartRecipe() {
        List<IItemHandler> itemInputs = findAllItemInputs();
        if (itemInputs.isEmpty()) return;

        // --- MULTI‑BLOCK ENERGY CHECK ---
        List<IEnergyStorage> storages = getAllEnergyStorages();
        if (storages.isEmpty()) return;

        int totalEnergy = 0;
        for (IEnergyStorage es : storages) {
            totalEnergy += es.getEnergyStored();
        }

        MachineRecipeInput recipeInput = new MachineRecipeInput();

        // Collect item inputs
        for (IItemHandler handler : itemInputs) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    recipeInput.addItem(stack);
                }
            }
        }


        ElectricArcFurnaceRecipe recipe = MachineRecipeMatcher.findMatch(
                level,
                ModRecipeTypes.ELECTRIC_ARC_FURNACE_TYPE,
                recipeInput
        );

        if (recipe == null) return;

        if (recipe.getItemInputs().isEmpty() && recipe.getItemTags().isEmpty()) return;

        // --- FIXED: Use TOTAL energy instead of single block ---
        if (totalEnergy < recipe.getEnergyPerTick()) {
            resetMachineState();   // <-- FIX
            return;                // <-- REQUIRED
        }

        if (!hasRequiredItems(recipe, itemInputs)) return;

        consumeRequiredItems(recipe, itemInputs);

        currentRecipe = recipe;
        recipeRunning = true;
        recipeProgress = 0;
        recipeMaxProgress = recipe.getProcessingTime();
        renderData.running = true;

        if (getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.hasProperty(RUNNING)) {
                level.setBlock(worldPosition, state.setValue(RUNNING, true), 3);
            }
        }

        sendUpdatePacket(null);
    }


    private boolean hasRequiredItems(ElectricArcFurnaceRecipe recipe, List<IItemHandler> handlers) {
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

    private void consumeRequiredItems(ElectricArcFurnaceRecipe recipe, List<IItemHandler> handlers) {
        Map<IItemHandler, Map<Integer, Integer>> usedCounts = new HashMap<>();

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
        for (int dx = minX(); dx <= maxX(); dx++)
            for (int dy = minY(); dy <= maxY(); dy++)
                for (int dz = minZ(); dz <= maxZ(); dz++) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    if (level.getBlockState(p).getBlock() == blockType)
                        list.add(p);
                }
        return list;
    }


    private BlockPos findSpecificBlock(Block blockType) {
        for (int dx = minX(); dx <= maxX(); dx++)
            for (int dy = minY(); dy <= maxY(); dy++)
                for (int dz = minZ(); dz <= maxZ(); dz++) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    if (level.getBlockState(p).getBlock() == blockType)
                        return p;
                }
        return null;
    }


    public List<IItemHandler> findAllItemInputs() {
        List<IItemHandler> list = new ArrayList<>();

        for (BlockPos pos : findAllBlocks(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get())) {
            BlockEntity be = level.getBlockEntity(pos);

            IItemHandler cap = level.getCapability(
                    Capabilities.ItemHandler.BLOCK,
                    pos,
                    level.getBlockState(pos),
                    be,
                    null
            );

            if (cap != null) {
                list.add(cap);
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
        ElectricArcFurnaceRecipe finished = currentRecipe;

        recipeRunning = false;
        renderData.running = false;
        renderData.arcIntensity = 0f;

        if (finished != null) {
            List<IItemHandler> itemOutputs = findAllItemOutputs();
            for (ItemStack out : finished.getItemOutputs()) {
                ItemStack remainder = out.copy();

                for (IItemHandler handler : itemOutputs) {
                    for (int slot = 0; slot < handler.getSlots(); slot++) {
                        remainder = handler.insertItem(slot, remainder, false);
                        if (remainder.isEmpty()) break;
                    }
                    if (remainder.isEmpty()) break;
                }
            }
        }

        currentRecipe = null;
        recipeProgress = 0;
        recipeMaxProgress = 0;
        if (getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.hasProperty(RUNNING)) {
                level.setBlock(worldPosition, state.setValue(RUNNING, false), 3);
            }
        }
        sendUpdatePacket(null);
    }


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
            renderData.arcIntensity = (float) Math.abs(Math.sin(level.getGameTime() * 0.25f));
        } else {
            renderData.arcIntensity = 0f;
        }
    }

    public int getRecipeProgress() {
        return recipeProgress;
    }

    public int getRecipeMaxProgress() {
        return recipeMaxProgress;
    }

    public int getClientEnergyStored() {
        return clientEnergyStored;
    }

    public int getClientEnergyMax() {
        return clientEnergyMax;
    }

    public void sendUpdatePacket(ServerPlayer specificPlayer) {
        if (level == null || level.isClientSide) return;

        CompoundTag tag = new CompoundTag();
        tag.putBoolean("running", renderData.running);
        tag.putBoolean("recipeRunning", recipeRunning);
        tag.putFloat("arcIntensity", renderData.arcIntensity);
        tag.putInt("energyStored", clientEnergyStored);
        tag.putInt("energyMax", clientEnergyMax);
        tag.putInt("recipeProgress", recipeProgress);
        tag.putInt("recipeMaxProgress", recipeMaxProgress);
        tag.putBoolean("hasInputItems", clientHasInputItems);

        PacketBlockEntity packet = PacketBlockEntity.getBlockEntityPacket(this, tag);

        if (specificPlayer != null) {
            PacketDistributor.sendToPlayer(specificPlayer, packet);
        } else {
            PacketDistributor.sendToPlayersTrackingChunk(
                    (net.minecraft.server.level.ServerLevel) level,
                    new net.minecraft.world.level.ChunkPos(worldPosition),
                    packet
            );

        }
    }

    public void readClient(CompoundTag tag) {
        if (tag.contains("running")) renderData.running = tag.getBoolean("running");
        if (tag.contains("recipeRunning")) recipeRunning = tag.getBoolean("recipeRunning");
        if (tag.contains("arcIntensity")) renderData.arcIntensity = tag.getFloat("arcIntensity");
        clientEnergyStored = tag.getInt("energyStored");
        clientEnergyMax = tag.getInt("energyMax");
        recipeProgress = tag.getInt("recipeProgress");
        recipeMaxProgress = tag.getInt("recipeMaxProgress");
        if (tag.contains("hasInputItems"))
            clientHasInputItems = tag.getBoolean("hasInputItems");

    }
}
