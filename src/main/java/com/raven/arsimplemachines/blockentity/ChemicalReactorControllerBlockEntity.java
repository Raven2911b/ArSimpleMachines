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
import com.raven.arsimplemachines.block.ChemicalReactorControllerBlock;

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

import com.raven.arsimplemachines.menu.ChemicalReactorMenu;

import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;

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

    private int clientEnergyStored = 0;
    private int clientEnergyMax = 0;

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
        // Adjust to your actual reactor multiblock layout
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

    public void tick() {
        if (level == null || level.isClientSide) return;

        boolean formed = getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED);

        if (!formed) {
            recipeRunning = false;
            currentRecipe = null;
            renderData.running = false;
            return;
        }

        IEnergyStorage storage = getEnergyStorage();

        if (storage != null) {
            clientEnergyStored = storage.getEnergyStored();
            clientEnergyMax = storage.getMaxEnergyStored();
                  } else {
            System.out.println("NO ENERGY STORAGE FOUND — machine cannot run.");
        }

        updateClientFluidStats();
        sendUpdatePacket(null);

        if (!recipeRunning) {
            tryStartRecipe();
            return;
        }

        if (currentRecipe == null) {
            recipeRunning = false;
            renderData.running = false;
            return;
        }

        if (storage == null) {
            return;
        }

        if (storage.getEnergyStored() < currentRecipe.getEnergyPerTick()) {
            return;
        }

        storage.extractEnergy(currentRecipe.getEnergyPerTick(), false);

        recipeProgress++;

        if (recipeProgress >= recipeMaxProgress) {
            finishRecipe();
        }
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

    private void updateClientFluidStats() {
        IFluidHandler hydrogen = getHydrogenTank();
        IFluidHandler oxygen = getOxygenTank();
        IFluidHandler output = getOutputTank();

        if (hydrogen != null) {
            clientHydrogenAmount = hydrogen.getFluidInTank(0).getAmount();
            clientHydrogenCapacity = hydrogen.getTankCapacity(0);
        }

        if (oxygen != null) {
            clientOxygenAmount = oxygen.getFluidInTank(0).getAmount();
            clientOxygenCapacity = oxygen.getTankCapacity(0);
        }

        if (output != null) {
            clientOutputAmount = output.getFluidInTank(0).getAmount();
            clientOutputCapacity = output.getTankCapacity(0);
        }
    }

    private void tryStartRecipe() {

        IEnergyStorage storage = getEnergyStorage();
        if (storage == null) return;

        IFluidHandler hydrogen = getHydrogenTank();
        IFluidHandler oxygen = getOxygenTank();
        IFluidHandler output = getOutputTank();

        if (hydrogen == null || oxygen == null || output == null) {
            return;
        }

        var hydrogenStack = hydrogen.getFluidInTank(0);
        var oxygenStack = oxygen.getFluidInTank(0);

        var allRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.CHEMICAL_REACTOR_TYPE.get());

        ChemicalReactorRecipe recipe = null;

        for (var holder : allRecipes) {
            var r = holder.value();
            if (r.matches(new ChemicalReactorRecipeInput(hydrogenStack, oxygenStack), level)) {
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

        if (!recipe.canConsume(hydrogenStack, oxygenStack)) {
            return;
        }

        recipe.consumeInputs(hydrogen, oxygen);

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
            IFluidHandler output = getOutputTank();
            if (output != null) {
                output.fill(currentRecipe.getOutput(), IFluidHandler.FluidAction.EXECUTE);
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
    private IFluidHandler getTankAt(BlockPos pos) {
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

    private BlockPos getHydrogenPos() {
        return rotateOffset(+1, 0, +1);
    }

    private BlockPos getOxygenPos() {
        return rotateOffset(-1, 0, +1);
    }

    private BlockPos getOutputPos() {
        return rotateOffset(0, -1, +1);
    }



    private IFluidHandler getHydrogenTank() {
        return getTankAt(getHydrogenPos());
    }

    private IFluidHandler getOxygenTank() {
        return getTankAt(getOxygenPos());
    }

    private IFluidHandler getOutputTank() {
        return getTankAt(getOutputPos());
    }



    public void clientTick() {
        if (level == null || !level.isClientSide) return;

        if (renderData.running) {
            renderData.animPhase = (renderData.animPhase + 0.05f) % (float) (Math.PI * 2);
        } else {
            renderData.animPhase = 0f;
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

        tag.putInt("energyStored", clientEnergyStored);
        tag.putInt("energyMax", clientEnergyMax);

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

        if (tag.contains("energyStored")) clientEnergyStored = tag.getInt("energyStored");
        if (tag.contains("energyMax")) clientEnergyMax = tag.getInt("energyMax");

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
