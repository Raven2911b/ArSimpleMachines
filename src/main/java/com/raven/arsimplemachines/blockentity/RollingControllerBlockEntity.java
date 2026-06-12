package com.raven.arsimplemachines.blockentity;

import ARLib.ARLibRegistry;
import ARLib.multiblockCore.EntityMultiblockMachineMaster;
import ARLib.multiblockCore.BlockMultiblockMaster;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import com.raven.arsimplemachines.recipe.RollingRecipeRegistry;
import com.raven.arsimplemachines.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

public class RollingControllerBlockEntity extends EntityMultiblockMachineMaster implements INetworkTagReceiver {

    public static class RenderData {
        public float rollerSpin = 0f;
        public float pressOffset = 0f;
        public boolean running = false;
    }

    public RenderData renderData = new RenderData();

    private static final int ENERGY_PER_TICK = 20;
    private boolean recipeRunning = false;
    private int recipeProgress = 0;
    private int recipeMaxProgress = 0;

    public RollingControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROLLING_CONTROLLER.get(), pos, state);
    }

    @Override
    public Object[][][] getStructure() {
        return new Object[][][]{
                { { 'C', 'S', 'O' } },
                { { 'E', 'R', 'I' } }
        };
    }

    public static final Map<Character, List<Block>> MAPPING = Map.of(
            'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
            'S', List.of(ARLibRegistry.BLOCK_STRUCTURE.get()),
            'I', List.of(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get()),
            'O', List.of(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get()),
            'R', List.of(ARLibRegistry.BLOCK_MOTOR.get()),
            'C', List.of(ModBlocks.ROLLING_CONTROLLER.get())
    );

    @Override
    public HashMap<Character, List<Block>> getCharMapping() {
        return new HashMap<>(MAPPING);
    }

    @Override
    public Vec3i getControllerOffset(Object[][][] structure) {
        return new Vec3i(0, 0, 0);
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

    public void tick() {
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
        IEnergyStorage storage = getEnergyStorage();
        if (storage == null || storage.getEnergyStored() < ENERGY_PER_TICK) return;

        BlockPos inputPos = findNearbyBlock(EntityItemInputBlock.class);
        if (inputPos == null) return;

        BlockEntity be = level.getBlockEntity(inputPos);
        if (!(be instanceof EntityItemInputBlock input)) return;

        for (int slot = 0; slot < input.inventory.getSlots(); slot++) {
            var stack = input.inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            var recipe = RollingRecipeRegistry.findRecipe(stack);
            if (recipe != null) {
                input.inventory.extractItem(slot, 1, false);

                recipeRunning = true;
                recipeProgress = 0;
                recipeMaxProgress = recipe.processingTime;

                renderData.running = true;
                sendUpdatePacket(null);
                return;
            }
        }
    }

    private void finishRecipe() {
        recipeRunning = false;
        renderData.running = false;

        sendUpdatePacket(null);
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

    public void clientTick() {
        if (level == null || !level.isClientSide) return;

        if (renderData.running) {
            renderData.rollerSpin = (renderData.rollerSpin + 8f) % 360f;
            renderData.pressOffset = (float) Math.sin(level.getGameTime() * 0.05f) * 0.25f;
        } else {
            renderData.rollerSpin = 0f;
            renderData.pressOffset = 0f;
        }
    }

    public void sendUpdatePacket(ServerPlayer specificPlayer) {
        if (level == null || level.isClientSide) return;

        CompoundTag tag = new CompoundTag();
        tag.putBoolean("running", renderData.running);
        tag.putFloat("rollerSpin", renderData.rollerSpin);
        tag.putFloat("pressOffset", renderData.pressOffset);

        PacketBlockEntity packet = PacketBlockEntity.getBlockEntityPacket(this, tag);

        if (specificPlayer != null)
            PacketDistributor.sendToPlayer(specificPlayer, packet);
        else
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(worldPosition), packet);
    }

    @Override
    public void readClient(CompoundTag tag) {
        if (tag.contains("running")) renderData.running = tag.getBoolean("running");
        if (tag.contains("rollerSpin")) renderData.rollerSpin = tag.getFloat("rollerSpin");
        if (tag.contains("pressOffset")) renderData.pressOffset = tag.getFloat("pressOffset");
    }

    @Override
    public void readServer(CompoundTag tag, ServerPlayer sender) {}
}
