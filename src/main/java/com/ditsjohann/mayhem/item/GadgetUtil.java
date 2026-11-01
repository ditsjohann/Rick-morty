package com.ditsjohann.mayhem.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

final class GadgetUtil {
    @Nullable
    static LivingEntity raycastLiving(PlayerEntity user, double range) {
        HitResult hit = ProjectileUtil.getCollision(user, e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator() && e != user, range);
        if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity living) return living;
        return null;
    }

    static Vec3d beamEnd(PlayerEntity user, double range) {
        HitResult hit = ProjectileUtil.getCollision(user, e -> e instanceof LivingEntity && e != user, range);
        return hit.getType() == HitResult.Type.MISS ? user.getEyePos().add(user.getRotationVec(1f).multiply(range)) : hit.getPos();
    }

    static void beam(ServerWorld world, Vec3d from, Vec3d to, ParticleEffect particle) {
        Vec3d d = to.subtract(from);
        int n = (int) Math.max(4, d.length() * 3);
        for (int i = 0; i <= n; i++) {
            Vec3d p = from.add(d.multiply(i / (double) n));
            world.spawnParticles(particle, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0);
        }
    }

    static Vec3d muzzle(Entity user) {
        return user.getEyePos().add(user.getRotationVec(1f).multiply(0.6)).add(0, -0.25, 0);
    }

    private GadgetUtil() {
    }
}
