package com.ditsjohann.mayhem.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/** A glowing, jiggling mushroom found on the Gloopverse floating islands. Also generic "weird plant". */
public class WobbleShroomBlock extends PlantBlock {
    public static final MapCodec<WobbleShroomBlock> CODEC = createCodec(WobbleShroomBlock::new);
    private static final VoxelShape SHAPE = net.minecraft.block.Block.createCuboidShape(3, 0, 3, 13, 12, 13);

    public WobbleShroomBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends PlantBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isSideSolidFullSquare(world, pos, net.minecraft.util.math.Direction.UP);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(4) == 0) {
            world.addParticle(ParticleTypes.GLOW, pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6, pos.getY() + 0.7, pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6, 0, 0.02, 0);
        }
    }
}
