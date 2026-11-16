package com.raven.arsimplemachines.blockentity;

import ARLib.ARLibRegistry;
import ARLib.multiblockCore.BlockMultiblockMaster;
import ARLib.multiblockCore.EntityMultiblockMachineMaster;
import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import com.raven.arsimplemachines.menu.FluidConverterMenu;
import com.raven.arsimplemachines.recipe.fluidconverter.FluidConverterRecipe;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import com.raven.arsimplemachines.registry.ModBlocks;
import com.raven.arsimplemachines.registry.ModRecipeTypes;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FluidConverterBlockEntity extends EntityMultiblockMachineMaster implements INetworkTagReceiver, MenuProvider {

    public static class RenderData {
        public boolean running = false;
    }

    public RenderData renderData = new RenderData();
    public FluidConverterRecipe currentRecipe;

    public boolean recipeRunning = false;
    private int recipeProgress = 0;
    private int recipeMaxProgress = 0;

    private int clientEnergyStored = 0;
    private int clientEnergyMax = 0;

    private String clientInputFluidName = "";
    private String clientOutputFluidName = "";
    private int clientInputFluidStored = 0;
    private int clientInputFluidCapacity = 0;
    private int clientOutputFluidStored = 0;
    private int clientOutputFluidCapacity = 0;

    public FluidConverterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_CONVERTER_CONTROLLER.get(), pos, state);
    }

    public int getClientInputFluidStored() {
        return clientInputFluidStored;
    }

    public int getClientInputFluidCapacity() {
        return clientInputFluidCapacity;
    }

    public int getClientOutputFluidStored() {
        return clientOutputFluidStored;
    }

    public int getClientOutputFluidCapacity() {
        return clientOutputFluidCapacity;
    }

    public String getClientInputFluidName() {
        return clientInputFluidName;
    }

    public String getClientOutputFluidName() {
        return clientOutputFluidName;
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

    @Override
    public Component getDisplayName() {
        return Component.literal("Fluid Converter");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new FluidConverterMenu(windowId, inv, this.getBlockPos());
    }

    @Override
    public Object[][][] getStructure() {
        return new Object[][][] {
                { { null, 'C', null } },
                { { 'I', 'E', 'O' } }
        };
    }

    public static final Map<Character, List<Block>> MAPPING = Map.of(
            'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
            'I', List.of(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get()),
            'O', List.of(ARLibRegistry.BLOCK_FLUID_OUTPUT_BLOCK.get()),
            'C', List.of(ModBlocks.FLUID_CONVERTER_CONTROLLER.get())
    );

    @Override
    public HashMap<Character, List<Block>> getCharMapping() {
        return new HashMap<>(MAPPING);
    }

    @Override
    public Vec3i getControllerOffset(Object[][][] structure) {
        return new Vec3i(1, 0, 0);
    }

    private Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    private int minX() { return -1; }
    private int maxX() { return 1; }
    private int minY() { return 0; }
    private int maxY() { return 1; }
    private int minZ() { return -1; }
    private int maxZ() { return 1; }

    private BlockPos findSpecificBlock(Block blockType) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    if (level.getBlockState(p).getBlock() == blockType) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    private List<BlockPos> findAllBlocks(Block blockType) {
        List<BlockPos> list = new ArrayList<>();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    if (level.getBlockState(p).getBlock() == blockType) {
                        list.add(p);
                    }
                }
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

    private List<IFluidHandler> findAllFluidInputs() {
        List<IFluidHandler> list = new ArrayList<>();
        for (BlockPos pos : findAllBlocks(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null) continue;

            IFluidHandler cap = level.getCapability(
                    Capabilities.FluidHandler.BLOCK,
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

    private List<IFluidHandler> findAllFluidOutputs() {
        List<IFluidHandler> list = new ArrayList<>();
        for (BlockPos pos : findAllBlocks(ARLibRegistry.BLOCK_FLUID_OUTPUT_BLOCK.get())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null) continue;

            IFluidHandler cap = level.getCapability(
                    Capabilities.FluidHandler.BLOCK,
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

    private boolean canOutputRecipe(FluidConverterRecipe recipe) {
        if (recipe == null) return false;

        List<IFluidHandler> outputTanks = findAllFluidOutputs();
        if (outputTanks.isEmpty()) return false;

        for (FluidStack out : recipe.getFluidOutputs()) {
            if (out.isEmpty()) continue;

            FluidStack remaining = out.copy();
            for (IFluidHandler tank : outputTanks) {
                if (remaining.isEmpty()) break;
                int accepted = tank.fill(remaining, IFluidHandler.FluidAction.SIMULATE);
                remaining.shrink(accepted);
            }

            if (!remaining.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private boolean hasRequiredFluids(FluidConverterRecipe recipe, List<IFluidHandler> handlers) {
        for (FluidStack req : recipe.getFluidInputs()) {
            int needed = req.getAmount();
            int found = 0;

            for (IFluidHandler handler : handlers) {
                for (int slot = 0; slot < handler.getTanks(); slot++) {
                    FluidStack tank = handler.getFluidInTank(slot);
                    if (tank.isEmpty()) continue;
                    if (tank.getFluid() == req.getFluid()) {
                        found += tank.getAmount();
                        if (found >= needed) break;
                    }
                }
                if (found >= needed) break;
            }

            if (found < needed) {
                return false;
            }
        }

        for (FluidConverterRecipe.FluidTagInput tagReq : recipe.getFluidInputTags()) {
            int needed = tagReq.amount();
            int found = 0;

            TagKey<net.minecraft.world.level.material.Fluid> key =
                    TagKey.create(BuiltInRegistries.FLUID.key(), tagReq.tag());

            for (IFluidHandler handler : handlers) {
                for (int slot = 0; slot < handler.getTanks(); slot++) {
                    FluidStack tank = handler.getFluidInTank(slot);
                    if (tank.isEmpty()) continue;
                    if (tank.is(key)) {
                        found += tank.getAmount();
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

    private void consumeRequiredFluids(FluidConverterRecipe recipe, List<IFluidHandler> handlers) {
        for (FluidStack req : recipe.getFluidInputs()) {
            int remaining = req.getAmount();

            for (IFluidHandler handler : handlers) {
                for (int slot = 0; slot < handler.getTanks() && remaining > 0; slot++) {
                    FluidStack tank = handler.getFluidInTank(slot);
                    if (tank.isEmpty()) continue;
                    if (tank.getFluid() != req.getFluid()) continue;

                    int take = Math.min(tank.getAmount(), remaining);
                    handler.drain(new FluidStack(req.getFluid(), take), IFluidHandler.FluidAction.EXECUTE);
                    remaining -= take;
                }
            }
        }

        for (FluidConverterRecipe.FluidTagInput tagReq : recipe.getFluidInputTags()) {
            int remaining = tagReq.amount();

            TagKey<net.minecraft.world.level.material.Fluid> key =
                    TagKey.create(BuiltInRegistries.FLUID.key(), tagReq.tag());

            for (IFluidHandler handler : handlers) {
                for (int slot = 0; slot < handler.getTanks() && remaining > 0; slot++) {
                    FluidStack tank = handler.getFluidInTank(slot);
                    if (tank.isEmpty()) continue;
                    if (!tank.is(key)) continue;

                    int take = Math.min(tank.getAmount(), remaining);
                    handler.drain(new FluidStack(tank.getFluid(), take), IFluidHandler.FluidAction.EXECUTE);
                    remaining -= take;
                }
            }
        }
    }

    private List<FluidStack> getVisibleFluidInputs(List<IFluidHandler> handlers) {
        List<FluidStack> result = new ArrayList<>();
        for (IFluidHandler handler : handlers) {
            for (int slot = 0; slot < handler.getTanks(); slot++) {
                FluidStack fs = handler.getFluidInTank(slot);
                if (!fs.isEmpty()) {
                    result.add(fs.copy());
                }
            }
        }
        return result;
    }

    private void refreshFluidDisplayTotals() {
        List<IFluidHandler> inputTanks = findAllFluidInputs();
        List<IFluidHandler> outputTanks = findAllFluidOutputs();

        clientInputFluidStored = 0;
        clientInputFluidCapacity = 0;
        clientInputFluidName = "";

        for (IFluidHandler handler : inputTanks) {
            for (int slot = 0; slot < handler.getTanks(); slot++) {
                FluidStack tank = handler.getFluidInTank(slot);
                if (!tank.isEmpty()) {
                    clientInputFluidStored += tank.getAmount();
                    if (clientInputFluidName.isEmpty()) {
                        clientInputFluidName = BuiltInRegistries.FLUID.getKey(tank.getFluid()).toString();
                    }
                }
                clientInputFluidCapacity += handler.getTankCapacity(slot);
            }
        }

        clientOutputFluidStored = 0;
        clientOutputFluidCapacity = 0;
        clientOutputFluidName = "";

        for (IFluidHandler handler : outputTanks) {
            for (int slot = 0; slot < handler.getTanks(); slot++) {
                FluidStack tank = handler.getFluidInTank(slot);
                if (!tank.isEmpty()) {
                    clientOutputFluidStored += tank.getAmount();
                    if (clientOutputFluidName.isEmpty()) {
                        clientOutputFluidName = BuiltInRegistries.FLUID.getKey(tank.getFluid()).toString();
                    }
                }
                clientOutputFluidCapacity += handler.getTankCapacity(slot);
            }
        }

        if (clientInputFluidCapacity > 0) {
            clientInputFluidStored = Math.min(clientInputFluidStored, clientInputFluidCapacity);
        }
        if (clientOutputFluidCapacity > 0) {
            clientOutputFluidStored = Math.min(clientOutputFluidStored, clientOutputFluidCapacity);
        }
    }

    private void tryStartRecipe() {
        if (level == null) return;

        List<IFluidHandler> inputTanks = findAllFluidInputs();
        IEnergyStorage storage = getEnergyStorage();

        if (inputTanks.isEmpty()) return;
        if (storage == null) return;

        FluidConverterRecipe recipe = null;

        for (var holder : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.FLUID_CONVERTER_TYPE)) {
            FluidConverterRecipe candidate = holder.value();
            if (candidate == null) continue;

            boolean directMatch = false;
            for (FluidStack in : getVisibleFluidInputs(inputTanks)) {
                if (candidate.matchesFluidInput(in)) {
                    directMatch = true;
                    break;
                }
            }

            if (directMatch && hasRequiredFluids(candidate, inputTanks)) {
                recipe = candidate;
                break;
            }
        }

        if (recipe == null) return;
        if (storage.getEnergyStored() < recipe.getEnergyPerTick()) return;
        if (!canOutputRecipe(recipe)) return;

        currentRecipe = recipe;
        recipeRunning = true;
        recipeProgress = 0;
        recipeMaxProgress = recipe.getProcessingTime();
        renderData.running = true;

        consumeRequiredFluids(recipe, inputTanks);
        sendUpdatePacket(null);
    }

    private void finishRecipe() {
        FluidConverterRecipe finished = currentRecipe;

        recipeRunning = false;
        renderData.running = false;

        if (finished != null) {
            List<IFluidHandler> outputTanks = findAllFluidOutputs();
            if (outputTanks.isEmpty()) {
                currentRecipe = null;
                recipeRunning = false;
                recipeProgress = 0;
                recipeMaxProgress = 0;
                renderData.running = false;
                sendUpdatePacket(null);
                return;
            }

            for (FluidStack out : finished.getFluidOutputs()) {
                if (out.isEmpty()) continue;

                FluidStack remaining = out.copy();
                for (IFluidHandler tank : outputTanks) {
                    if (remaining.isEmpty()) break;
                    int accepted = tank.fill(remaining, IFluidHandler.FluidAction.EXECUTE);
                    remaining.shrink(accepted);
                }
            }
        }

        currentRecipe = null;
        recipeProgress = 0;
        recipeMaxProgress = 0;
        sendUpdatePacket(null);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (!getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) {
            resetMachineState();
            return;
        }

        refreshFluidDisplayTotals();

        IEnergyStorage storage = getEnergyStorage();
        if (storage != null) {
            clientEnergyStored = storage.getEnergyStored();
            clientEnergyMax = storage.getMaxEnergyStored();
        } else {
            clientEnergyStored = 0;
            clientEnergyMax = 0;
        }

        sendUpdatePacket(null);

        if (!recipeRunning) {
            if (currentRecipe == null) {
                tryStartRecipe();
            }
            if (!recipeRunning || currentRecipe == null) {
                sendUpdatePacket(null);
                return;
            }
        }

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

        if (recipeProgress >= recipeMaxProgress) {
            finishRecipe();
            return;
        }

        sendUpdatePacket(null);
    }

    private void resetMachineState() {
        recipeRunning = false;
        currentRecipe = null;
        recipeProgress = 0;
        recipeMaxProgress = 0;
        renderData.running = false;

        clientEnergyStored = 0;
        clientEnergyMax = 0;

        clientInputFluidStored = 0;
        clientInputFluidCapacity = 0;
        clientOutputFluidStored = 0;
        clientOutputFluidCapacity = 0;

        clientInputFluidName = "";
        clientOutputFluidName = "";

        refreshFluidDisplayTotals();
        sendUpdatePacket(null);
    }

    public void clientTick() {
        if (level == null || !level.isClientSide) return;
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

    public void sendUpdatePacket(ServerPlayer specificPlayer) {
        if (level == null || level.isClientSide) return;

        CompoundTag tag = new CompoundTag();
        tag.putBoolean("running", renderData.running);
        tag.putBoolean("recipeRunning", recipeRunning);
        tag.putInt("energyStored", clientEnergyStored);
        tag.putInt("energyMax", clientEnergyMax);
        tag.putInt("recipeProgress", recipeProgress);
        tag.putInt("recipeMaxProgress", recipeMaxProgress);

        tag.putInt("inputFluidStored", clientInputFluidStored);
        tag.putInt("inputFluidCapacity", clientInputFluidCapacity);
        tag.putInt("outputFluidStored", clientOutputFluidStored);
        tag.putInt("outputFluidCapacity", clientOutputFluidCapacity);

        tag.putString("inputFluidName", clientInputFluidName);
        tag.putString("outputFluidName", clientOutputFluidName);

        PacketBlockEntity packet = PacketBlockEntity.getBlockEntityPacket(this, tag);

        if (specificPlayer != null) {
            PacketDistributor.sendToPlayer(specificPlayer, packet);
        } else {
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(worldPosition), packet);
        }
    }

    @Override
    public void readClient(CompoundTag tag) {
        if (tag.contains("running")) renderData.running = tag.getBoolean("running");
        if (tag.contains("recipeRunning")) recipeRunning = tag.getBoolean("recipeRunning");
        if (tag.contains("energyStored")) clientEnergyStored = tag.getInt("energyStored");
        if (tag.contains("energyMax")) clientEnergyMax = tag.getInt("energyMax");
        if (tag.contains("recipeProgress")) recipeProgress = tag.getInt("recipeProgress");
        if (tag.contains("recipeMaxProgress")) recipeMaxProgress = tag.getInt("recipeMaxProgress");

        if (tag.contains("inputFluidStored")) clientInputFluidStored = tag.getInt("inputFluidStored");
        if (tag.contains("inputFluidCapacity")) clientInputFluidCapacity = tag.getInt("inputFluidCapacity");
        if (tag.contains("outputFluidStored")) clientOutputFluidStored = tag.getInt("outputFluidStored");
        if (tag.contains("outputFluidCapacity")) clientOutputFluidCapacity = tag.getInt("outputFluidCapacity");

        if (clientInputFluidCapacity > 0) {
            clientInputFluidStored = Math.min(clientInputFluidStored, clientInputFluidCapacity);
        }
        if (clientOutputFluidCapacity > 0) {
            clientOutputFluidStored = Math.min(clientOutputFluidStored, clientOutputFluidCapacity);
        }

        if (tag.contains("inputFluidName")) clientInputFluidName = tag.getString("inputFluidName");
        if (tag.contains("outputFluidName")) clientOutputFluidName = tag.getString("outputFluidName");
    }

    @Override
    public void readServer(CompoundTag tag, ServerPlayer sender) {
    }
}