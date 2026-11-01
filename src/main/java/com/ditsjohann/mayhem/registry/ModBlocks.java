package com.ditsjohann.mayhem.registry;

import com.ditsjohann.mayhem.MultiverseMayhem;
import com.ditsjohann.mayhem.block.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.intprovider.UniformIntProvider;

import java.util.ArrayList;
import java.util.List;

public final class ModBlocks {
    public static final List<Block> BLOCKS_WITH_ITEMS = new ArrayList<>();

    // --- Overworld ---
    public static final Block FLUX_CRYSTAL_ORE = register("flux_crystal_ore", new ExperienceDroppingBlock(UniformIntProvider.create(3, 7),
            AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(3.0f, 3.0f).luminance(s -> 4)));
    public static final Block DEEPSLATE_FLUX_CRYSTAL_ORE = register("deepslate_flux_crystal_ore", new ExperienceDroppingBlock(UniformIntProvider.create(3, 7),
            AbstractBlock.Settings.copy(FLUX_CRYSTAL_ORE).mapColor(MapColor.DEEPSLATE_GRAY).strength(4.5f, 3.0f).sounds(BlockSoundGroup.DEEPSLATE)));
    public static final Block FLUX_BLOCK = register("flux_block", new GlowingBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.CYAN).requiresTool().strength(5.0f, 6.0f).sounds(BlockSoundGroup.AMETHYST_BLOCK).luminance(s -> 12),
            ParticleTypes.ELECTRIC_SPARK, 4));
    public static final Block DARK_MATTER_BLOCK = register("dark_matter_block", new GlowingBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.BLACK).requiresTool().strength(25.0f, 1200.0f).sounds(BlockSoundGroup.NETHERITE),
            ParticleTypes.REVERSE_PORTAL, 3));

    // --- Gloopverse ---
    public static final Block GLOOP_BLOCK = register("gloop_block", new GloopBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).slipperiness(0.85f).sounds(BlockSoundGroup.SLIME).nonOpaque().strength(0.4f).luminance(s -> 3)));
    public static final Block GLOOPSTONE = register("gloopstone", new Block(
            AbstractBlock.Settings.create().mapColor(MapColor.TERRACOTTA_PURPLE).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.5f, 6.0f).sounds(BlockSoundGroup.CALCITE)));
    public static final Block GLOOPSTONE_BRICKS = register("gloopstone_bricks", new Block(AbstractBlock.Settings.copy(GLOOPSTONE)));
    public static final Block GLOOP_GRASS = register("gloop_grass", new GlowingBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.LIME).strength(0.8f).sounds(BlockSoundGroup.MOSS_BLOCK), ParticleTypes.WARPED_SPORE, 12));
    public static final Block WOBBLE_SHROOM = register("wobble_shroom", new WobbleShroomBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.PINK).noCollision().breakInstantly().sounds(BlockSoundGroup.FUNGUS).luminance(s -> 10).pistonBehavior(PistonBehavior.DESTROY)));
    public static final Block FLUX_CLUSTER = register("flux_cluster", new CrystalClusterBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.CYAN).noCollision().strength(1.5f).sounds(BlockSoundGroup.AMETHYST_CLUSTER).luminance(s -> 9).pistonBehavior(PistonBehavior.DESTROY).requiresTool()));

    // --- Meatverse ---
    public static final Block FLESH_BLOCK = register("flesh_block", new Block(
            AbstractBlock.Settings.create().mapColor(MapColor.DULL_PINK).strength(0.9f).sounds(BlockSoundGroup.MUD)));
    public static final Block EYE_FLESH = register("eye_flesh", new GlowingBlock(
            AbstractBlock.Settings.copy(FLESH_BLOCK).luminance(s -> 6).sounds(BlockSoundGroup.HONEY), ParticleTypes.CRIMSON_SPORE, 8));
    public static final Block GRISTLE = register("gristle", new Block(
            AbstractBlock.Settings.create().mapColor(MapColor.OFF_WHITE).instrument(NoteBlockInstrument.XYLOPHONE).requiresTool().strength(2.0f, 6.0f).sounds(BlockSoundGroup.BONE)));
    public static final Block DARK_MATTER_ORE = register("dark_matter_ore", new ExperienceDroppingBlock(UniformIntProvider.create(5, 10),
            AbstractBlock.Settings.copy(GRISTLE).strength(5.0f, 9.0f).luminance(s -> 2)));

    // --- Gadgetry ---
    public static final Block INTERDIMENSIONAL_TV = register("interdimensional_tv", new InterdimensionalTvBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.BROWN).strength(1.5f).sounds(BlockSoundGroup.WOOD).luminance(s -> 9).nonOpaque()));
    public static final Block LANDING_PAD = register("landing_pad", new LandingPadBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.LIME).strength(1.0f, 6.0f).sounds(BlockSoundGroup.METAL).luminance(s -> 15)));

    private static Block register(String name, Block block) {
        Block registered = Registry.register(Registries.BLOCK, MultiverseMayhem.id(name), block);
        Registry.register(Registries.ITEM, MultiverseMayhem.id(name), new BlockItem(registered, new Item.Settings()));
        BLOCKS_WITH_ITEMS.add(registered);
        return registered;
    }

    public static void init() {
    }

    private ModBlocks() {
    }
}
