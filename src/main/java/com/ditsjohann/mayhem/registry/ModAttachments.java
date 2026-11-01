package com.ditsjohann.mayhem.registry;

import com.ditsjohann.mayhem.MultiverseMayhem;
import com.ditsjohann.mayhem.registry.ModItems;
import com.ditsjohann.mayhem.util.InvUtil;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.network.ServerPlayerEntity;

/** Per-player persistent data. */
@SuppressWarnings("UnstableApiUsage")
public final class ModAttachments {
    /** How suspicious Interdimensional Customs is of this player. */
    public static final AttachmentType<Integer> PORTAL_HEAT = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).initializer(() -> 0).copyOnDeath().buildAndRegister(MultiverseMayhem.id("portal_heat"));
    /** Whether the player has received their Lab Notes. */
    public static final AttachmentType<Boolean> GOT_NOTES = AttachmentRegistry.<Boolean>builder()
            .persistent(Codec.BOOL).initializer(() -> false).copyOnDeath().buildAndRegister(MultiverseMayhem.id("got_notes"));

    public static int getHeat(ServerPlayerEntity p) {
        return p.getAttachedOrElse(PORTAL_HEAT, 0);
    }

    public static void setHeat(ServerPlayerEntity p, int v) {
        p.setAttached(PORTAL_HEAT, Math.max(0, v));
    }

    public static void addPortalHeat(ServerPlayerEntity p, int amount) {
        if (p.isCreative() || InvUtil.has(p, ModItems.DIMENSIONAL_PERMIT)) return;
        setHeat(p, getHeat(p) + amount);
    }

    public static void init() {
    }

    private ModAttachments() {
    }
}
