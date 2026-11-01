package com.ditsjohann.mayhem.util;

import com.ditsjohann.mayhem.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;

/**
 * Finds somewhere a traveller can stand without immediately suffocating, drowning or burning.
 * If reality refuses to cooperate, it builds an emergency landing pad.
 */
public final class SafeSpotFinder {
    private static final int[] RADII = {0, 3, 6, 10, 16, 24, 32};

    public static BlockPos find(ServerWorld world, BlockPos around) {
        int minY = world.getBottomY() + 1;
        int maxY = world.getBottomY() + world.getDimension().logicalHeight() - 3;
        int startY = MathHelper.clamp(around.getY(), minY, maxY);
        boolean ceiling = world.getDimension().hasCeiling();

        for (int r : RADII) {
            int steps = r == 0 ? 1 : 8;
            for (int i = 0; i < steps; i++) {
                double angle = (Math.PI * 2 * i) / steps;
                int x = around.getX() + (int) Math.round(Math.cos(angle) * r);
                int z = around.getZ() + (int) Math.round(Math.sin(angle) * r);
                world.getChunk(x >> 4, z >> 4); // force generation
                BlockPos found = ceiling ? scanColumn(world, x, z, startY, minY, maxY) : surface(world, x, z, startY, minY, maxY);
                if (found != null) return found;
            }
        }
        return buildPad(world, new BlockPos(around.getX(), MathHelper.clamp(startY < minY + 10 ? 100 : startY, minY + 5, maxY - 5), around.getZ()));
    }

    private static BlockPos surface(ServerWorld world, int x, int z, int startY, int minY, int maxY) {
        int top = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (top > minY && top <= maxY) {
            BlockPos p = new BlockPos(x, top, z);
            if (isStandable(world, p)) return p;
        }
        return scanColumn(world, x, z, startY, minY, maxY);
    }

    private static BlockPos scanColumn(ServerWorld world, int x, int z, int startY, int minY, int maxY) {
        BlockPos.Mutable m = new BlockPos.Mutable();
        for (int d = 0; d < (maxY - minY); d++) {
            int up = startY + d;
            int down = startY - d;
            if (up <= maxY && isStandable(world, m.set(x, up, z))) return m.toImmutable();
            if (down >= minY && isStandable(world, m.set(x, down, z))) return m.toImmutable();
            if (up > maxY && down < minY) break;
        }
        return null;
    }

    public static boolean isStandable(ServerWorld world, BlockPos feet) {
        BlockState below = world.getBlockState(feet.down());
        BlockState at = world.getBlockState(feet);
        BlockState head = world.getBlockState(feet.up());
        if (!below.isSideSolidFullSquare(world, feet.down(), Direction.UP)) return false;
        if (below.isOf(Blocks.MAGMA_BLOCK) || below.isOf(Blocks.BEDROCK) || below.isOf(Blocks.CAMPFIRE)) return false;
        if (!at.getCollisionShape(world, feet).isEmpty() || !head.getCollisionShape(world, feet.up()).isEmpty()) return false;
        if (!at.getFluidState().isEmpty() || !head.getFluidState().isEmpty()) return false;
        return !at.isOf(Blocks.FIRE) && !at.isOf(Blocks.SOUL_FIRE) && !at.isOf(Blocks.POWDER_SNOW);
    }

    /** Builds a 5x5 glowing landing pad with a clear 3-block-high bubble above it. */
    public static BlockPos buildPad(ServerWorld world, BlockPos feet) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                world.setBlockState(feet.add(dx, -1, dz), ModBlocks.LANDING_PAD.getDefaultState());
                for (int dy = 0; dy < 3; dy++) {
                    BlockPos p = feet.add(dx, dy, dz);
                    if (!world.getBlockState(p).isAir()) world.setBlockState(p, Blocks.AIR.getDefaultState());
                }
            }
        }
        return feet;
    }

    private SafeSpotFinder() {
    }
}
