package com.raven.arsimplemachines.multiblock;

import ARLib.ARLibRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;
import ARLib.multiblockCore.BlockMultiblockMaster;
import net.minecraft.world.level.block.state.BlockState;
public class MultiblockPattern {

    private final Object[][][] pattern;
    private final Map<Character, List<Block>> mapping;

    public MultiblockPattern(Object[][][] pattern, Map<Character, List<Block>> mapping) {
        this.pattern = pattern;
        this.mapping = mapping;
    }

    public boolean validate(Level level, BlockPos controllerPos) {

        Direction facing = level.getBlockState(controllerPos)
                .getValue(MultiblockControllerBlock.FACING);

        int rotation = switch (facing) {
            case NORTH -> 0;
            case EAST  -> 1;
            case SOUTH -> 2;
            case WEST  -> 3;
            default    -> 0;
        };

        return validateWithRotation(level, controllerPos, rotation);
    }

    private boolean validateWithRotation(Level level, BlockPos controllerPos, int rotation) {

        // 1. Find controller position inside the pattern
        BlockPos controllerIndex = findControllerIndex();
        if (controllerIndex == null) {
            System.out.println("❌ Pattern has no controller 'C'");
            return false;
        }

        int cx = controllerIndex.getX();
        int cy = controllerIndex.getY();
        int cz = controllerIndex.getZ();

        // 2. Loop through pattern
        for (int y = 0; y < pattern.length; y++) {
            for (int z = 0; z < pattern[y].length; z++) {
                for (int x = 0; x < pattern[y][z].length; x++) {

                    Object expected = pattern[y][z][x];
                    if (expected == null) continue;

                    // 3. Compute offset relative to controller
                    int rx = x - cx;
                    int ry = y - cy;
                    int rz = z - cz;

                    // 4. Rotate around controller
                    BlockPos rotated = rotate(rx, ry, rz, rotation);

                    // 5. Convert to world position
                    BlockPos worldPos = controllerPos.offset(rotated);

                    Block actual = level.getBlockState(worldPos).getBlock();

                    if (expected instanceof Character ch) {
                        List<Block> valid = mapping.get(ch);
                        // Accept either the mapped block OR an ARLib placeholder (which stands in for the real block)
                        boolean isPlaceholder = actual == ARLibRegistry.BLOCK_PLACEHOLDER.get();
                        if ((valid == null || !valid.contains(actual)) && !isPlaceholder) {
                            System.out.println("❌ Mismatch at " + worldPos +
                                    " expected '" + ch + "' but found " + actual);
                            return false;
                        }
                    } else if (expected instanceof Block block) {
                        if (actual != block) {
                            System.out.println("❌ Mismatch at " + worldPos +
                                    " expected block " + block + " but found " + actual);
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }
    /**
     * Set the ARLib STATE_MULTIBLOCK_FORMED property on world blocks covered by this pattern.
     * This iterates the pattern (using the controller's facing) and for any block that has the
     * ARLib BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED property it sets that property to
     * the given formed flag.
     */
    public void applyFormedToWorld(Level level, BlockPos controllerPos, boolean formed) {
        if (level == null || controllerPos == null) return;

        Direction facing = level.getBlockState(controllerPos)
                .getValue(MultiblockControllerBlock.FACING);

        int rotation = switch (facing) {
            case NORTH -> 0;
            case EAST  -> 1;
            case SOUTH -> 2;
            case WEST  -> 3;
            default    -> 0;
        };

        BlockPos controllerIndex = findControllerIndex();
        if (controllerIndex == null) return;

        int cx = controllerIndex.getX();
        int cy = controllerIndex.getY();
        int cz = controllerIndex.getZ();

        for (int y = 0; y < pattern.length; y++) {
            for (int z = 0; z < pattern[y].length; z++) {
                for (int x = 0; x < pattern[y][z].length; x++) {
                    Object expected = pattern[y][z][x];
                    if (expected == null) continue;

                    int rx = x - cx;
                    int ry = y - cy;
                    int rz = z - cz;

                    BlockPos rotated = rotate(rx, ry, rz, rotation);
                    BlockPos worldPos = controllerPos.offset(rotated);

                    try {
                        BlockState bs = level.getBlockState(worldPos);
                        if (bs != null && bs.hasProperty(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) {
                            level.setBlock(worldPos, bs.setValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED, formed), 3);
                        }
                    } catch (Exception e) {
                        // Be defensive: if something can't be set, skip it.
                        System.out.println("applyFormedToWorld: failed updating at " + worldPos + " - " + e);
                    }
                }
            }
        }
    }
    private BlockPos findControllerIndex() {
        for (int y = 0; y < pattern.length; y++) {
            for (int z = 0; z < pattern[y].length; z++) {
                for (int x = 0; x < pattern[y][z].length; x++) {
                    if (pattern[y][z][x] instanceof Character ch && ch == 'C') {
                        return new BlockPos(x, y, z);
                    }
                }
            }
        }
        return null;
    }
    private BlockPos rotate(int x, int y, int z, int rotation) {
        return switch (rotation) {
            case 0 -> new BlockPos(-x, y, z);     // NORTH
            case 1 -> new BlockPos(-z, y, -x);    // EAST
            case 2 -> new BlockPos(x, y, -z);     // SOUTH
            case 3 -> new BlockPos(z, y, x);      // WEST
            default -> BlockPos.ZERO;
        };
    }

}
