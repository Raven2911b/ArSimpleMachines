package com.raven.arsimplemachines.blockentity;

import ARLib.blockentities.EntityItemInputBlock;
import ARLib.blockentities.EntityItemOutputBlock;
import com.raven.arsimplemachines.multiblock.LathePattern;
import com.raven.arsimplemachines.multiblock.MultiblockControllerBlock;
import com.raven.arsimplemachines.multiblock.MultiblockControllerBlockEntity;
import com.raven.arsimplemachines.recipe.LatheRecipeRegistry;
import com.raven.arsimplemachines.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.energy.IEnergyStorage;

import ARLib.ARLibRegistry;
import ARLib.multiblockCore.EntityMultiblockPlaceholder;
import ARLib.multiblockCore.BlockMultiblockMaster;

import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;

public class LatheControllerBlockEntity extends MultiblockControllerBlockEntity implements INetworkTagReceiver {

    public static class RenderData {
        public float shaftRotation = 0f;
        public float toolOffset = 0f;
        public float rodOffset = 0f;
        public boolean running = false;
    }

    public RenderData renderData = new RenderData();
    public boolean isFormed() {
        return this.formed; // or whatever your internal flag is called
    }


    // Recipe state
    private boolean hasPower = false;
    private static final int ENERGY_PER_TICK = 20;

    public boolean recipeRunning = false;
    private int recipeProgress = 0;
    private int recipeMaxProgress = 0;
    private ItemStack processingInput = ItemStack.EMPTY;

