package com.ditsjohann.mayhem.world;

import com.ditsjohann.mayhem.MultiverseMayhem;
import com.ditsjohann.mayhem.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * A small flying saucer that misjudged a landing: tilted hull, cracked glass dome,
 * scorched earth and a loot chest full of interdimensional salvage.
 */
public class CrashedSaucerFeature extends Feature<DefaultFeatureConfig> {
    public static final RegistryKey<LootTable> LOOT = RegistryKey.of(RegistryKeys.LOOT_TABLE, MultiverseMayhem.id("chests/crashed_saucer"));

    public CrashedSaucerFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> ctx) {
        StructureWorldAccess world = ctx.getWorld();
        Random random = ctx.getRandom();
        BlockPos origin = world.getTopPosition(Heightmap.Type.WORLD_SURFACE_WG, ctx.getOrigin());
        BlockState ground = world.getBlockState(origin.down());
        if (!ground.isSolidBlock(world, origin.down()) || ground.isIn(BlockTags.LEAVES) || !world.getFluidState(origin).isEmpty()) return false;

        double tiltX = (random.nextDouble() - 0.5) * 0.6;
        double tiltZ = (random.nextDouble() - 0.5) * 0.6;
        BlockPos base = origin.down(1);

        // Scorch marks
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 8 || random.nextFloat() > 0.65f - d * 0.05f) continue;
                BlockPos top = world.getTopPosition(Heightmap.Type.WORLD_SURFACE_WG, origin.add(dx, 0, dz)).down();
                BlockState s = world.getBlockState(top);
                if (s.isIn(BlockTags.DIRT) || s.isIn(BlockTags.SAND)) {
                    world.setBlockState(top, random.nextInt(3) == 0 ? Blocks.BLACKSTONE.getDefaultState() : Blocks.COARSE_DIRT.getDefaultState(), 2);
                }
            }
        }

        // Hull
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 5.5) continue;
                int ty = (int) Math.round(dx * tiltX + dz * tiltZ);
                BlockPos p = base.add(dx, ty, dz);
                BlockState hull;
                if (d > 4.5) {
                    hull = (Math.abs(dx) + Math.abs(dz)) % 3 == 0 ? ModBlocks.FLUX_BLOCK.getDefaultState() : Blocks.IRON_BLOCK.getDefaultState();
                } else {
                    hull = random.nextInt(10) == 0 ? Blocks.CRACKED_STONE_BRICKS.getDefaultState() : Blocks.LIGHT_GRAY_CONCRETE.getDefaultState();
                }
                world.setBlockState(p, hull, 2);
                if (d <= 4) world.setBlockState(p.down(), Blocks.IRON_BLOCK.getDefaultState(), 2);
                if (d <= 2) world.setBlockState(p.down(2), ModBlocks.LANDING_PAD.getDefaultState(), 2);
                // Dome
                if (d <= 3.4) {
                    int h = (int) Math.floor(Math.sqrt(3.4 * 3.4 - d * d));
                    for (int y = 1; y <= h; y++) {
                        BlockPos q = p.up(y);
                        boolean shell = y == h || d > 2.5;
                        if (shell) {
                            if (random.nextInt(5) != 0) world.setBlockState(q, Blocks.LIME_STAINED_GLASS.getDefaultState(), 2);
                            else world.setBlockState(q, Blocks.AIR.getDefaultState(), 2);
                        } else {
                            world.setBlockState(q, Blocks.AIR.getDefaultState(), 2);
                        }
                    }
                }
                // Clear space above the rim
                for (int y = 1; y <= 2 && d > 3.4; y++) {
                    BlockPos q = p.up(y);
                    if (!world.getBlockState(q).isAir() && !world.getBlockState(q).isOpaqueFullCube(world, q)) world.setBlockState(q, Blocks.AIR.getDefaultState(), 2);
                }
            }
        }

        // Cockpit contents
        BlockPos chest = base.up(1);
        world.setBlockState(chest, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.Type.HORIZONTAL.random(random)), 2);
        LootableInventory.setLootTable(world, random, chest, LOOT);
        world.setBlockState(chest.add(1, 0, 0), ModBlocks.INTERDIMENSIONAL_TV.getDefaultState(), 2);
        if (random.nextBoolean()) world.setBlockState(chest.add(-1, 0, 1), ModBlocks.GLOOP_BLOCK.getDefaultState(), 2);
        return true;
    }
}
