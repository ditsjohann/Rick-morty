package com.ditsjohann.mayhem.registry;

import com.ditsjohann.mayhem.MultiverseMayhem;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public final class ModDimensions {
    public static final RegistryKey<World> GLOOPVERSE = RegistryKey.of(RegistryKeys.WORLD, MultiverseMayhem.id("gloopverse"));
    public static final RegistryKey<World> MEATVERSE = RegistryKey.of(RegistryKeys.WORLD, MultiverseMayhem.id("meatverse"));

    private ModDimensions() {
    }
}
