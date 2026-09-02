package com.raven.arsimplemachines.blockentity;

import ARLib.ARLibRegistry;
import ARLib.multiblockCore.EntityMultiblockMachineMaster;
import ARLib.multiblockCore.BlockMultiblockMaster;

import com.raven.arsimplemachines.menu.ChemicalReactorMenu;
import com.raven.arsimplemachines.recipe.MachineRecipe;
import com.raven.arsimplemachines.recipe.MachineRecipeInput;
import com.raven.arsimplemachines.recipe.MachineRecipeMatcher;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import com.raven.arsimplemachines.registry.ModBlocks;
import com.raven.arsimplemachines.registry.ModRecipeTypes;
import com.raven.arsimplemachines.recipe.chemical.ChemicalReactorRecipe;

import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
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

    // Auto-detected fluid names and amounts for the two input tanks
    private String clientInputAName = "";
    private int clientInputAAmount = 0;
    private int clientInputACapacity = 0;

    private String clientInputBName = "";
    private int clientInputBAmount = 0;
    private int clientInputBCapacity = 0;

    // Output tank stats
    private String clientOutputName = "";
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

    private int minY() { return -1; }
    private int maxY() { return 0; }

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

    @Override
    public Object[][][] getStructure() {
        return new Object[][][] {
                {
                        { null, 'C', null },
                        { 'O', 'S', 'H' }
                },
                {
                        { 'E', 'M', 'E' },
                        { 'S', 'X', 'S' }
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
    // FLUID INPUTS / OUTPUTS
    // ---------------------------------------------------------------------

    private List<IFluidHandler> getAllFluidInputs() {
        List<IFluidHandler> list = new ArrayList<>();
        for (int dx = minX(); dx <= maxX(); dx++)
            for (int dy = minY(); dy <= maxY(); dy++)
                for (int dz = minZ(); dz <= maxZ(); dz++) {
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
    // TAG-AWARE FLUID MATCHING (SPECIAL-CASE FOR CHEMICAL REACTOR)
    // ---------------------------------------------------------------------

    private boolean matchesFluidWithTags(
            FluidStack direct,
            List<ChemicalReactorRecipe.FluidTagInput> tags,
            FluidStack inTank
    ) {
        if (inTank.isEmpty()) return false;

        // Direct match
        if (direct != null && !direct.isEmpty()
                && inTank.getFluid() == direct.getFluid()
                && inTank.getAmount() >= direct.getAmount()) {
            return true;
        }

        // Tag match
        for (var tag : tags) {
            TagKey<Fluid> key = TagKey.create(BuiltInRegistries.FLUID.key(), tag.tag());
            if (inTank.is(key) && inTank.getAmount() >= tag.amount()) {
                return true;
            }
        }

        return false;
    }

    private void consumeFluidWithTags(
            FluidStack direct,
            List<ChemicalReactorRecipe.FluidTagInput> tags,
            List<IFluidHandler> tanks
    ) {
        int needed = 0;

        if (direct != null && !direct.isEmpty()) {
            needed = direct.getAmount();
        } else if (!tags.isEmpty()) {
            needed = tags.get(0).amount();
        }

        if (needed <= 0) return;

        for (IFluidHandler tank : tanks) {
            FluidStack fs = tank.getFluidInTank(0);
            if (fs.isEmpty()) continue;

            boolean matches = false;

            if (direct != null && !direct.isEmpty() && fs.getFluid() == direct.getFluid()) {
                matches = true;
            } else {
                for (var tag : tags) {
                    TagKey<Fluid> key = TagKey.create(BuiltInRegistries.FLUID.key(), tag.tag());
                    if (fs.is(key)) {
                        matches = true;
                        break;
                    }
                }
            }

            if (matches) {
                int take = Math.min(fs.getAmount(), needed);
                tank.drain(new FluidStack(fs.getFluid(), take), IFluidHandler.FluidAction.EXECUTE);
                needed -= take;
                if (needed <= 0) break;
            }
        }
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
    // RECIPE START — ORDER-INDEPENDENT FLUID MATCHING (2 INPUT TANKS)
    // ---------------------------------------------------------------------

    private void tryStartRecipe() {
        if (level == null) return;

        IEnergyStorage storageA = getEnergyStorageA();
        IEnergyStorage storageB = getEnergyStorageB();

        if (storageA == null || storageB == null) return;

        List<IFluidHandler> inputs = getAllFluidInputs();
        if (inputs.size() < 2) return;

        IFluidHandler output = getOutputTank();
        if (output == null) return;

        // Build unified MachineRecipeInput with exactly two fluid inputs
        MachineRecipeInput recipeInput = new MachineRecipeInput();

        FluidStack tank0 = inputs.get(0).getFluidInTank(0);
        FluidStack tank1 = inputs.get(1).getFluidInTank(0);

        if (!tank0.isEmpty()) {
            recipeInput.addFluid(tank0);
        }
        if (!tank1.isEmpty()) {
            recipeInput.addFluid(tank1);
        }

        // Use MachineRecipeMatcher to find a candidate recipe
        MachineRecipe match = MachineRecipeMatcher.findMatch(
                level,
                ModRecipeTypes.CHEMICAL_REACTOR_TYPE.get(),
                recipeInput
        );

        if (!(match instanceof ChemicalReactorRecipe recipe)) {
            recipeRunning = false;
            renderData.running = false;
            currentRecipe = null;
            return;
        }

        // Energy check
        if (storageA.getEnergyStored() < recipe.getEnergyPerTick()
                || storageB.getEnergyStored() < recipe.getEnergyPerTick()) {
            return;
        }

        // Tag-aware fluid validation: ensure both required fluids are present
        FluidStack reqA = recipe.getFluidA();
        FluidStack reqB = recipe.getFluidB();
        List<ChemicalReactorRecipe.FluidTagInput> tagsA = recipe.getFluidATags();
        List<ChemicalReactorRecipe.FluidTagInput> tagsB = recipe.getFluidBTags();

        boolean foundA = false;
        boolean foundB = false;

        for (IFluidHandler tank : inputs) {
            FluidStack fs = tank.getFluidInTank(0);
            if (!foundA && matchesFluidWithTags(reqA, tagsA, fs)) {
                foundA = true;
            } else if (!foundB && matchesFluidWithTags(reqB, tagsB, fs)) {
                foundB = true;
            }
        }

        if (!foundA || !foundB) {
            recipeRunning = false;
            renderData.running = false;
            currentRecipe = null;
            return;
        }

        // Check output tank capacity BEFORE consuming fluids
        FluidStack out = recipe.getOutput();
        if (!out.isEmpty()) {
            int space = output.getTankCapacity(0) - output.getFluidInTank(0).getAmount();
            if (space < out.getAmount()) {
                // Not enough room → do NOT consume fluids
                recipeRunning = false;
                renderData.running = false;
                currentRecipe = null;
                return;
            }
        }

// Now safe to consume fluids
        consumeFluidWithTags(reqA, tagsA, inputs);
        consumeFluidWithTags(reqB, tagsB, inputs);


        currentRecipe = recipe;
        recipeRunning = true;
        recipeProgress = 0;
        recipeMaxProgress = recipe.getProcessingTime();
        renderData.running = true;
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
                FluidStack out = currentRecipe.getOutput();
                if (!out.isEmpty()) {

                    // Check output tank capacity BEFORE inserting
                    FluidStack existing = output.getFluidInTank(0);
                    int space = output.getTankCapacity(0) - existing.getAmount();

                    if (space < out.getAmount()) {
                        // Not enough room → do NOT insert anything
                        currentRecipe = null;
                        sendUpdatePacket(null);
                        return;
                    }

                    // Safe to insert
                    output.fill(out, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }

        currentRecipe = null;
        sendUpdatePacket(null);
    }


    // ---------------------------------------------------------------------
    // CLIENT FLUID STATS (AUTO-DETECT FLUID NAMES)
    // ---------------------------------------------------------------------

    private void updateClientFluidStats() {
        clientInputAName = "";
        clientInputAAmount = 0;
        clientInputACapacity = 0;

        clientInputBName = "";
        clientInputBAmount = 0;
        clientInputBCapacity = 0;

        clientOutputName = "";
        clientOutputAmount = 0;
        clientOutputCapacity = 0;

        List<IFluidHandler> inputs = getAllFluidInputs();

        if (inputs.size() >= 1) {
            IFluidHandler tankA = inputs.get(0);
            FluidStack fsA = tankA.getFluidInTank(0);
            if (!fsA.isEmpty()) {
                ResourceLocation key = BuiltInRegistries.FLUID.getKey(fsA.getFluid());
                clientInputAName = key.toString();
                clientInputAAmount = fsA.getAmount();
                clientInputACapacity = tankA.getTankCapacity(0);
            }
        }

        if (inputs.size() >= 2) {
            IFluidHandler tankB = inputs.get(1);
            FluidStack fsB = tankB.getFluidInTank(0);
            if (!fsB.isEmpty()) {
                ResourceLocation key = BuiltInRegistries.FLUID.getKey(fsB.getFluid());
                clientInputBName = key.toString();
                clientInputBAmount = fsB.getAmount();
                clientInputBCapacity = tankB.getTankCapacity(0);
            }
        }

        IFluidHandler output = getOutputTank();
        if (output != null) {
            FluidStack fs = output.getFluidInTank(0);
            if (!fs.isEmpty()) {
                ResourceLocation key = BuiltInRegistries.FLUID.getKey(fs.getFluid());
                clientOutputName = key.toString();
                clientOutputAmount = fs.getAmount();
                clientOutputCapacity = output.getTankCapacity(0);
            }
        }
    }

    // ---------------------------------------------------------------------
    // UTILS
    // ---------------------------------------------------------------------

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

    public String getClientInputAName() { return clientInputAName; }
    public int getClientInputAAmount() { return clientInputAAmount; }
    public int getClientInputACapacity() { return clientInputACapacity; }

    public String getClientInputBName() { return clientInputBName; }
    public int getClientInputBAmount() { return clientInputBAmount; }
    public int getClientInputBCapacity() { return clientInputBCapacity; }

    public String getClientOutputName() { return clientOutputName; }
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

        tag.putString("inputAName", clientInputAName);
        tag.putInt("inputAAmount", clientInputAAmount);
        tag.putInt("inputACapacity", clientInputACapacity);

        tag.putString("inputBName", clientInputBName);
        tag.putInt("inputBAmount", clientInputBAmount);
        tag.putInt("inputBCapacity", clientInputBCapacity);

        tag.putString("outputName", clientOutputName);
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

        if (tag.contains("inputAName")) clientInputAName = tag.getString("inputAName");
        if (tag.contains("inputAAmount")) clientInputAAmount = tag.getInt("inputAAmount");
        if (tag.contains("inputACapacity")) clientInputACapacity = tag.getInt("inputACapacity");

        if (tag.contains("inputBName")) clientInputBName = tag.getString("inputBName");
        if (tag.contains("inputBAmount")) clientInputBAmount = tag.getInt("inputBAmount");
        if (tag.contains("inputBCapacity")) clientInputBCapacity = tag.getInt("inputBCapacity");

        if (tag.contains("outputName")) clientOutputName = tag.getString("outputName");
        if (tag.contains("outputAmount")) clientOutputAmount = tag.getInt("outputAmount");
        if (tag.contains("outputCapacity")) clientOutputCapacity = tag.getInt("outputCapacity");
    }

    @Override
    public void readServer(CompoundTag tag, ServerPlayer sender) {}
}
