package com.raven.arsimplemachines.blockentity;

import ARLib.ARLibRegistry;
import ARLib.multiblockCore.EntityMultiblockMachineMaster;
import ARLib.multiblockCore.BlockMultiblockMaster;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import com.raven.arsimplemachines.recipe.LatheRecipeRegistry;
import com.raven.arsimplemachines.registry.ModBlocks;
import net.minecraft.core.BlockPos;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LatheControllerBlockEntity extends EntityMultiblockMachineMaster implements INetworkTagReceiver {

    // -------------------------
    // Rendering / Animation Data
    // -------------------------
    public static class RenderData {
        public float shaftRotation = 0f;
        public float toolOffset = 0f;
        public float rodOffset = 0f;
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

    public LatheControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LATHE_CONTROLLER.get(), pos, state);
    }

    // -------------------------
    // ARLib Multiblock Definition
    // -------------------------

    @Override
    public Object[][][] getStructure() {
        // MUST match projector pattern; no nulls, no empty layer
        return new Object[][][]{
                { { 'C', 'M', null, 'O' } },   // bottom layer (y = 0)
                { { 'E', 'S', 'S', 'I' } }    // top layer   (y = 1)
        };
    }

    // Correct ARLib block mapping
    public static final Map<Character, List<Block>> MAPPING = Map.of(
            'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
            'S', List.of(ARLibRegistry.BLOCK_STRUCTURE.get()),
            'I', List.of(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get()),
            'O', List.of(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get()),
            'M', List.of(ARLibRegistry.BLOCK_MOTOR.get()),
            'C', List.of(ModBlocks.LATHE_CONTROLLER.get())
    );

    @Override
    public HashMap<Character, List<Block>> getCharMapping() {
        return new HashMap<>(MAPPING);
    }
    /**
     * Ensure ARLib never receives a null controller offset.
     * If the structure contains a 'c' or 'C' character it will be returned;
     * otherwise a fallback center offset is used and a warning is printed.
     */
    @Override
    public Vec3i getControllerOffset(Object[][][] structure) {
        if (structure == null) return new Vec3i(0, 0, 0);

        for (int y = 0; y < structure.length; y++) {
            for (int z = 0; z < structure[y].length; z++) {
                for (int x = 0; x < structure[y][z].length; x++) {
                    Object v = structure[y][z][x];
                    if (v instanceof Character) {
                        char ch = (Character) v;
                        if (ch == 'c' || ch == 'C') {
                            return new Vec3i(x, y, z);
                        }
                    }
                }
            }
        }

        // Fallback: use center of the structure to avoid NPEs in ARLib
        int cy = Math.max(0, structure.length / 2);
        int cz = Math.max(0, structure[0].length / 2);
        int cx = Math.max(0, structure[0][0].length / 2);
        return new Vec3i(cx, cy, cz);
    }


    // -------------------------
    // Multiblock Callbacks
    // -------------------------

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

    // -------------------------
    // Server Tick (Gameplay Logic)
    // -------------------------

    public void tick() {
        boolean formed = getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED);


        if (level == null || level.isClientSide) return;

        if (!getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) {
            recipeRunning = false;
            return;
        }

        if (!recipeRunning) {
            tryStartRecipe();
        } else {
            IEnergyStorage storage = getEnergyStorage();
            if (storage == null) return;

            if (storage.getEnergyStored() < ENERGY_PER_TICK) return;

            storage.extractEnergy(ENERGY_PER_TICK, false);
            recipeProgress++;

            if (recipeProgress >= recipeMaxProgress) {
                finishRecipe();
            }
        }
    }

    private IEnergyStorage getEnergyStorage() {
        BlockPos below = worldPosition.below();
        BlockEntity be = level.getBlockEntity(below);
        if (be == null) return null;

        return level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                below,
                level.getBlockState(below),
                be,
                null
        );
    }

    private void tryStartRecipe() {
        if (!getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) return;

        IEnergyStorage storage = getEnergyStorage();
        if (storage == null || storage.getEnergyStored() < ENERGY_PER_TICK) return;

        BlockPos inputPos = findInputBlock();
        if (inputPos == null) return;

        BlockEntity be = level.getBlockEntity(inputPos);
        if (!(be instanceof EntityItemInputBlock input)) return;

        for (int slot = 0; slot < input.inventory.getSlots(); slot++) {
            ItemStack stack = input.inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            LatheRecipeRegistry.LatheRecipe recipe = LatheRecipeRegistry.findRecipe(stack);
            if (recipe != null) {
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
    }

    private void finishRecipe() {
        recipeRunning = false;
        renderData.running = false;

        LatheRecipeRegistry.LatheRecipe recipe = LatheRecipeRegistry.findRecipe(processingInput);
        if (recipe == null) {
            processingInput = ItemStack.EMPTY;
            sendUpdatePacket(null);
            return;
        }

        ItemStack output = recipe.output.copy();
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
        return findNearbyBlock(EntityItemInputBlock.class);
    }

    private BlockPos findOutputBlock() {
        return findNearbyBlock(EntityItemOutputBlock.class);
    }

    private BlockPos findNearbyBlock(Class<?> type) {
        for (int dx = -3; dx <= 3; dx++)
            for (int dy = -2; dy <= 2; dy++)
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    BlockEntity be = level.getBlockEntity(p);
                    if (type.isInstance(be)) return p;
                }
        return null;
    }

    // -------------------------
    // Client Tick (Animation)
    // -------------------------

    public void clientTick() {
        if (level == null || !level.isClientSide) return;

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

    // -------------------------
    // Networking
    // -------------------------

    public void sendUpdatePacket(ServerPlayer specificPlayer) {
        if (level == null || level.isClientSide) return;

        CompoundTag tag = new CompoundTag();
        tag.putBoolean("recipeRunning", recipeRunning);
        tag.putInt("recipeProgress", recipeProgress);
        tag.putInt("recipeMaxProgress", recipeMaxProgress);

        tag.putFloat("shaftRotation", renderData.shaftRotation);
        tag.putFloat("toolOffset", renderData.toolOffset);
        tag.putFloat("rodOffset", renderData.rodOffset);
        tag.putBoolean("running", renderData.running);

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

        if (tag.contains("shaftRotation")) renderData.shaftRotation = tag.getFloat("shaftRotation");
        if (tag.contains("toolOffset")) renderData.toolOffset = tag.getFloat("toolOffset");
        if (tag.contains("rodOffset")) renderData.rodOffset = tag.getFloat("rodOffset");
        if (tag.contains("running")) renderData.running = tag.getBoolean("running");
    }

    @Override
    public void readServer(CompoundTag tag, ServerPlayer sender) {
        // No server-side GUI actions yet
    }
}
