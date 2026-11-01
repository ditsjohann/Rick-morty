package com.ditsjohann.mayhem.block;

import com.ditsjohann.mayhem.registry.ModEntities;
import com.ditsjohann.mayhem.registry.ModItems;
import com.ditsjohann.mayhem.registry.ModSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Receives broadcasts from infinite realities. Every right-click tunes into a random channel,
 * each with its own (mostly harmless) side effects.
 */
public class InterdimensionalTvBlock extends HorizontalFacingBlock {
    public static final MapCodec<InterdimensionalTvBlock> CODEC = createCodec(InterdimensionalTvBlock::new);
    public static final IntProperty SCREEN = IntProperty.of("screen", 0, 3);
    public static final int CHANNEL_COUNT = 14;
    private static final VoxelShape SHAPE = Block.createCuboidShape(1, 0, 1, 15, 14, 15);

    public InterdimensionalTvBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, net.minecraft.util.math.Direction.NORTH).with(SCREEN, 0));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, SCREEN);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world instanceof ServerWorld serverWorld) {
            int channel = world.random.nextInt(CHANNEL_COUNT);
            world.setBlockState(pos, state.with(SCREEN, channel % 4), Block.NOTIFY_ALL);
            world.playSound(null, pos, ModSounds.TV_CHANNEL, SoundCategory.BLOCKS, 0.8f, 0.9f + world.random.nextFloat() * 0.2f);
            player.sendMessage(Text.literal("📺 ").append(Text.translatable("tv.mayhem.channel", channel + 1).formatted(Formatting.AQUA))
                    .append(Text.literal(" "))
                    .append(Text.translatable("tv.mayhem.broadcast." + channel).formatted(Formatting.WHITE)), false);
            broadcastEffect(serverWorld, pos, player, channel);
        }
        return ActionResult.success(world.isClient);
    }

    private void broadcastEffect(ServerWorld world, BlockPos pos, PlayerEntity player, int channel) {
        Random r = world.random;
        switch (channel) {
            case 0 -> player.giveItemStack(new ItemStack(Items.COOKIE, 1 + r.nextInt(3)));
            case 1 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 200));
            case 2 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 400, 1));
            case 3 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100));
            case 4 -> { /* The brick says nothing. Masterful. */ }
            case 5 -> world.spawnParticles(ParticleTypes.ITEM_SLIME, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 30, 0.5, 0.3, 0.5, 0.1);
            case 6 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 300));
            case 7 -> player.giveItemStack(new ItemStack(ModItems.COOKED_MYSTERY_MEAT, 1));
            case 8 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200));
            case 9 -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 600));
            case 10 -> world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 15, 0.5, 0.3, 0.5, 0.1);
            case 11 -> {
                if (r.nextInt(4) == 0) {
                    ModEntities.THOUGHT_LEECH.spawn(world, pos.up(), SpawnReason.TRIGGERED);
                    player.sendMessage(Text.translatable("tv.mayhem.something_crawled_out").formatted(Formatting.DARK_PURPLE), false);
                }
            }
            case 12 -> {
                var good = new StatusEffectInstance[]{
                        new StatusEffectInstance(StatusEffects.SPEED, 400, 1),
                        new StatusEffectInstance(StatusEffects.HASTE, 400, 1),
                        new StatusEffectInstance(StatusEffects.LUCK, 1200),
                        new StatusEffectInstance(StatusEffects.LEVITATION, 60),
                        new StatusEffectInstance(StatusEffects.NAUSEA, 120)};
                player.addStatusEffect(good[r.nextInt(good.length)]);
            }
            default -> player.addExperience(3);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(5) == 0) {
            var dir = state.get(FACING);
            world.addParticle(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5 + dir.getOffsetX() * 0.55, pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.5, pos.getZ() + 0.5 + dir.getOffsetZ() * 0.55,
                    dir.getOffsetX() * 0.05, 0.02, dir.getOffsetZ() * 0.05);
        }
    }
}
