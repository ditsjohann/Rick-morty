package com.ditsjohann.mayhem.world;

import com.ditsjohann.mayhem.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/** Wriggling columns of meat that reach from the cavern floor towards the ceiling, studded with eyes. */
public class FleshTendrilFeature extends Feature<DefaultFeatureConfig> {
    public FleshTendrilFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> ctx) {
        StructureWorldAccess world = ctx.getWorld();
        Random random = ctx.getRandom();
        BlockPos origin = ctx.getOrigin();
        // Find a floor: air with solid below.
        BlockPos.Mutable m = origin.mutableCopy();
        int guard = 0;
        while (!world.isAir(m) && guard++ < 40) m.move(0, 1, 0);
        while (world.isAir(m.down()) && guard++ < 80 && m.getY() > world.getBottomY() + 2) m.move(0, -1, 0);
        if (!world.isAir(m) || world.isAir(m.down())) return false;
        BlockPos floor = m.toImmutable();
        int maxH = 10 + random.nextInt(22);
        float phase = random.nextFloat() * 6.28f;
        float amp = 1.0f + random.nextFloat() * 1.8f;
        BlockState flesh = ModBlocks.FLESH_BLOCK.getDefaultState();
        BlockState eye = ModBlocks.EYE_FLESH.getDefaultState();
        int placed = 0;
        for (int y = 0; y < maxH; y++) {
            int ox = Math.round(MathHelper.sin(phase + y * 0.35f) * amp);
            int oz = Math.round(MathHelper.cos(phase * 1.3f + y * 0.27f) * amp);
            float r = y < 2 ? 1.6f : 0.9f + 0.4f * MathHelper.sin(y * 0.8f);
            int ir = (int) Math.ceil(r);
            boolean hitCeiling = false;
            for (int dx = -ir; dx <= ir; dx++) {
                for (int dz = -ir; dz <= ir; dz++) {
                    if (dx * dx + dz * dz > r * r) continue;
                    BlockPos p = floor.add(ox + dx, y, oz + dz);
                    if (world.isAir(p)) {
                        world.setBlockState(p, random.nextInt(9) == 0 ? eye : flesh, 2);
                        placed++;
                    } else if (y > 3 && dx == 0 && dz == 0) {
                        hitCeiling = true;
                    }
                }
            }
            if (hitCeiling) break;
        }
        return placed > 0;
    }
}
