package com.raven.arsimplemachines.blockentity;

import ARLib.ARLibRegistry;
import ARLib.multiblockCore.EntityMultiblockMachineMaster;
import ARLib.multiblockCore.BlockMultiblockMaster;

import com.raven.arsimplemachines.recipe.electrolyzer.ElectrolyzerRecipeInput;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import com.raven.arsimplemachines.registry.ModBlocks;
import com.raven.arsimplemachines.registry.ModRecipeTypes;
import com.raven.arsimplemachines.recipe.electrolyzer.ElectrolyzerRecipe;

import com.raven.arsimplemachines.util.PatternScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import com.raven.arsimplemachines.menu.ElectrolyzerMenu;

import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElectrolyzerControllerBlockEntity extends EntityMultiblockMachineMaster
        implements INetworkTagReceiver, MenuProvider {

    public static class RenderData {
        public boolean running = false;
        public float animPhase = 0f;
    }

    public RenderData renderData = new RenderData();
    private ElectrolyzerRecipe currentRecipe;

    private boolean recipeRunning = false;
    private int recipeProgress = 0;
    private int recipeMaxProgress = 0;

    private int clientEnergyStored = 0;
    private int clientEnergyMax = 0;

    private int clientInputAmount = 0;
    private int clientInputCapacity = 0;

    private int clientOutputAAmount = 0;
    private int clientOutputACapacity = 0;

    private int clientOutputBAmount = 0;
    private int clientOutputBCapacity = 0;

    public ElectrolyzerControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTROLYZER_CONTROLLER.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Electrolyzer");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new ElectrolyzerMenu(windowId, inv, this.getBlockPos());
    }

    // -------------------------------------------------------------------------
    // STRUCTURE
    // -------------------------------------------------------------------------
    @Override
    public Object[][][] getStructure() {
        return new Object[][][]{
                {
                        { null, null, null },
                        { 'O', 'M', 'O' }
                },
                {
                        { 'E', 'C', 'E' },
                        { 'S', 'I', 'S' }
                }
        };
    }

    public static final Map<Character, List<Block>> MAPPING = Map.of(
            'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
            'S', List.of(ARLibRegistry.BLOCK_STRUCTURE.get()),
            'I', List.of(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get()),
            'O', List.of(ARLibRegistry.BLOCK_FLUID_OUTPUT_BLOCK.get()),
            'X', List.of(ARLibRegistry.BLOCK_FLUID_OUTPUT_BLOCK.get()),
            'M', List.of(ARLibRegistry.BLOCK_MOTOR.get()),
            'C', List.of(ModBlocks.ELECTROLYZER_CONTROLLER.get())
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

        return new Vec3i(1, 1, 0);
    }

    // -------------------------------------------------------------------------
    // SCANNER SYSTEM (Unified)
    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    // ENERGY + FLUID LOOKUP (Scanner-based)
    // -------------------------------------------------------------------------
    private IEnergyStorage getEnergyStorage() {
        BlockPos pos = findSpecificBlock(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get());
        if (pos == null) return null;

        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return null;

        return level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                pos,
                level.getBlockState(pos),
                be,
                null
        );
    }

    private IFluidHandler getInputTank() {
        BlockPos pos = findSpecificBlock(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get());
        if (pos == null) return null;

        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return null;

        return level.getCapability(
                Capabilities.FluidHandler.BLOCK,
                pos,
                level.getBlockState(pos),
                be,
                null
        );
    }

    private IFluidHandler getOutputATank() {
        List<BlockPos> outputs = findAllBlocks(ARLibRegistry.BLOCK_FLUID_OUTPUT_BLOCK.get());
        if (outputs.size() < 1) return null;

        BlockPos pos = outputs.get(0);
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return null;

        return level.getCapability(
                Capabilities.FluidHandler.BLOCK,
                pos,
                level.getBlockState(pos),
                be,
                null
        );
    }

    private IFluidHandler getOutputBTank() {
        List<BlockPos> outputs = findAllBlocks(ARLibRegistry.BLOCK_FLUID_OUTPUT_BLOCK.get());
        if (outputs.size() < 2) return null;

        BlockPos pos = outputs.get(1);
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return null;

        return level.getCapability(
                Capabilities.FluidHandler.BLOCK,
                pos,
                level.getBlockState(pos),
                be,
                null
        );
    }

    // -------------------------------------------------------------------------
    // MACHINE LOGIC
    // -------------------------------------------------------------------------
    @Override
    public void onStructureComplete() {
        renderData.running = false;
        sendUpdatePacket(null);
    }

    @Override
    public void onStructureInvalid() {
        recipeRunning = false;
        renderData.running = false;
        currentRecipe = null;
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

        boolean formed = getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED);

        if (!formed) {
            recipeRunning = false;
            currentRecipe = null;
            renderData.running = false;

            updateClientFluidStats();
            sendUpdatePacket(null);
            return;
        }

        IEnergyStorage storage = getEnergyStorage();
        if (storage != null) {
            clientEnergyStored = storage.getEnergyStored();
            clientEnergyMax = storage.getMaxEnergyStored();
        }

        if (!recipeRunning) {
            tryStartRecipe();
        }

        if (recipeRunning && currentRecipe != null && storage != null) {
            storage.extractEnergy(currentRecipe.getEnergyPerTick(), false);
            recipeProgress++;

            if (recipeProgress >= recipeMaxProgress) {
                finishRecipe();
            }
        }

        updateClientFluidStats();
        sendUpdatePacket(null);
    }

    private void updateClientFluidStats() {
        IFluidHandler input = getInputTank();
        IFluidHandler outA = getOutputATank();
        IFluidHandler outB = getOutputBTank();

        clientInputAmount = 0;
        clientInputCapacity = 0;

        clientOutputAAmount = 0;
        clientOutputACapacity = 0;

        clientOutputBAmount = 0;
        clientOutputBCapacity = 0;

        if (input != null) {
            var stack = input.getFluidInTank(0);
            clientInputAmount = stack.getAmount();
            clientInputCapacity = input.getTankCapacity(0);
        }

        if (outA != null) {
            var stack = outA.getFluidInTank(0);
            clientOutputAAmount = stack.getAmount();
            clientOutputACapacity = outA.getTankCapacity(0);
        }

        if (outB != null) {
            var stack = outB.getFluidInTank(0);
            clientOutputBAmount = stack.getAmount();
            clientOutputBCapacity = outB.getTankCapacity(0);
        }
    }

    private void tryStartRecipe() {
        IEnergyStorage storage = getEnergyStorage();
        if (storage == null) return;

        IFluidHandler input = getInputTank();
        IFluidHandler outA = getOutputATank();
        IFluidHandler outB = getOutputBTank();

        if (input == null || outA == null || outB == null) return;

        var inputStack = input.getFluidInTank(0);

        var allRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.ELECTROLYZER_TYPE.get());

        ElectrolyzerRecipe recipe = null;

        for (var holder : allRecipes) {
            var r = holder.value();
            if (r.matches(new ElectrolyzerRecipeInput(inputStack), level)) {
                recipe = r;
                break;
            }
        }

        if (recipe == null) {
            recipeRunning = false;
            renderData.running = false;
            currentRecipe = null;
            return;
        }

        if (storage.getEnergyStored() < recipe.getEnergyPerTick()) {
            return;
        }

        if (!recipe.canConsume(inputStack)) {
            return;
        }

        recipe.consumeInputs(input);

        currentRecipe = recipe;
        recipeRunning = true;
        recipeProgress = 0;
        recipeMaxProgress = recipe.getProcessingTime();
        renderData.running = true;
    }

    private void finishRecipe() {
        recipeRunning = false;
        renderData.running = false;

        if (currentRecipe != null) {
            IFluidHandler outA = getOutputATank();
            IFluidHandler outB = getOutputBTank();

            if (outA != null) {
                outA.fill(currentRecipe.getOutputA(), IFluidHandler.FluidAction.EXECUTE);
            }
            if (outB != null) {
                outB.fill(currentRecipe.getOutputB(), IFluidHandler.FluidAction.EXECUTE);
            }
        }

        currentRecipe = null;
        sendUpdatePacket(null);
    }

    // -------------------------------------------------------------------------
    // CLIENT
    // -------------------------------------------------------------------------

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
            renderData.animPhase = (renderData.animPhase + 0.05f) % (float) (Math.PI * 2);
        } else {
            renderData.animPhase = 0f;
        }
    }

    // -------------------------------------------------------------------------
    // NETWORK
    // -------------------------------------------------------------------------
    public int getRecipeProgress() { return recipeProgress; }
    public int getRecipeMaxProgress() { return recipeMaxProgress; }

    public int getClientEnergyStored() { return clientEnergyStored; }
    public int getClientEnergyMax() { return clientEnergyMax; }

    public int getClientInputAmount() { return clientInputAmount; }
    public int getClientInputCapacity() { return clientInputCapacity; }

    public int getClientOutputAAmount() { return clientOutputAAmount; }
    public int getClientOutputACapacity() { return clientOutputACapacity; }

    public int getClientOutputBAmount() { return clientOutputBAmount; }
    public int getClientOutputBCapacity() { return clientOutputBCapacity; }

    public void sendUpdatePacket(ServerPlayer specificPlayer) {
        if (level == null || level.isClientSide) return;

        CompoundTag tag = new CompoundTag();

        tag.putBoolean("running", renderData.running);
        tag.putFloat("animPhase", renderData.animPhase);

        tag.putInt("energyStored", clientEnergyStored);
        tag.putInt("energyMax", clientEnergyMax);

        tag.putInt("recipeProgress", recipeProgress);
        tag.putInt("recipeMaxProgress", recipeMaxProgress);

        tag.putInt("inputAmount", clientInputAmount);
        tag.putInt("inputCapacity", clientInputCapacity);

        tag.putInt("outputAAmount", clientOutputAAmount);
        tag.putInt("outputACapacity", clientOutputACapacity);

        tag.putInt("outputBAmount", clientOutputBAmount);
        tag.putInt("outputBCapacity", clientOutputBCapacity);

        PacketBlockEntity packet = PacketBlockEntity.getBlockEntityPacket(this, tag);

        if (specificPlayer != null)
            PacketDistributor.sendToPlayer(specificPlayer, packet);
        else
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(worldPosition), packet);
    }

    @Override
    public void readClient(CompoundTag tag) {

        if (tag.contains("running")) renderData.running = tag.getBoolean("running");
        if (tag.contains("animPhase")) renderData.animPhase = tag.getFloat("animPhase");

        if (tag.contains("energyStored")) clientEnergyStored = tag.getInt("energyStored");
        if (tag.contains("energyMax")) clientEnergyMax = tag.getInt("energyMax");

        if (tag.contains("recipeProgress")) recipeProgress = tag.getInt("recipeProgress");
        if (tag.contains("recipeMaxProgress")) recipeMaxProgress = tag.getInt("recipeMaxProgress");

        if (tag.contains("inputAmount")) clientInputAmount = tag.getInt("inputAmount");
        if (tag.contains("inputCapacity")) clientInputCapacity = tag.getInt("inputCapacity");

        if (tag.contains("outputAAmount")) clientOutputAAmount = tag.getInt("outputAAmount");
        if (tag.contains("outputACapacity")) clientOutputACapacity = tag.getInt("outputACapacity");

        if (tag.contains("outputBAmount")) clientOutputBAmount = tag.getInt("outputBAmount");
        if (tag.contains("outputBCapacity")) clientOutputBCapacity = tag.getInt("outputBCapacity");
    }

    @Override
    public void readServer(CompoundTag tag, ServerPlayer sender) {}
}
