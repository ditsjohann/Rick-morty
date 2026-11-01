package com.ditsjohann.mayhem.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** A plain block that occasionally emits a particle above it. */
public class GlowingBlock extends Block {
    private final ParticleEffect particle;
    private final int chance;

    public GlowingBlock(Settings settings, ParticleEffect particle, int chance) {
        super(settings);
        this.particle = particle;
        this.chance = chance;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(chance) == 0 && !world.getBlockState(pos.up()).isOpaqueFullCube(world, pos.up())) {
            world.addParticle(particle, pos.getX() + random.nextDouble(), pos.getY() + 1.05, pos.getZ() + random.nextDouble(), 0, 0.01, 0);
        }
    }
}
