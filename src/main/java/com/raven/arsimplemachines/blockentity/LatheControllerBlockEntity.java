package com.raven.arsimplemachines.blockentity;

import ARLib.ARLibRegistry;
import ARLib.blockentities.EntityItemInputBlock;
import ARLib.blockentities.EntityItemOutputBlock;
import ARLib.multiblockCore.BlockMultiblockMaster;
import ARLib.multiblockCore.EntityMultiblockMachineMaster;
import ARLib.multiblockCore.EntityMultiblockPlaceholder;
import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import com.raven.arsimplemachines.menu.LatheMenu;
import com.raven.arsimplemachines.recipe.MachineRecipeInput;
import com.raven.arsimplemachines.recipe.MachineRecipeMatcher;
import com.raven.arsimplemachines.recipe.TagInput;
import com.raven.arsimplemachines.recipe.lathe.LatheRecipe;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import com.raven.arsimplemachines.registry.ModBlocks;
import com.raven.arsimplemachines.registry.ModRecipeTypes;
import com.raven.arsimplemachines.util.PatternScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
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
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LatheControllerBlockEntity extends EntityMultiblockMachineMaster implements INetworkTagReceiver, MenuProvider {

    public static class RenderData {
        public float shaftRotation = 0f;
        public float toolOffset = 0f;
        public float rodOffset = 0f;
        public boolean running = false;
    }

    public RenderData renderData = new RenderData();
    public LatheRecipe currentRecipe;

    public boolean recipeRunning = false;
    private int recipeProgress = 0;
    private int recipeMaxProgress = 0;
    private int clientEnergyStored = 0;
    private int clientEnergyMax = 0;

    // Scanner bounding box (relative to controller)
    private int minX() {
        return switch (getFacing()) {
            case NORTH -> -3;
            case SOUTH -> 0;
            case EAST  -> 0;
            case WEST  -> 0;   // your WEST values
            default -> 0;
        };
    }

    private int maxX() {
        return switch (getFacing()) {
            case NORTH -> 0;
            case SOUTH -> 3;
            case EAST  -> 0;
            case WEST  -> 0;   // your WEST values
            default -> 0;
        };
    }

    private int minY() { return -1; }
    private int maxY() { return 0; }

    private int minZ() {
        return switch (getFacing()) {
            case NORTH -> 0;
            case SOUTH -> 0;
            case EAST  -> -3;
            case WEST  -> 0;
            default -> 0;
        };
    }

    private int maxZ() {
        return switch (getFacing()) {
            case NORTH -> 0;
            case SOUTH -> 0;
            case EAST  -> 0;
            case WEST  -> 3;   // your WEST values
            default -> 0;
        };
    }


    private Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }


    public LatheControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LATHE_CONTROLLER.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Lathe");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new LatheMenu(windowId, inv, this.getBlockPos());
    }

    @Override
    public Object[][][] getStructure() {
        // Your original Lathe multiblock pattern, unchanged
        return new Object[][][] {
                { { 'C', 'M', null, 'O' } },
                { { 'E', 'S', 'S', 'I' } }
        };
    }

    public static final Map<Character, List<Block>> MAPPING = Map.of(
            'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
            'S', BuiltInRegistries.BLOCK.stream().toList(),
            'I', List.of(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get()),
            'O', List.of(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get()),
            'M', BuiltInRegistries.BLOCK.stream().toList(),
            'C', List.of(ModBlocks.LATHE_CONTROLLER.get())
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

        return new Vec3i(structure[0][0].length / 2, structure.length / 2, structure[0].length / 2);
    }

    @Override
    public void onStructureComplete() {
        renderData.running = false;
        sendUpdatePacket(null);
    }

    @Override
    public void onStructureInvalid() {
        recipeRunning = false;
        renderData.running = false;
        sendUpdatePacket(null);
    }

    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 2,
                worldPosition.getY() - 1,
                worldPosition.getZ() - 2,
                worldPosition.getX() + 3,
                worldPosition.getY() + 3,
                worldPosition.getZ() + 3
        );
    }

    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public void readServer(CompoundTag tag, ServerPlayer sender) {
        // No server-side direct tag handling needed
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

    // ---------------------------------------------------------
    // REQUIRED INPUTS: DIRECT ITEMS
    // ---------------------------------------------------------
    private boolean hasRequiredItems(LatheRecipe recipe, List<IItemHandler> handlers) {
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

            if (found < needed) {
                return false;
            }
        }

        return true;
    }

    private void consumeRequiredItems(LatheRecipe recipe, List<IItemHandler> handlers) {
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

    // ---------------------------------------------------------
    // REQUIRED INPUTS: TAG ITEMS
    // ---------------------------------------------------------
    private boolean hasRequiredTags(LatheRecipe recipe, List<IItemHandler> handlers) {
        for (TagInput tag : recipe.getItemTags()) {

            TagKey<Item> tagKey = TagKey.create(
                    BuiltInRegistries.ITEM.key(),
                    tag.tag()
            );

            int needed = tag.count();
            int matched = 0;

            for (IItemHandler handler : handlers) {
                for (int slot = 0; slot < handler.getSlots(); slot++) {

                    ItemStack stack = handler.getStackInSlot(slot);

                    if (!stack.isEmpty() && stack.is(tagKey)) {
                        matched += stack.getCount();
                        if (matched >= needed) break;
                    }
                }
                if (matched >= needed) break;
            }

            if (matched < needed) {
                return false;
            }
        }

        return true;
    }

    private void consumeRequiredTags(LatheRecipe recipe, List<IItemHandler> handlers) {
        for (TagInput tag : recipe.getItemTags()) {

            TagKey<Item> tagKey = TagKey.create(
                    BuiltInRegistries.ITEM.key(),
                    tag.tag()
            );

            int remaining = tag.count();

            for (IItemHandler handler : handlers) {
                for (int slot = 0; slot < handler.getSlots(); slot++) {

                    if (remaining <= 0) break;

                    ItemStack stack = handler.getStackInSlot(slot);

                    if (!stack.isEmpty() && stack.is(tagKey)) {

                        int take = Math.min(stack.getCount(), remaining);

                        handler.extractItem(slot, take, false);
                        remaining -= take;
                    }
                }

                if (remaining <= 0) break;
            }
        }
    }

    // ---------------------------------------------------------
    // TICK LOGIC (NO FLUIDS)
    // ---------------------------------------------------------
    public void tick() {
        if (level == null || level.isClientSide) return;

        if (!getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) {
            resetMachineState();
            return;
        }

        syncEnergy();

        // START RECIPE ONLY IF NOT RUNNING
        if (!recipeRunning) {

            if (currentRecipe == null) {
                tryStartRecipe();
            }

            if (!recipeRunning || currentRecipe == null) {
                return;
            }
        }

        IEnergyStorage storage = getEnergyStorage();
        if (storage == null) {
            resetMachineState();
            return;
        }

        int energyPerTick = currentRecipe.getEnergyPerTick();
        if (storage.getEnergyStored() < energyPerTick) {
            sendUpdatePacket(null);
            return;
        }

        storage.extractEnergy(energyPerTick, false);
        recipeProgress++;
        sendUpdatePacket(null);

        if (recipeProgress >= recipeMaxProgress) {
            finishRecipe();
        }
    }

    private void syncEnergy() {
        IEnergyStorage storage = getEnergyStorage();
        if (storage == null) return;

        clientEnergyStored = storage.getEnergyStored();
        clientEnergyMax = storage.getMaxEnergyStored();
        sendUpdatePacket(null);
    }

    private void resetMachineState() {
        recipeRunning = false;
        currentRecipe = null;
        recipeProgress = 0;
        recipeMaxProgress = 0;
        renderData.running = false;
        sendUpdatePacket(null);
    }

    private void tryStartRecipe() {
        List<IItemHandler> itemInputs = findAllItemInputs();
        if (itemInputs.isEmpty()) return;

        IEnergyStorage storage = getEnergyStorage();
        if (storage == null) return;

        MachineRecipeInput recipeInput = new MachineRecipeInput();

        for (IItemHandler handler : itemInputs) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    recipeInput.addItem(stack);
                }
            }
        }

        LatheRecipe recipe = MachineRecipeMatcher.findMatch(
                level,
                ModRecipeTypes.LATHE_TYPE,
                recipeInput
        );

        if (recipe == null) return;

        if (storage.getEnergyStored() < recipe.getEnergyPerTick()) return;

        if (!recipe.getItemInputs().isEmpty()) {
            if (!hasRequiredItems(recipe, itemInputs)) return;
        }

        if (!recipe.getItemTags().isEmpty()) {
            if (!hasRequiredTags(recipe, itemInputs)) return;
        }

        currentRecipe = recipe;
        recipeRunning = true;
        recipeProgress = 0;
        recipeMaxProgress = recipe.getProcessingTime();
        renderData.running = true;

        consumeRequiredItems(recipe, itemInputs);
        consumeRequiredTags(recipe, itemInputs);

        sendUpdatePacket(null);
    }

    private void finishRecipe() {
        LatheRecipe finished = currentRecipe;

        recipeRunning = false;
        renderData.running = false;

        if (finished != null) {

            // ITEM OUTPUTS ONLY (no fluids)
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

        sendUpdatePacket(null);
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
            renderData.shaftRotation = (renderData.shaftRotation + 8f) % 360f;
            float t = (float) Math.sin(level.getGameTime() * 0.05f);
            renderData.toolOffset = (t * 0.5f + 0.5f) * 1.11f;
            renderData.rodOffset = (renderData.rodOffset + 8f) % 360f;
        } else {
            renderData.shaftRotation = 0f;
            renderData.toolOffset = 0f;
            renderData.rodOffset = 0f;
        }
    }

    public int getRecipeProgress() {
        return recipeProgress;
    }

    public int getRecipeMaxProgress() {
        return recipeMaxProgress;
    }

    public IItemHandler getInputHandler() {
        BlockPos inputPos = findSpecificBlock(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get());
        if (inputPos == null) return null;

        BlockEntity be = level.getBlockEntity(inputPos);
        if (be instanceof EntityItemInputBlock input)
            return input.inventory;

        return null;
    }

    public IItemHandler getOutputHandler() {
        BlockPos outputPos = findSpecificBlock(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get());
        if (outputPos == null) return null;

        BlockEntity be = level.getBlockEntity(outputPos);
        if (be instanceof EntityItemOutputBlock out)
            return out.inventory;

        return null;
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
        tag.putInt("energyStored", clientEnergyStored);
        tag.putInt("energyMax", clientEnergyMax);
        tag.putInt("recipeProgress", recipeProgress);
        tag.putInt("recipeMaxProgress", recipeMaxProgress);

        PacketBlockEntity packet = PacketBlockEntity.getBlockEntityPacket(this, tag);

        if (specificPlayer != null)
            PacketDistributor.sendToPlayer(specificPlayer, packet);
        else
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(worldPosition), packet);
    }

    @Override
    public void readClient(CompoundTag tag) {
        if (tag.contains("running")) renderData.running = tag.getBoolean("running");
        if (tag.contains("recipeRunning")) recipeRunning = tag.getBoolean("recipeRunning");
        if (tag.contains("energyStored")) clientEnergyStored = tag.getInt("energyStored");
        if (tag.contains("energyMax")) clientEnergyMax = tag.getInt("energyMax");
        if (tag.contains("recipeProgress")) recipeProgress = tag.getInt("recipeProgress");
        if (tag.contains("recipeMaxProgress")) recipeMaxProgress = tag.getInt("recipeMaxProgress");
    }
}
