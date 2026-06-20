package com.raven.arsimplemachines.blockentity;

import ARLib.ARLibRegistry;
import ARLib.multiblockCore.EntityMultiblockMachineMaster;
import ARLib.multiblockCore.BlockMultiblockMaster;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import com.raven.arsimplemachines.recipe.RollingRecipeRegistry;
import com.raven.arsimplemachines.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
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
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import com.raven.arsimplemachines.menu.RollingMenu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RollingControllerBlockEntity extends EntityMultiblockMachineMaster implements INetworkTagReceiver, MenuProvider {

    public static class RenderData {
        public float rollerSpin = 0f;
        public float pressOffset = 0f;
        public float ingotOffset = 0f;
        public float plateOffset = 0f;
        public boolean running = false;
    }

    public RenderData renderData = new RenderData();
    private RollingRecipeRegistry.RollingRecipe currentRecipe;

    private static final int ENERGY_PER_TICK = 20;
    private boolean recipeRunning = false;
    private int recipeProgress = 0;
    private int recipeMaxProgress = 0;
    private int clientEnergyStored = 0;
    private int clientEnergyMax = 0;
    private int clientFluidAmount = 0;
    private int clientFluidCapacity = 0;

    public RollingControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROLLING_CONTROLLER.get(), pos, state);
    }
    @Override
    public Component getDisplayName() {
        return Component.literal("Rolling Machine");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new RollingMenu(windowId, inv, this.getBlockPos());
    }


    @Override
    public Object[][][] getStructure() {
        return new Object[][][]{

                // Y = 0 (top)
                {
                        { 'C', null, null ,null},   // Z = 0
                        { 'I', 'S', 'S', null},   // Z = 1
                        { 'E', 'S', 'S', null}    // Z = 2
                },

                // Y = 1 (bottom)
                {
                        { 'F', 'R', 'R',null },   // Z = 0
                        { null, 'S', 'S','O' },   // Z = 1
                        { null, 'S', 'S','O' }    // Z = 2
                }
        };
    }



    public static final Map<Character, List<Block>> MAPPING = Map.of(
            'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
            'F', List.of(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get()),
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
        for (int y = 0; y < structure.length; y++)
            for (int z = 0; z < structure[y].length; z++)
                for (int x = 0; x < structure[y][z].length; x++)
                    if (structure[y][z][x] instanceof Character ch && (ch == 'c' || ch == 'C'))
                        return new Vec3i(x, y, z);

        // fallback center
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

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (!getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) {
            recipeRunning = false;
            currentRecipe = null;
            return;
        }

        // ⭐ Always sync energy to client
        IEnergyStorage storage = getEnergyStorage();
        if (storage != null) {
            clientEnergyStored = storage.getEnergyStored();
            clientEnergyMax = storage.getMaxEnergyStored();
            sendUpdatePacket(null);
        }

        if (!recipeRunning) {
            tryStartRecipe();
            return;
        }

        if (currentRecipe == null) {
            recipeRunning = false;
            return;
        }

        if (storage == null) return;
        if (storage.getEnergyStored() < currentRecipe.energyPerTick) return;

        storage.extractEnergy(currentRecipe.energyPerTick, false);
        recipeProgress++;
        sendUpdatePacket(null);
        if (recipeProgress >= recipeMaxProgress) {
            finishRecipe();
        }
        // ⭐ Sync fluid to client
        BlockPos fluidPos = findSpecificBlock(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get());
        if (fluidPos != null) {
            BlockEntity fluidBE = level.getBlockEntity(fluidPos);
            var fluidCap = level.getCapability(
                    Capabilities.FluidHandler.BLOCK,
                    fluidPos,
                    level.getBlockState(fluidPos),
                    fluidBE,
                    null
            );

            if (fluidCap != null) {
                clientFluidAmount = fluidCap.getFluidInTank(0).getAmount();
                clientFluidCapacity = fluidCap.getTankCapacity(0);
                sendUpdatePacket(null);
            }
        }

    }

    private BlockPos getEnergyBlockPos() {
        // Energy block is at offset (0, 0, 2) from controller
        return worldPosition.offset(0, 0, 2);
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




    public int getClientEnergyStored() {
        return clientEnergyStored;
    }

    public int getClientEnergyMax() {
        return clientEnergyMax;
    }


    private void tryStartRecipe() {

        // 1. Check energy
        BlockPos energyPos = findSpecificBlock(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get());
        if (energyPos == null) return;

        IEnergyStorage storage = level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                energyPos,
                level.getBlockState(energyPos),
                level.getBlockEntity(energyPos),
                null
        );
        if (storage == null) return;

        // 2. Find item input block
        BlockPos inputPos = findSpecificBlock(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get());
        if (inputPos == null) return;

        BlockEntity be = level.getBlockEntity(inputPos);
        if (!(be instanceof EntityItemInputBlock input)) return;

        // 3. Loop through input slots
        for (int slot = 0; slot < input.inventory.getSlots(); slot++) {

            var stack = input.inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            // 4. Find recipe
            var recipe = RollingRecipeRegistry.findRecipe(stack);
            if (recipe == null) continue;

            // 5. Check energy requirement
            if (storage.getEnergyStored() < recipe.energyPerTick) return;

            // 6. Check fluid requirement
            BlockPos fluidPos = findSpecificBlock(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get());
            if (fluidPos == null) return;

            BlockEntity fluidBE = level.getBlockEntity(fluidPos);
            var fluidCap = level.getCapability(
                    Capabilities.FluidHandler.BLOCK,
                    fluidPos,
                    level.getBlockState(fluidPos),
                    fluidBE,
                    null
            );
            if (fluidCap == null) return;

            if (fluidCap.getFluidInTank(0).getAmount() < recipe.fluidRequired) return;

            // 7. Consume item + fluid
            input.inventory.extractItem(slot, 1, false);
            fluidCap.drain(recipe.fluidRequired, IFluidHandler.FluidAction.EXECUTE);

            // 8. Start recipe
            currentRecipe = recipe;
            recipeRunning = true;
            recipeProgress = 0;
            recipeMaxProgress = recipe.processingTime;

            renderData.running = true;
            sendUpdatePacket(null);
            return;
        }
    }


    private void finishRecipe() {
        recipeRunning = false;
        renderData.running = false;

        if (currentRecipe != null) {
            // Find output block
            BlockPos outPos = findSpecificBlock(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get());
            if (outPos != null) {
                BlockEntity be = level.getBlockEntity(outPos);
                if (be instanceof EntityItemOutputBlock out) {
                    out.inventory.insertItem(0, currentRecipe.output.copy(), false);
                }
            }
        }

        currentRecipe = null;
        sendUpdatePacket(null);
    }

    private BlockPos findSpecificBlock(Block blockType) {
        for (int dx = -4; dx <= 4; dx++)
            for (int dy = -2; dy <= 2; dy++)
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    if (level.getBlockState(p).getBlock() == blockType)
                        return p;
                }
        return null;
    }

    public void clientTick() {
        if (level == null || !level.isClientSide) return;

        if (renderData.running) {

            // Rollers spin
            renderData.rollerSpin = (renderData.rollerSpin + 4f) % 360f;

            // Press bounce
            renderData.pressOffset =
                    (float) Math.sin(level.getGameTime() * 0.08f) * 0.20f;

            // -------------------------------
            // INGOT FEED (moves INTO rollers)
            // -------------------------------
            renderData.ingotOffset += 0.03f;   // speed

            // When ingot reaches rollers, trigger plate start (only once)
            boolean ingotEnteredRollers = renderData.ingotOffset > 1.0f;

            if (ingotEnteredRollers && renderData.plateOffset == 0f) {
                // Start plate movement
                renderData.plateOffset = 0.001f;
            }

            // Reset ingot after full travel
            if (renderData.ingotOffset > 1.6f) {
                renderData.ingotOffset = 0f;
            }

            // -------------------------------
            // PLATE FEED (moves OUT of rollers)
            // -------------------------------
            if (renderData.plateOffset > 0f) {
                renderData.plateOffset += 0.03f;

                // Reset plate ONLY after it fully exits
                if (renderData.plateOffset > 1.0f) {
                    renderData.plateOffset = 0f;
                }
            }

        } else {
            // Reset everything when machine stops
            renderData.rollerSpin = 0f;
            renderData.pressOffset = 0f;
            renderData.ingotOffset = 0f;
            renderData.plateOffset = 0f;
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

    public int getClientFluidAmount() { return clientFluidAmount; }
    public int getClientFluidCapacity() { return clientFluidCapacity; }


    public void sendUpdatePacket(ServerPlayer specificPlayer) {
        if (level == null || level.isClientSide) return;

        CompoundTag tag = new CompoundTag();
        tag.putBoolean("running", renderData.running);
        tag.putFloat("rollerSpin", renderData.rollerSpin);
        tag.putFloat("pressOffset", renderData.pressOffset);
        tag.putInt("energyStored", clientEnergyStored);
        tag.putInt("energyMax", clientEnergyMax);
        tag.putInt("recipeProgress", recipeProgress);
        tag.putInt("recipeMaxProgress", recipeMaxProgress);
        tag.putInt("fluidAmount", clientFluidAmount);
        tag.putInt("fluidCapacity", clientFluidCapacity);

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
        if (tag.contains("energyStored")) clientEnergyStored = tag.getInt("energyStored");
        if (tag.contains("energyMax")) clientEnergyMax = tag.getInt("energyMax");
        if (tag.contains("recipeProgress")) recipeProgress = tag.getInt("recipeProgress");
        if (tag.contains("recipeMaxProgress")) recipeMaxProgress = tag.getInt("recipeMaxProgress");
        if (tag.contains("fluidAmount")) clientFluidAmount = tag.getInt("fluidAmount");
        if (tag.contains("fluidCapacity")) clientFluidCapacity = tag.getInt("fluidCapacity");

    }

    @Override
    public void readServer(CompoundTag tag, ServerPlayer sender) {}
}
