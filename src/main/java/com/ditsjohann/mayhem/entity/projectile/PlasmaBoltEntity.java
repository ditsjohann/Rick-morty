package com.ditsjohann.mayhem.entity.projectile;

import com.ditsjohann.mayhem.registry.ModEntities;
import com.ditsjohann.mayhem.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/** Superheated flux plasma. Burns, knocks back, sizzles. */
public class PlasmaBoltEntity extends ThrownItemEntity {
    public PlasmaBoltEntity(EntityType<? extends PlasmaBoltEntity> type, World world) {
        super(type, world);
    }

    public PlasmaBoltEntity(World world, LivingEntity owner) {
        super(ModEntities.PLASMA_BOLT, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.PLASMA_ORB;
    }

    @Override
    protected double getGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) {
            getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 0, 0, 0);
            getWorld().addParticle(ParticleTypes.GLOW, getX(), getY(), getZ(), 0, 0, 0);
        } else if (age > 60) {
            discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        var target = hit.getEntity();
        target.damage(getDamageSources().thrown(this, getOwner()), 7.0f);
        target.setOnFireFor(3);
        if (target instanceof LivingEntity living) {
            living.takeKnockback(0.6, -getVelocity().x, -getVelocity().z);
        }
    }

    @Override
    protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 14, 0.2, 0.2, 0.2, 0.3);
            sw.spawnParticles(ParticleTypes.FLASH, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
            sw.playSound(null, getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.NEUTRAL, 0.5f, 1.8f);
            discard();
        }
    }
}
