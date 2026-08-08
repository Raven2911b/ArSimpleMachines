package com.raven.arsimplemachines.blockentity;

import ARLib.ARLibRegistry;
import ARLib.multiblockCore.EntityMultiblockMachineMaster;
import ARLib.multiblockCore.BlockMultiblockMaster;

import com.raven.arsimplemachines.recipe.chemical.ChemicalReactorRecipeInput;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import com.raven.arsimplemachines.registry.ModBlocks;
import com.raven.arsimplemachines.registry.ModRecipeTypes;
import com.raven.arsimplemachines.recipe.chemical.ChemicalReactorRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import com.raven.arsimplemachines.menu.ChemicalReactorMenu;

import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChemicalReactorControllerBlockEntity extends EntityMultiblockMachineMaster
        implements INetworkTagReceiver, MenuProvider {

    public static class RenderData {
        public boolean running = false;
        public float animPhase = 0f;
    }

    public RenderData renderData = new RenderData();
    private ChemicalReactorRecipe currentRecipe;

    private boolean recipeRunning = false;
    private int recipeProgress = 0;
    private int recipeMaxProgress = 0;

    private int clientEnergyStoredA = 0;
    private int clientEnergyStoredB = 0;
    private int clientEnergyMaxA = 0;
    private int clientEnergyMaxB = 0;

    private int clientHydrogenAmount = 0;
    private int clientHydrogenCapacity = 0;
    private int clientOxygenAmount = 0;
    private int clientOxygenCapacity = 0;
    private int clientOutputAmount = 0;
    private int clientOutputCapacity = 0;

    public ChemicalReactorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEMICAL_REACTOR_CONTROLLER.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Chemical Reactor");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new ChemicalReactorMenu(windowId, inv, this.getBlockPos());
    }

    @Override
    public Object[][][] getStructure() {
        return new Object[][][]{
                {
                        { null,'C',  null },
                        { 'O', 'S', 'H'}
                },
                {
                        { 'E', 'M', 'E' },
                        { 'S', 'X', 'S'}
                }
        };
    }

    public static final Map<Character, List<Block>> MAPPING = Map.of(
            'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
            'S', List.of(ARLibRegistry.BLOCK_STRUCTURE.get()),
            'H', List.of(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get()),
            'O', List.of(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get()),
            'X', List.of(ARLibRegistry.BLOCK_FLUID_OUTPUT_BLOCK.get()),
            'M', List.of(ARLibRegistry.BLOCK_MOTOR.get()),
            'C', List.of(ModBlocks.CHEMICAL_REACTOR_CONTROLLER.get())
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

    // ---------------------------------------------------------------------
    // FIND ALL FLUID INPUT BLOCKS (order-independent)
    // ---------------------------------------------------------------------
    private List<IFluidHandler> getAllFluidInputs() {
        List<IFluidHandler> list = new ArrayList<>();
        for (int dx = -4; dx <= 4; dx++)
            for (int dy = -2; dy <= 2; dy++)
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    Block block = level.getBlockState(p).getBlock();
                    if (block == ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get()) {
                        BlockEntity be = level.getBlockEntity(p);
                        if (be != null) {
                            IFluidHandler cap = level.getCapability(
                                    Capabilities.FluidHandler.BLOCK,
                                    p,
                                    level.getBlockState(p),
                                    be,
                                    null
                            );
                            if (cap != null) list.add(cap);
                        }
                    }
                }
        return list;
    }

    private IFluidHandler getOutputTank() {
        BlockPos pos = findSpecificBlock(ARLibRegistry.BLOCK_FLUID_OUTPUT_BLOCK.get());
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

    // ---------------------------------------------------------------------
    // ENERGY
    // ---------------------------------------------------------------------
    private IEnergyStorage getEnergyStorageA() {
        BlockPos pos = rotateOffset(-1, -1, 0);
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

    private IEnergyStorage getEnergyStorageB() {
        BlockPos pos = rotateOffset(+1, -1, 0);
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

    private BlockPos rotateOffset(int dx, int dy, int dz) {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

        return switch (facing) {
            case NORTH -> worldPosition.offset(dx, dy, dz);
            case SOUTH -> worldPosition.offset(-dx, dy, -dz);
            case EAST  -> worldPosition.offset(dz, dy, -dx);
            case WEST  -> worldPosition.offset(-dz, dy, dx);
            default    -> worldPosition.offset(dx, dy, dz);
        };
    }

    // ---------------------------------------------------------------------
    // MAIN TICK
    // ---------------------------------------------------------------------
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

        IEnergyStorage storageA = getEnergyStorageA();
        IEnergyStorage storageB = getEnergyStorageB();

        if (storageA != null && storageB != null) {
            clientEnergyStoredA = storageA.getEnergyStored();
            clientEnergyMaxA = storageA.getMaxEnergyStored();

            clientEnergyStoredB = storageB.getEnergyStored();
            clientEnergyMaxB = storageB.getMaxEnergyStored();
        }

        if (!recipeRunning) {
            tryStartRecipe();
        }

        if (recipeRunning && currentRecipe != null) {
            storageA.extractEnergy(currentRecipe.getEnergyPerTick(), false);
            storageB.extractEnergy(currentRecipe.getEnergyPerTick(), false);

            recipeProgress++;

            if (recipeProgress >= recipeMaxProgress) {
                finishRecipe();
            }
        }

        updateClientFluidStats();
        sendUpdatePacket(null);
    }

    // ---------------------------------------------------------------------
    // RECIPE START — ORDER-INDEPENDENT FLUID MATCHING
    // ---------------------------------------------------------------------
    private void tryStartRecipe() {

        IEnergyStorage storageA = getEnergyStorageA();
        IEnergyStorage storageB = getEnergyStorageB();

        if (storageA == null || storageB == null) return;

        List<IFluidHandler> inputs = getAllFluidInputs();
        if (inputs.isEmpty()) return;

        IFluidHandler output = getOutputTank();
        if (output == null) return;

        var allRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.CHEMICAL_REACTOR_TYPE.get());

        ChemicalReactorRecipe recipe = null;

        for (var holder : allRecipes) {
            var r = holder.value();

            FluidStack reqA = r.getFluidA();
            FluidStack reqB = r.getFluidB();

            boolean foundA = false;
            boolean foundB = false;

            for (IFluidHandler tank : inputs) {
                FluidStack fs = tank.getFluidInTank(0);
                if (fs.isEmpty()) continue;

                if (fs.getFluid() == reqA.getFluid() && fs.getAmount() >= reqA.getAmount())
                    foundA = true;

                if (fs.getFluid() == reqB.getFluid() && fs.getAmount() >= reqB.getAmount())
                    foundB = true;
            }

            if (foundA && foundB) {
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

        if (storageA.getEnergyStored() < recipe.getEnergyPerTick() ||
                storageB.getEnergyStored() < recipe.getEnergyPerTick()) {
            return;
        }

        consumeFluidFromAnyTank(recipe.getFluidA(), inputs);
        consumeFluidFromAnyTank(recipe.getFluidB(), inputs);

        currentRecipe = recipe;
        recipeRunning = true;
        recipeProgress = 0;
        recipeMaxProgress = recipe.getProcessingTime();
        renderData.running = true;
    }

    // ---------------------------------------------------------------------
    // CONSUME FLUIDS FROM ANY TANK
    // ---------------------------------------------------------------------
    private void consumeFluidFromAnyTank(FluidStack req, List<IFluidHandler> inputs) {
        int needed = req.getAmount();

        for (IFluidHandler tank : inputs) {
            FluidStack fs = tank.getFluidInTank(0);
            if (!fs.isEmpty() && fs.getFluid() == req.getFluid()) {
                int take = Math.min(fs.getAmount(), needed);
                tank.drain(new FluidStack(req.getFluid(), take), IFluidHandler.FluidAction.EXECUTE);
                needed -= take;
                if (needed <= 0) break;
            }
        }
    }

    // ---------------------------------------------------------------------
    // FINISH RECIPE
    // ---------------------------------------------------------------------
    private void finishRecipe() {
        recipeRunning = false;
        renderData.running = false;

        if (currentRecipe != null) {
            IFluidHandler output = getOutputTank();
            if (output != null) {
                output.fill(currentRecipe.getOutput(), IFluidHandler.FluidAction.EXECUTE);
            }
        }

        currentRecipe = null;
        sendUpdatePacket(null);
    }

    // ---------------------------------------------------------------------
    // CLIENT FLUID STATS
    // ---------------------------------------------------------------------
    private void updateClientFluidStats() {
        clientHydrogenAmount = 0;
        clientHydrogenCapacity = 0;
        clientOxygenAmount = 0;
        clientOxygenCapacity = 0;
        clientOutputAmount = 0;
        clientOutputCapacity = 0;

        List<IFluidHandler> inputs = getAllFluidInputs();

        for (IFluidHandler tank : inputs) {
            FluidStack fs = tank.getFluidInTank(0);
            if (fs.isEmpty()) continue;

            var key = BuiltInRegistries.FLUID.getKey(fs.getFluid());
            String type = key.getPath().toLowerCase();

            if (type.contains("hydrogen")) {
                clientHydrogenAmount = fs.getAmount();
                clientHydrogenCapacity = tank.getTankCapacity(0);
            }
            if (type.contains("oxygen")) {
                clientOxygenAmount = fs.getAmount();
                clientOxygenCapacity = tank.getTankCapacity(0);
            }
        }

        IFluidHandler output = getOutputTank();
        if (output != null) {
            FluidStack fs = output.getFluidInTank(0);
            clientOutputAmount = fs.getAmount();
            clientOutputCapacity = output.getTankCapacity(0);
        }
    }

    // ---------------------------------------------------------------------
    // UTILS
    // ---------------------------------------------------------------------
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
            renderData.animPhase = (renderData.animPhase + 0.05f) % (float) (Math.PI * 2);
        } else {
            renderData.animPhase = 0f;
        }
    }

    public int getRecipeProgress() { return recipeProgress; }
    public int getRecipeMaxProgress() { return recipeMaxProgress; }

    public int getClientEnergyStoredA() { return clientEnergyStoredA; }
    public int getClientEnergyMaxA() { return clientEnergyMaxA; }
    public int getClientEnergyStoredB() { return clientEnergyStoredB; }
    public int getClientEnergyMaxB() { return clientEnergyMaxB; }

    public int getClientHydrogenAmount() { return clientHydrogenAmount; }
    public int getClientHydrogenCapacity() { return clientHydrogenCapacity; }
    public int getClientOxygenAmount() { return clientOxygenAmount; }
    public int getClientOxygenCapacity() { return clientOxygenCapacity; }
    public int getClientOutputAmount() { return clientOutputAmount; }
    public int getClientOutputCapacity() { return clientOutputCapacity; }

    public void sendUpdatePacket(ServerPlayer specificPlayer) {
        if (level == null || level.isClientSide) return;

        CompoundTag tag = new CompoundTag();

        tag.putBoolean("running", renderData.running);
        tag.putFloat("animPhase", renderData.animPhase);

        tag.putInt("energyStoredA", clientEnergyStoredA);
        tag.putInt("energyMaxA", clientEnergyMaxA);
        tag.putInt("energyStoredB", clientEnergyStoredB);
        tag.putInt("energyMaxB", clientEnergyMaxB);

        tag.putInt("recipeProgress", recipeProgress);
        tag.putInt("recipeMaxProgress", recipeMaxProgress);

        tag.putInt("hydrogenAmount", clientHydrogenAmount);
        tag.putInt("hydrogenCapacity", clientHydrogenCapacity);
        tag.putInt("oxygenAmount", clientOxygenAmount);
        tag.putInt("oxygenCapacity", clientOxygenCapacity);
        tag.putInt("outputAmount", clientOutputAmount);
        tag.putInt("outputCapacity", clientOutputCapacity);

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

        if (tag.contains("energyStoredA")) clientEnergyStoredA = tag.getInt("energyStoredA");
        if (tag.contains("energyMaxA")) clientEnergyMaxA = tag.getInt("energyMaxA");
        if (tag.contains("energyStoredB")) clientEnergyStoredB = tag.getInt("energyStoredB");
        if (tag.contains("energyMaxB")) clientEnergyMaxB = tag.getInt("energyMaxB");

        if (tag.contains("recipeProgress")) recipeProgress = tag.getInt("recipeProgress");
        if (tag.contains("recipeMaxProgress")) recipeMaxProgress = tag.getInt("recipeMaxProgress");

        if (tag.contains("hydrogenAmount")) clientHydrogenAmount = tag.getInt("hydrogenAmount");
        if (tag.contains("hydrogenCapacity")) clientHydrogenCapacity = tag.getInt("hydrogenCapacity");
        if (tag.contains("oxygenAmount")) clientOxygenAmount = tag.getInt("oxygenAmount");
        if (tag.contains("oxygenCapacity")) clientOxygenCapacity = tag.getInt("oxygenCapacity");
        if (tag.contains("outputAmount")) clientOutputAmount = tag.getInt("outputAmount");
        if (tag.contains("outputCapacity")) clientOutputCapacity = tag.getInt("outputCapacity");
    }

    @Override
    public void readServer(CompoundTag tag, ServerPlayer sender) {}
}
