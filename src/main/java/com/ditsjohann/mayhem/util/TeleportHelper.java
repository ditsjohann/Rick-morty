package com.ditsjohann.mayhem.util;

import com.ditsjohann.mayhem.entity.mob.GlooperEntity;
import com.ditsjohann.mayhem.registry.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

import java.util.ArrayList;
import java.util.List;

public final class TeleportHelper {
    public static final int PORTAL_COOLDOWN = 60;

    public static Entity teleport(Entity entity, ServerWorld dest, Vec3d pos, float yaw) {
        Entity moved;
        if (entity instanceof ServerPlayerEntity player) {
            List<Entity> loyalRiders = new ArrayList<>();
            for (Entity passenger : player.getPassengerList()) {
                if (passenger instanceof GlooperEntity) loyalRiders.add(passenger);
            }
            player.teleport(dest, pos.x, pos.y, pos.z, yaw, player.getPitch());
            for (Entity rider : loyalRiders) {
                rider.stopRiding();
                Entity r = rider.teleportTo(new TeleportTarget(dest, pos, Vec3d.ZERO, yaw, 0, TeleportTarget.NO_OP));
                if (r != null) r.setPortalCooldown(PORTAL_COOLDOWN);
            }
            moved = player;
        } else {
            moved = entity.teleportTo(new TeleportTarget(dest, pos, entity.getVelocity(), yaw, entity.getPitch(), TeleportTarget.NO_OP));
        }
        if (moved != null) {
            moved.setPortalCooldown(PORTAL_COOLDOWN);
            dest.playSound(null, pos.x, pos.y, pos.z, ModSounds.PORTAL_TRAVEL, SoundCategory.NEUTRAL, 0.7f, 0.9f + dest.random.nextFloat() * 0.2f);
        }
        return moved;
    }

    private TeleportHelper() {
    }
}
