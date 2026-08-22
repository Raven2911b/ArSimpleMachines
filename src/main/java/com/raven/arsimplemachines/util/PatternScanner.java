package com.raven.arsimplemachines.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;

public class PatternScanner {

    public static void drawScanBox(Level level, BlockPos origin,
                                   int minX, int maxX,
                                   int minY, int maxY,
                                   int minZ, int maxZ) {

        if (level == null || !level.isClientSide) return;

        for (int dx = minX; dx <= maxX; dx++)
            for (int dy = minY; dy <= maxY; dy++)
                for (int dz = minZ; dz <= maxZ; dz++) {

                    boolean edge =
                            dx == minX || dx == maxX ||
                                    dy == minY || dy == maxY ||
                                    dz == minZ || dz == maxZ;

                    if (!edge) continue;

                    BlockPos p = origin.offset(dx, dy, dz);

                    level.addParticle(
                            ParticleTypes.END_ROD,
                            p.getX() + 0.5,
                            p.getY() + 0.5,
                            p.getZ() + 0.5,
                            0, 0, 0
                    );
                }
    }
}
