package com.raven.arsimplemachines.multiblock;

import ARLib.ARLibRegistry;
import com.raven.arsimplemachines.registry.ModBlocks;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;

public class LathePattern extends MultiblockPattern {

    public static final Object[][][] PATTERN = {
            {}, // ARLib’s phantom top layer
            { { 'E', 'S', 'S', 'I' } },
            { { 'C', 'M', null, 'O' } }
    };

    public static Object[][][] flipY(Object[][][] pattern) {
        Object[][][] flipped = new Object[pattern.length][][];
        for (int y = 0; y < pattern.length; y++) {
            flipped[y] = pattern[pattern.length - 1 - y];
        }
        return flipped;
    }

    public static final Map<Character, List<Block>> MAPPING = Map.of(
            'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
            'S', List.of(ARLibRegistry.BLOCK_STRUCTURE.get()),
            'I', List.of(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get()),
            'O', List.of(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get()),
            'M', List.of(ARLibRegistry.BLOCK_MOTOR.get()),
            'C', List.of(ModBlocks.LATHE_CONTROLLER.get())
    );

    public LathePattern() {
        super(PATTERN, MAPPING);
    }
}
