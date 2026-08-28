package com.raven.arsimplemachines.blockentity;

import ARLib.ARLibRegistry;
import ARLib.multiblockCore.EntityMultiblockMachineMaster;
import ARLib.multiblockCore.BlockMultiblockMaster;

import com.raven.arsimplemachines.menu.ElectrolyzerMenu;
import com.raven.arsimplemachines.recipe.MachineRecipe;
import com.raven.arsimplemachines.recipe.MachineRecipeInput;
import com.raven.arsimplemachines.recipe.MachineRecipeMatcher;

import com.raven.arsimplemachines.registry.ModBlockEntities;
import com.raven.arsimplemachines.registry.ModBlocks;
import com.raven.arsimplemachines.registry.ModRecipeTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.fluids.FluidStack;

import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified Electrolyzer Controller
 *
 * - Uses MachineRecipe + MachineRecipeInput
 * - One fluid input
 * - Two fluid outputs
 * - No item inputs/outputs
 * - Unified matching + processing
 */
public class ElectrolyzerControllerBlockEntity extends EntityMultiblockMachineMaster
        implements INetworkTagReceiver, MenuProvider {

    // ---------------------------------------------------------
    // RENDER DATA
    // ---------------------------------------------------------
    public static class RenderData {
        public boolean running = false;
        public float animPhase = 0f;
    }

    public RenderData renderData = new RenderData();

    // ---------------------------------------------------------
    // RECIPE STATE (Unified)
    // ---------------------------------------------------------
    private MachineRecipe currentRecipe;

    private boolean recipeRunning = false;
    private int recipeProgress = 0;
    private int recipeMaxProgress = 0;

    // ---------------------------------------------------------
    // CLIENT SYNC FIELDS
    // ---------------------------------------------------------
    private int clientEnergyStored = 0;
    private int clientEnergyMax = 0;

    private int clientInputAmount = 0;
    private int clientInputCapacity = 0;

    private int clientOutputAAmount = 0;
    private int clientOutputACapacity = 0;

    private int clientOutputBAmount = 0;
    private int clientOutputBCapacity = 0;

    // Fluid names (NEW unified system)
    private String clientInputName = "";
    private String clientOutputAName = "";
    private String clientOutputBName = "";

    // ---------------------------------------------------------
    // CLIENT FLUIDSTACKS (synced from server)
    // ---------------------------------------------------------
    private FluidStack clientInputFluid = FluidStack.EMPTY;
    private FluidStack clientOutputAFluid = FluidStack.EMPTY;
    private FluidStack clientOutputBFluid = FluidStack.EMPTY;

    // ---------------------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------------------
    public ElectrolyzerControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTROLYZER_CONTROLLER.get(), pos, state);
    }
    public FluidStack getClientInputFluid() {
        return clientInputFluid;
    }

    public FluidStack getClientOutputAFluid() {
        return clientOutputAFluid;
    }

    public FluidStack getClientOutputBFluid() {
        return clientOutputBFluid;
    }

    // ---------------------------------------------------------
    // MENU + TITLE
    // ---------------------------------------------------------
    @Override
    public Component getDisplayName() {
        return Component.literal("Electrolyzer");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new ElectrolyzerMenu(windowId, inv, this.getBlockPos());
    }

    // ---------------------------------------------------------
    // STRUCTURE DEFINITION
    // ---------------------------------------------------------
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
    private List<IEnergyStorage> getAllEnergyStorages() {
        List<IEnergyStorage> list = new ArrayList<>();

        List<BlockPos> positions = findAllBlocks(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get());
        for (BlockPos pos : positions) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null) continue;

            IEnergyStorage es = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    pos,
                    level.getBlockState(pos),
                    be,
                    null
            );

            if (es != null) list.add(es);
        }

        return list;
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
    // MACHINE LOGIC — STRUCTURE EVENTS
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

    // -------------------------------------------------------------------------
    // SERVER TICK (Unified)
    // -------------------------------------------------------------------------
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

        List<IEnergyStorage> storages = getAllEnergyStorages();
        clientEnergyStored = 0;
        clientEnergyMax = 0;

        for (IEnergyStorage es : storages) {
            clientEnergyStored += es.getEnergyStored();
            clientEnergyMax += es.getMaxEnergyStored();
        }
        if (!recipeRunning) {
            tryStartRecipe();
        }

        if (recipeRunning && currentRecipe != null && !storages.isEmpty()) {

            int needed = currentRecipe.getEnergyPerTick();

            for (IEnergyStorage es : storages) {
                if (needed <= 0) break;

                int extracted = es.extractEnergy(needed, false);
                needed -= extracted;
            }

            if (needed > 0) {
                // Not enough energy after attempting extraction
                recipeRunning = false;
                renderData.running = false;
                currentRecipe = null;
                return;
            }
            recipeProgress++;
            if (recipeProgress >= recipeMaxProgress) {
                finishRecipe();
            }
        }

        updateClientFluidStats();
        sendUpdatePacket(null);
    }

    // -------------------------------------------------------------------------
    // UNIFIED RECIPE START LOGIC
    // -------------------------------------------------------------------------
    private void tryStartRecipe() {
        if (level == null) return;

        List<IEnergyStorage> storages = getAllEnergyStorages();
        if (storages.isEmpty()) return;

        IFluidHandler input = getInputTank();
        IFluidHandler outA = getOutputATank();
        IFluidHandler outB = getOutputBTank();

        if (input == null || outA == null || outB == null) return;

        // Build unified MachineRecipeInput (one fluid input)
        MachineRecipeInput recipeInput = new MachineRecipeInput();
        var inputStack = input.getFluidInTank(0);
        recipeInput.addFluid(inputStack);

        // Find matching recipe using unified matcher
        var allRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.ELECTROLYZER_TYPE.get());

        MachineRecipe found = null;

        for (var holder : allRecipes) {
            var r = holder.value();
            if (MachineRecipeMatcher.matches(r, recipeInput.getItems(), recipeInput.getFluids(), level)) {
                found = r;
                break;
            }
        }

        if (found == null) {
            recipeRunning = false;
            renderData.running = false;
            currentRecipe = null;
            return;
        }

        // Energy check (per tick, same as original)
        int totalEnergy = 0;
        for (IEnergyStorage es : storages) {
            totalEnergy += es.getEnergyStored();
        }

        if (totalEnergy < found.getEnergyPerTick()) {
            return;
        }


        // Fluid amount + capacity checks
        var requiredInput = found.getFluidInputs().isEmpty() ? null : found.getFluidInputs().get(0);
        var outAStack = outA.getFluidInTank(0);
        var outBStack = outB.getFluidInTank(0);

        var outARequired = found.getFluidOutputs().size() > 0 ? found.getFluidOutputs().get(0) : FluidStack.EMPTY;
        var outBRequired = found.getFluidOutputs().size() > 1 ? found.getFluidOutputs().get(1) : FluidStack.EMPTY;

        // Ensure enough input fluid
        if (requiredInput != null) {
            if (!inputStack.isFluidEqual(requiredInput) ||
                    inputStack.getAmount() < requiredInput.getAmount()) {
                return;
            }
        }

        // Ensure output A has enough free capacity
        int outACurrent = outAStack.getAmount();
        int outACapacity = outA.getTankCapacity(0);
        if (!outAStack.isEmpty() && !outAStack.isFluidEqual(outARequired)) {
            // Different fluid already present — cannot mix
            return;
        }
        if (outACurrent + outARequired.getAmount() > outACapacity) {
            return;
        }

        // Ensure output B has enough free capacity
        int outBCurrent = outBStack.getAmount();
        int outBCapacity = outB.getTankCapacity(0);
        if (!outBStack.isEmpty() && !outBStack.isFluidEqual(outBRequired)) {
            // Different fluid already present — cannot mix
            return;
        }
        if (outBCurrent + outBRequired.getAmount() > outBCapacity) {
            return;
        }

        // Consume input immediately (same behavior as original consumeInputs)
        if (requiredInput != null) {
            input.drain(requiredInput, IFluidHandler.FluidAction.EXECUTE);
        }

        // Start unified recipe
        currentRecipe = found;
        recipeRunning = true;
        recipeProgress = 0;
        recipeMaxProgress = found.getProcessingTime();
        renderData.running = true;
    }
    // -------------------------------------------------------------------------
    // RECIPE COMPLETION (Unified)
    // -------------------------------------------------------------------------
    private void finishRecipe() {
        recipeRunning = false;
        renderData.running = false;

        if (currentRecipe != null) {
            IFluidHandler outA = getOutputATank();
            IFluidHandler outB = getOutputBTank();

            var outputs = currentRecipe.getFluidOutputs();

            FluidStack outARequired = outputs.size() > 0 ? outputs.get(0).copy() : FluidStack.EMPTY;
            FluidStack outBRequired = outputs.size() > 1 ? outputs.get(1).copy() : FluidStack.EMPTY;

            if (outA != null && !outARequired.isEmpty()) {
                outA.fill(outARequired, IFluidHandler.FluidAction.EXECUTE);
            }
            if (outB != null && !outBRequired.isEmpty()) {
                outB.fill(outBRequired, IFluidHandler.FluidAction.EXECUTE);
            }
        }
        currentRecipe = null;
    }

    // -------------------------------------------------------------------------
    // CLIENT FLUID STATS + NAMES (Unified)
    // -------------------------------------------------------------------------
    private void updateClientFluidStats() {
        IFluidHandler input = getInputTank();
        IFluidHandler outA = getOutputATank();
        IFluidHandler outB = getOutputBTank();

        clientInputAmount = 0;
        clientInputCapacity = 0;
        clientInputName = "";

        clientOutputAAmount = 0;
        clientOutputACapacity = 0;
        clientOutputAName = "";

        clientOutputBAmount = 0;
        clientOutputBCapacity = 0;
        clientOutputBName = "";

        if (input != null) {
            var stack = input.getFluidInTank(0);
            clientInputFluid = stack.copy();
            clientInputAmount = stack.getAmount();
            clientInputCapacity = input.getTankCapacity(0);

            if (!stack.isEmpty()) {
                clientInputName = Component.translatable(stack.getTranslationKey()).getString();
            }
        }

        if (outA != null) {
            var stack = outA.getFluidInTank(0);
            clientOutputAFluid = stack.copy();
            clientOutputAAmount = stack.getAmount();
            clientOutputACapacity = outA.getTankCapacity(0);

            if (!stack.isEmpty()) {
                clientOutputAName = Component.translatable(stack.getTranslationKey()).getString();
            }
        }

        if (outB != null) {
            var stack = outB.getFluidInTank(0);
            clientOutputBFluid = stack.copy();
            clientOutputBAmount = stack.getAmount();
            clientOutputBCapacity = outB.getTankCapacity(0);

            if (!stack.isEmpty()) {
                clientOutputBName = Component.translatable(stack.getTranslationKey()).getString();
            }
        }
    }
    // -------------------------------------------------------------------------
    // CLIENT TICK
    // -------------------------------------------------------------------------
    public void clientTick() {
        if (renderData.running) {
            renderData.animPhase = (renderData.animPhase + 0.05f) % (float) (Math.PI * 2);
        } else {
            renderData.animPhase = 0f;
        }
    }

    // -------------------------------------------------------------------------
    // NETWORK GETTERS (for Menu/Screen)
    // -------------------------------------------------------------------------
    public int getRecipeProgress() { return recipeProgress; }
    public int getRecipeMaxProgress() { return recipeMaxProgress; }

    public int getClientEnergyStored() { return clientEnergyStored; }
    public int getClientEnergyMax() { return clientEnergyMax; }

    public int getClientInputAmount() { return clientInputAmount; }
    public int getClientInputCapacity() { return clientInputCapacity; }
    public String getClientInputName() { return clientInputName; }

    public int getClientOutputAAmount() { return clientOutputAAmount; }
    public int getClientOutputACapacity() { return clientOutputACapacity; }
    public String getClientOutputAName() { return clientOutputAName; }

    public int getClientOutputBAmount() { return clientOutputBAmount; }
    public int getClientOutputBCapacity() { return clientOutputBCapacity; }
    public String getClientOutputBName() { return clientOutputBName; }

    // -------------------------------------------------------------------------
    // NETWORK SYNC
    // -------------------------------------------------------------------------
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
        tag.putString("inputName", clientInputName);

        tag.putInt("outputAAmount", clientOutputAAmount);
        tag.putInt("outputACapacity", clientOutputACapacity);
        tag.putString("outputAName", clientOutputAName);

        tag.putInt("outputBAmount", clientOutputBAmount);
        tag.putInt("outputBCapacity", clientOutputBCapacity);
        tag.putString("outputBName", clientOutputBName);

        // INPUT FLUID
        if (!clientInputFluid.isEmpty()) {
            int tint = IClientFluidTypeExtensions.of(clientInputFluid.getFluid())
                    .getTintColor(clientInputFluid);
            tag.put("inputFluid", FluidStack.CODEC.encodeStart(NbtOps.INSTANCE, clientInputFluid).getOrThrow());
        } else {
            tag.put("inputFluid", new CompoundTag()); // empty tag
        }

        // OUTPUT A FLUID
        if (!clientOutputAFluid.isEmpty()) {
            int tintA = IClientFluidTypeExtensions.of(clientOutputAFluid.getFluid())
                    .getTintColor(clientOutputAFluid);
            tag.put("outputAFluid", FluidStack.CODEC.encodeStart(NbtOps.INSTANCE, clientOutputAFluid).getOrThrow());
        } else {
            tag.put("outputAFluid", new CompoundTag());
        }

        // OUTPUT B FLUID
        if (!clientOutputBFluid.isEmpty()) {
            int tintB = IClientFluidTypeExtensions.of(clientOutputBFluid.getFluid())
                    .getTintColor(clientOutputBFluid);
            tag.put("outputBFluid", FluidStack.CODEC.encodeStart(NbtOps.INSTANCE, clientOutputBFluid).getOrThrow());
        } else {
            tag.put("outputBFluid", new CompoundTag());
        }

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
        if (tag.contains("inputName")) clientInputName = tag.getString("inputName");

        if (tag.contains("outputAAmount")) clientOutputAAmount = tag.getInt("outputAAmount");
        if (tag.contains("outputACapacity")) clientOutputACapacity = tag.getInt("outputACapacity");
        if (tag.contains("outputAName")) clientOutputAName = tag.getString("outputAName");

        if (tag.contains("outputBAmount")) clientOutputBAmount = tag.getInt("outputBAmount");
        if (tag.contains("outputBCapacity")) clientOutputBCapacity = tag.getInt("outputBCapacity");
        if (tag.contains("outputBName")) clientOutputBName = tag.getString("outputBName");
        if (tag.contains("inputFluid")) {

            int tint = IClientFluidTypeExtensions.of(clientInputFluid.getFluid())
                    .getTintColor(clientInputFluid);
            CompoundTag f = tag.getCompound("inputFluid");
            clientInputFluid = f.isEmpty() ? FluidStack.EMPTY :
                    FluidStack.CODEC.parse(NbtOps.INSTANCE, f).getOrThrow();
        }

        // in readClient
        if (tag.contains("outputAFluid")) {
            CompoundTag f = tag.getCompound("outputAFluid");
            clientOutputAFluid = f.isEmpty() ? FluidStack.EMPTY :
                    FluidStack.CODEC.parse(NbtOps.INSTANCE, f).getOrThrow();
        }

        if (tag.contains("outputBFluid")) {
            CompoundTag f = tag.getCompound("outputBFluid");
            clientOutputBFluid = f.isEmpty() ? FluidStack.EMPTY :
                    FluidStack.CODEC.parse(NbtOps.INSTANCE, f).getOrThrow();
        }



    }

    @Override
    public void readServer(CompoundTag tag, ServerPlayer sender) {}
}

