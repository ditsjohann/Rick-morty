package com.ditsjohann.mayhem.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.TranslucentBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Like slime, but with ambition. Bounces you higher than you fell (capped) and never hurts.
 */
public class GloopBlock extends TranslucentBlock {
    public GloopBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (entity.bypassesLandingEffects()) {
            super.onLandedUpon(world, state, pos, entity, fallDistance);
        } else {
            entity.handleFallDamage(fallDistance, 0.0F, world.getDamageSources().fall());
            if (fallDistance > 1.5f) {
                world.playSound(null, pos, SoundEvents.BLOCK_SLIME_BLOCK_FALL, SoundCategory.BLOCKS, 1.0f, 0.6f + world.random.nextFloat() * 0.3f);
            }
        }
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        if (entity.bypassesLandingEffects()) {
            super.onEntityLand(world, entity);
        } else {
            Vec3d v = entity.getVelocity();
            if (v.y < 0.0) {
                double factor = entity instanceof LivingEntity ? 1.15 : 0.9;
                double newY = Math.min(-v.y * factor, 2.2);
                if (newY < 0.25) newY = 0.0;
                entity.setVelocity(v.x * 1.05, newY, v.z * 1.05);
            }
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(6) == 0) {
            world.addParticle(ParticleTypes.ITEM_SLIME, pos.getX() + random.nextDouble(), pos.getY() + 1.02, pos.getZ() + random.nextDouble(), 0, 0.05, 0);
        }
    }
}
