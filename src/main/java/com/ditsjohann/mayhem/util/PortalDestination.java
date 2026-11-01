package com.ditsjohann.mayhem.util;

import com.ditsjohann.mayhem.registry.ModDimensions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** Destinations a portal gun can be dialled to. */
public enum PortalDestination {
    HOME("home", null, Formatting.GREEN, false),
    OVERWORLD("overworld", World.OVERWORLD, Formatting.DARK_GREEN, false),
    NETHER("nether", World.NETHER, Formatting.RED, false),
    GLOOPVERSE("gloopverse", ModDimensions.GLOOPVERSE, Formatting.LIGHT_PURPLE, false),
    MEATVERSE("meatverse", ModDimensions.MEATVERSE, Formatting.DARK_RED, false),
    SCRAMBLE("scramble", null, Formatting.GOLD, false),
    END("end", World.END, Formatting.YELLOW, true);

    public final String key;
    @Nullable
    public final RegistryKey<World> world;
    public final Formatting color;
    public final boolean omniOnly;

    PortalDestination(String key, @Nullable RegistryKey<World> world, Formatting color, boolean omniOnly) {
        this.key = key;
        this.world = world;
        this.color = color;
        this.omniOnly = omniOnly;
    }

    public Text displayName() {
        return Text.translatable("destination.mayhem." + key).formatted(color);
    }

    public static PortalDestination byIndex(int i) {
        PortalDestination[] v = values();
        return v[Math.floorMod(i, v.length)];
    }

    public PortalDestination next(boolean omni) {
        PortalDestination d = this;
        do {
            d = byIndex(d.ordinal() + 1);
        } while (d.omniOnly && !omni);
        return d;
    }
}
