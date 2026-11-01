package com.ditsjohann.mayhem.world;

import com.ditsjohann.mayhem.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/** Tall, leaning shards of crystallised flux sprouting from the Gloopverse islands. */
public class FluxSpireFeature extends Feature<DefaultFeatureConfig> {
    public FluxSpireFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> ctx) {
        StructureWorldAccess world = ctx.getWorld();
        Random random = ctx.getRandom();
        BlockPos origin = ctx.getOrigin();
        if (!world.getBlockState(origin.down()).isOf(ModBlocks.GLOOP_GRASS) && !world.getBlockState(origin.down()).isOf(ModBlocks.GLOOPSTONE)) return false;
        int height = 5 + random.nextInt(9);
        double leanX = (random.nextDouble() - 0.5) * 0.5;
        double leanZ = (random.nextDouble() - 0.5) * 0.5;
        BlockState flux = ModBlocks.FLUX_BLOCK.getDefaultState();
        BlockState gloop = ModBlocks.GLOOP_BLOCK.getDefaultState();
        for (int y = 0; y < height; y++) {
            double r = 1.6 * (1.0 - (double) y / height) + 0.4;
            int cx = (int) Math.round(leanX * y), cz = (int) Math.round(leanZ * y);
            int ir = (int) Math.ceil(r);
            for (int dx = -ir; dx <= ir; dx++) {
                for (int dz = -ir; dz <= ir; dz++) {
                    if (dx * dx + dz * dz > r * r) continue;
                    BlockPos p = origin.add(cx + dx, y, cz + dz);
                    if (world.isAir(p) || world.getBlockState(p).isReplaceable()) {
                        world.setBlockState(p, (y + dx + dz) % 4 == 0 ? gloop : flux, 2);
                    }
                }
            }
        }
        for (int i = 0; i < 6; i++) {
            BlockPos p = origin.add(random.nextBetween(-3, 3), 0, random.nextBetween(-3, 3));
            if (world.isAir(p) && world.getBlockState(p.down()).isSideSolidFullSquare(world, p.down(), Direction.UP)) {
                world.setBlockState(p, ModBlocks.FLUX_CLUSTER.getDefaultState(), 2);
            }
        }
        return true;
    }
}
