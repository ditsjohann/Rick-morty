package com.ditsjohann.mayhem.entity.projectile;

import com.ditsjohann.mayhem.registry.ModEntities;
import com.ditsjohann.mayhem.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.joml.Vector3f;

/** A concentrated glare. Fired by Watchers and the Omni-Eye. */
public class EyeBeamEntity extends ThrownItemEntity {
    private float damage = 5.0f;

    public EyeBeamEntity(EntityType<? extends EyeBeamEntity> type, World world) {
        super(type, world);
    }

    public EyeBeamEntity(World world, LivingEntity owner, float damage) {
        super(ModEntities.EYE_BEAM, owner, world);
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.EYE_BEAM_ORB;
    }

    @Override
    protected double getGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) {
            getWorld().addParticle(new DustParticleEffect(new Vector3f(1f, 0.2f, 0.25f), 1.4f), getX(), getY(), getZ(), 0, 0, 0);
        } else if (age > 100) {
            discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        if (hit.getEntity() == getOwner()) return;
        if (hit.getEntity().damage(getDamageSources().mobProjectile(this, getOwner() instanceof LivingEntity l ? l : null), damage)
                && hit.getEntity() instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 50, 1));
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 100, 0));
        }
    }

    @Override
    protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.CRIMSON_SPORE, getX(), getY(), getZ(), 12, 0.25, 0.25, 0.25, 0.05);
            discard();
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putFloat("BeamDamage", damage);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        damage = nbt.getFloat("BeamDamage");
    }
}
