package com.raven.arsimplemachines.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public abstract class MultiblockControllerBlockEntity extends BlockEntity {

    protected boolean formed = false;
    protected MultiblockPattern pattern;

    protected MultiblockControllerBlockEntity(BlockPos pos, BlockState state, BlockEntityType<?> type) {
        super(type, pos, state);
    }
    public MultiblockPattern getPattern() {
        return pattern;
    }
    public boolean isFormed() {
        return formed;
    }

    public void setPattern(MultiblockPattern pattern) {
        this.pattern = pattern;
    }

    public void validateStructure() {
        if (pattern == null || level == null) return;

        boolean newState = pattern.validate(level, worldPosition);
        if (newState != formed) {
            formed = newState;
            setChanged();

            BlockState state = level.getBlockState(worldPosition);
            if (state.hasProperty(MultiblockControllerBlock.FORMED)) {
                level.setBlock(worldPosition, state.setValue(MultiblockControllerBlock.FORMED, formed), 3);
            }

            // Propagate the "formed" property to all pattern parts that support ARLib's STATE_MULTIBLOCK_FORMED.
            if (pattern != null) {
                try {
                    // This method computes rotation internally and updates world blocks.
                    pattern.applyFormedToWorld(level, worldPosition, formed);
                    System.out.println("validateStructure: applied formed=" + formed + " to pattern parts");
                } catch (Exception e) {
                    System.out.println("validateStructure: failed to apply formed to pattern parts: " + e);
                }
            }

            // Hook for subclasses to react to formed state changes (server-side)
            onFormedChanged(formed);
        }
    }

    public boolean shouldHideBlock(int y, int z, int x, BlockState stateInWorld) {
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putBoolean("Formed", formed);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        formed = tag.getBoolean("Formed");
    }
    /**
     * Called when the formed flag changes. Subclasses override to react (server-side).
     * Default implementation does nothing.
     */
    protected void onFormedChanged(boolean formed) {
        // no-op in base class
    }
    public abstract void serverTick();
}