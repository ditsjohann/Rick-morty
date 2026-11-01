package com.ditsjohann.mayhem.registry;

import com.ditsjohann.mayhem.MultiverseMayhem;
import com.ditsjohann.mayhem.world.CrashedSaucerFeature;
import com.ditsjohann.mayhem.world.FleshTendrilFeature;
import com.ditsjohann.mayhem.world.FluxSpireFeature;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;

public final class ModFeatures {
    public static final Feature<DefaultFeatureConfig> CRASHED_SAUCER = Registry.register(Registries.FEATURE, MultiverseMayhem.id("crashed_saucer"),
            new CrashedSaucerFeature(DefaultFeatureConfig.CODEC));
    public static final Feature<DefaultFeatureConfig> FLUX_SPIRE = Registry.register(Registries.FEATURE, MultiverseMayhem.id("flux_spire"),
            new FluxSpireFeature(DefaultFeatureConfig.CODEC));
    public static final Feature<DefaultFeatureConfig> FLESH_TENDRIL = Registry.register(Registries.FEATURE, MultiverseMayhem.id("flesh_tendril"),
            new FleshTendrilFeature(DefaultFeatureConfig.CODEC));

    public static final RegistryKey<PlacedFeature> FLUX_ORE_PLACED = placed("ore_flux_crystal");
    public static final RegistryKey<PlacedFeature> FLUX_ORE_BURIED_PLACED = placed("ore_flux_crystal_buried");
    public static final RegistryKey<PlacedFeature> CRASHED_SAUCER_PLACED = placed("crashed_saucer");

    private static RegistryKey<PlacedFeature> placed(String name) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, MultiverseMayhem.id(name));
    }

    public static void init() {
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, FLUX_ORE_PLACED);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, FLUX_ORE_BURIED_PLACED);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld().and(BiomeSelectors.tag(net.minecraft.registry.tag.BiomeTags.IS_OCEAN).negate())
                        .and(BiomeSelectors.tag(net.minecraft.registry.tag.BiomeTags.IS_RIVER).negate()),
                GenerationStep.Feature.SURFACE_STRUCTURES, CRASHED_SAUCER_PLACED);
    }

    private ModFeatures() {
    }
}