    public LatheControllerBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state, ModBlockEntities.LATHE_CONTROLLER.get());
        System.out.println("LatheControllerBlockEntity: constructor called");
        this.setPattern(new LathePattern());
    }

    // SERVER TICK
    @Override
    public void serverTick() {
        if (level == null || level.isClientSide) return;

        validateStructure();

        // --- Recipe tick loop ---
        if (!recipeRunning) {
            if (checkPower()) {
                tryStartRecipe();
            }
        } else {
            IEnergyStorage storage = getEnergyStorage();
            if (storage == null) return;

// Not enough energy → pause
            if (storage.getEnergyStored() < ENERGY_PER_TICK) {
                System.out.println("Lathe: paused (not enough energy)");
                return;
            }

// Consume energy
            storage.extractEnergy(ENERGY_PER_TICK, false);

            // Progress recipe
            recipeProgress++;

            if (recipeProgress >= recipeMaxProgress) {
                finishRecipe();
            }
        }


    }
    private boolean checkPower() {
        IEnergyStorage storage = getEnergyStorage();
        if (storage == null) return false;

        return storage.getEnergyStored() >= ENERGY_PER_TICK;
    }

    // REAL RECIPE: Iron Ingot → Iron Nuggets
    // REAL RECIPE: Pull from input block and process
    private void tryStartRecipe() {
        if (!isFormed()) {
            System.out.println("Lathe: not formed, skipping recipe");
            return;
        }
        hasPower = checkPower();
        if (!hasPower) {
            System.out.println("Lathe: no power, skipping recipe");
            return;
        }

        // Locate ARLib Item Input Block
        BlockPos inputPos = getInputBlockPos();
        if (inputPos == null) {

            return;
        }

        BlockEntity be = level.getBlockEntity(inputPos);

        if (!(be instanceof EntityItemInputBlock inputBlock)) {

            return;
        }

        // Look for ANY item that has a recipe
        for (int slot = 0; slot < inputBlock.inventory.getSlots(); slot++) {
            ItemStack stack = inputBlock.inventory.getStackInSlot(slot);
            //System.out.println("  Slot " + slot + ": " + (stack.isEmpty() ? "EMPTY" : stack.getItem().getDescription().getString() + " x" + stack.getCount()));

            if (stack.isEmpty()) continue;

            // Check if this item has a recipe
            LatheRecipeRegistry.LatheRecipe recipe = LatheRecipeRegistry.findRecipe(stack);
            if (recipe != null) {
                System.out.println("  -> Found recipe for " + stack.getItem().getDescription().getString());

                // Extract 1 item
                inputBlock.inventory.extractItem(slot, 1, false);
                System.out.println("  -> Extracted 1 item");

                // Start recipe
                recipeRunning = true;
                recipeProgress = 0;
                recipeMaxProgress = recipe.processingTime;
                processingInput = stack.copy();
                processingInput.setCount(1);

                System.out.println("Lathe: SERVER recipeRunning set to TRUE at " + worldPosition);

// Mark BE dirty
                this.setChanged();

// Update client animation state
                renderData.running = true;

// ARLib sync to client
                sendUpdatePacket(null);

                System.out.println("Lathe: STARTING RECIPE for " + stack.getItem().getDescription().getString()
                        + " (time: " + recipe.processingTime + " ticks)");

                return;
            }
            }

    }

    private void finishRecipe() {
        // Stop recipe
        recipeRunning = false;
        recipeProgress = 0;
        recipeMaxProgress = 0;

        // Stop animation
        renderData.running = false;

        // Look up recipe again
        LatheRecipeRegistry.LatheRecipe recipe = LatheRecipeRegistry.findRecipe(processingInput);
        if (recipe == null) {
            System.out.println("Lathe: recipe for " + processingInput.getItem().getDescription().getString() + " not found!");
            processingInput = ItemStack.EMPTY;
            this.setChanged();
            sendUpdatePacket(null);   // <-- ARLib sync
            return;
        }

        // Create output
        ItemStack output = recipe.output.copy();

        // Locate ARLib output block
        BlockPos outputPos = getOutputBlockPos();
        System.out.println("Lathe: finishRecipe - output block pos = " + outputPos);

        if (outputPos != null) {
            BlockEntity be = level.getBlockEntity(outputPos);
            System.out.println("Lathe: output block entity type: " + (be != null ? be.getClass().getSimpleName() : "NULL"));

            if (be instanceof EntityItemOutputBlock outputBlock) {
                ItemStack remainder = output.copy();
                int insertedTotal = 0;

                for (int slot = 0; slot < outputBlock.inventory.getSlots(); slot++) {
                    ItemStack before = remainder.copy();
                    remainder = outputBlock.inventory.insertItem(slot, remainder, false);
                    int inserted = before.getCount() - remainder.getCount();
                    insertedTotal += inserted;
                    if (remainder.isEmpty()) break;
                }

                if (remainder.isEmpty()) {
                    System.out.println("Lathe: SUCCESS - output " + insertedTotal + "x " + output.getItem().getDescription().getString());
                } else if (insertedTotal > 0) {
                    System.out.println("Lathe: PARTIAL - output " + insertedTotal + "x, " + remainder.getCount() + " items rejected");
                } else {
                    System.out.println("Lathe: REJECTED - no available output slot for " + output.getItem().getDescription().getString());
                }
            }
        } else {
            System.out.println("Lathe: output block NOT FOUND");
        }

        // Clear input
        processingInput = ItemStack.EMPTY;

        // Sync to client
        System.out.println("Lathe: SERVER sending recipe stop update (recipeRunning=false)");
        this.setChanged();
        sendUpdatePacket(null);   // <-- ARLib sync
    }

    // --- INPUT BLOCK POSITION (pattern 'I') ---
    // Search for ARLib input block near controller
    private BlockPos getInputBlockPos() {
        if (level == null) return null;

        // Search in a larger cube around controller
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos checkPos = worldPosition.offset(dx, dy, dz);
                    BlockEntity be = level.getBlockEntity(checkPos);
                    Block block = level.getBlockState(checkPos).getBlock();

                    if (be instanceof ARLib.blockentities.EntityItemInputBlock) {

                        return checkPos;
                    }
                }
            }
        }
        return null;
    }
    // --- OUTPUT BLOCK POSITION (pattern 'O') ---
    // Search for ARLib output block near controller
    private BlockPos getOutputBlockPos() {
        if (level == null) return null;

        // Search in a larger cube around controller
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos checkPos = worldPosition.offset(dx, dy, dz);
                    BlockEntity be = level.getBlockEntity(checkPos);
                    Block block = level.getBlockState(checkPos).getBlock();

                    if (be instanceof ARLib.blockentities.EntityItemOutputBlock) {
                        return checkPos;
                    }
                }
            }
        }
        return null;
    }
    private IEnergyStorage getEnergyStorage() {
        BlockPos below = worldPosition.below();
        BlockState state = level.getBlockState(below);
        BlockEntity be = level.getBlockEntity(below);
        if (be == null) return null;

        return level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                below,
                state,
                be,
                null
        );
    }

    // CLIENT TICK — animation only
    public void clientTick() {
        if (level == null || !level.isClientSide) return;

        if (renderData.running) {
            renderData.shaftRotation = (renderData.shaftRotation + 8.0f) % 360f;
            float t = (float)Math.sin(level.getGameTime() * 0.05f);
            renderData.toolOffset = (t * 0.5f + 0.5f) * 1.11f;
            renderData.rodOffset = (renderData.rodOffset + 8.0f) % 360f;
        } else {
            renderData.shaftRotation = 0f;
            renderData.toolOffset = 0f;
            renderData.rodOffset = 0f;
        }
    }

    @Override
    public void onFormedChanged(boolean formed) {
        if (level == null || level.isClientSide) return;
        this.formed = formed;
        this.setChanged();
        sendUpdatePacket(null);
        // Reset recipe state when structure is formed/unformed
        if (!formed) {
            recipeRunning = false;
            recipeProgress = 0;
            recipeMaxProgress = 0;
            renderData.running = false;
            sendUpdatePacket(null);
            processingInput = ItemStack.EMPTY;
            this.setChanged();
            sendUpdatePacket(null);
        }
        // Get facing from current block state (if it still exists)
        Direction facing = null;
        try {
            BlockState state = level.getBlockState(worldPosition);
            if (state.hasProperty(MultiblockControllerBlock.FACING)) {
                facing = state.getValue(MultiblockControllerBlock.FACING);
            }
        } catch (Exception e) {
            // Block might be gone, use default
            facing = Direction.NORTH;
        }
        applyPlaceholdersForPattern(formed, facing);
    }


    public void applyPlaceholdersForPattern(boolean formed, Direction facing) {
        if (level == null || level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        Object[][][] pattern = LathePattern.PATTERN;

        // Find controller index
        int cx = -1, cy = -1, cz = -1;
        outer:
        for (int y = 0; y < pattern.length; y++) {
            for (int z = 0; z < pattern[y].length; z++) {
                for (int x = 0; x < pattern[y][z].length; x++) {
                    Object v = pattern[y][z][x];
                    if (v instanceof Character ch && ch == 'C') {
                        cx = x; cy = y; cz = z;
                        break outer;
                    }
                }
            }
        }
        if (cx == -1) {
            System.out.println("applyPlaceholdersForPattern: controller index not found");
            return;
        }

        int rotation = switch (facing) {
            case SOUTH -> 0;
            case EAST -> 1;
            case NORTH -> 2;
            case WEST -> 3;
            default -> 0;
        };

        // ... rest of the method stays the same

        for (int y = 0; y < pattern.length; y++) {
            for (int z = 0; z < pattern[y].length; z++) {
                for (int x = 0; x < pattern[y][z].length; x++) {
                    Object expected = pattern[y][z][x];
                    if (!(expected instanceof Character ch)) continue;
                    if (!(ch == 'M' || ch == 'S')) continue;

                    int rx = x - cx;
                    int ry = y - cy;
                    int rz = z - cz;

                    BlockPos rotated = rotateOffset(rx, ry, rz, rotation);
                    BlockPos worldPos = worldPosition.offset(rotated);

                    BlockState current = level.getBlockState(worldPos);

                    if (formed) {
                        if (level.getBlockEntity(worldPos) instanceof EntityMultiblockPlaceholder existing) {
                            existing.renderBlock = false;
                            existing.setChanged();
                            continue;
                        }

                        Block placeholderBlock = ARLibRegistry.BLOCK_PLACEHOLDER.get();
                        BlockState placeholderState = placeholderBlock.defaultBlockState();
                        BlockState originalState = current;

                        level.setBlock(worldPos, placeholderState, 3);

                        if (level.getBlockEntity(worldPos) instanceof EntityMultiblockPlaceholder tile) {
                            tile.replacedState = originalState;
                            tile.renderBlock = false;
                            tile.setChanged();
                        }
                    } else {
                        // Unforming: restore placeholder blocks and make structure visible
                        if (level.getBlockEntity(worldPos) instanceof EntityMultiblockPlaceholder placeholder) {
                            BlockState replaced = placeholder.replacedState != null ? placeholder.replacedState : Blocks.AIR.defaultBlockState();
                            level.setBlock(worldPos, replaced, 3);
                            System.out.println("applyPlaceholdersForPattern: restored placeholder at " + worldPos);
                            continue;
                        }

                        // Also clear the formed flag on regular multiblock blocks
                        if (current.hasProperty(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) {
                            level.setBlock(worldPos, current.setValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED, false), 3);
                            System.out.println("applyPlaceholdersForPattern: cleared FORMED flag at " + worldPos);
                        }

                        // Update formed property on ARLib blocks (they may cache formed state)
                        BlockEntity be = level.getBlockEntity(worldPos);
                        if (be != null && be.getClass().getName().contains("EntityMultiblock")) {
                            // ARLib block entity — notify it the multiblock is no longer formed
                            be.setChanged();
                            System.out.println("applyPlaceholdersForPattern: marked ARLib BE as changed at " + worldPos);
                        }
                    }
                }
            }
        }
    }

    private BlockPos rotateOffset(int x, int y, int z, int rotation) {
        return switch (rotation) {
            case 0 -> new BlockPos(x, y, z);        // NORTH (no rotation)
            case 1 -> new BlockPos(z, y, -x);       // EAST  (90° clockwise)
            case 2 -> new BlockPos(-x, y, -z);      // SOUTH (180°)
            case 3 -> new BlockPos(-z, y, x);       // WEST  (270°)
            default -> BlockPos.ZERO;
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putBoolean("Formed", this.formed);
        tag.putFloat("shaftRotation", renderData.shaftRotation);
        tag.putFloat("toolOffset", renderData.toolOffset);
        tag.putFloat("rodOffset", renderData.rodOffset);

        // Recipe sync
        tag.putBoolean("recipeRunning", recipeRunning);
        tag.putInt("recipeProgress", recipeProgress);
        tag.putInt("recipeMaxProgress", recipeMaxProgress);
        // (Optional) persist input if you want to show it client-side:
        // if (!processingInput.isEmpty()) tag.put("processingInput", processingInput.save(new CompoundTag()));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("Formed")) this.formed = tag.getBoolean("Formed");

        if (tag.contains("shaftRotation")) renderData.shaftRotation = tag.getFloat("shaftRotation");
        if (tag.contains("toolOffset")) renderData.toolOffset = tag.getFloat("toolOffset");
        if (tag.contains("rodOffset")) renderData.rodOffset = tag.getFloat("rodOffset");

        // Recipe sync
        if (tag.contains("recipeRunning")) recipeRunning = tag.getBoolean("recipeRunning");
        if (tag.contains("recipeProgress")) recipeProgress = tag.getInt("recipeProgress");
        if (tag.contains("recipeMaxProgress")) recipeMaxProgress = tag.getInt("recipeMaxProgress");
        // (Optional) read processingInput if you wrote it
        // if (tag.contains("processingInput")) processingInput = ItemStack.of(tag.getCompound("processingInput"));
    }



    public void sendUpdatePacket(ServerPlayer specificPlayer) {
        if (level == null || level.isClientSide) return;
        ServerLevel server = (ServerLevel) level;

        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Formed", this.formed);

        // animation / recipe state to sync:
        tag.putBoolean("recipeRunning", recipeRunning);
        tag.putInt("recipeProgress", recipeProgress);
        tag.putInt("recipeMaxProgress", recipeMaxProgress);

        tag.putFloat("shaftRotation", renderData.shaftRotation);
        tag.putFloat("toolOffset", renderData.toolOffset);
        tag.putFloat("rodOffset", renderData.rodOffset);
        tag.putBoolean("running", renderData.running);

        PacketBlockEntity packet = PacketBlockEntity.getBlockEntityPacket(this, tag);

        if (specificPlayer != null) {
            PacketDistributor.sendToPlayer(specificPlayer, packet);
        } else {
            PacketDistributor.sendToPlayersTrackingChunk(server, new ChunkPos(worldPosition), packet);
        }
    }
    @Override
    public void readClient(CompoundTag tag) {
        if (tag.contains("Formed"))
            this.formed = tag.getBoolean("Formed");
        if (tag.contains("recipeRunning"))
            recipeRunning = tag.getBoolean("recipeRunning");
        if (tag.contains("recipeProgress"))
            recipeProgress = tag.getInt("recipeProgress");
        if (tag.contains("recipeMaxProgress"))
            recipeMaxProgress = tag.getInt("recipeMaxProgress");

        if (tag.contains("shaftRotation"))
            renderData.shaftRotation = tag.getFloat("shaftRotation");
        if (tag.contains("toolOffset"))
            renderData.toolOffset = tag.getFloat("toolOffset");
        if (tag.contains("rodOffset"))
            renderData.rodOffset = tag.getFloat("rodOffset");
        if (tag.contains("running"))
            renderData.running = tag.getBoolean("running");
    }

    @Override
    public void readServer(CompoundTag tag, ServerPlayer sender) {
        // No server-side GUI actions yet
    }


}
