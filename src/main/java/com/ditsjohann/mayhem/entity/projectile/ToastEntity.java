package com.ditsjohann.mayhem.entity.projectile;

import com.ditsjohann.mayhem.registry.ModEntities;
import com.ditsjohann.mayhem.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/** Burnt toast, launched at ballistic velocity by a Defense Toaster. */
public class ToastEntity extends ThrownItemEntity {
    public ToastEntity(EntityType<? extends ToastEntity> type, World world) {
        super(type, world);
    }

    public ToastEntity(World world, LivingEntity owner) {
        super(ModEntities.TOAST, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BURNT_TOAST;
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        hit.getEntity().damage(getDamageSources().thrown(this, getOwner()), 5.0f);
        hit.getEntity().setOnFireFor(2);
    }

    @Override
    protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(ModItems.BURNT_TOAST)), getX(), getY(), getZ(), 8, 0.1, 0.1, 0.1, 0.1);
            sw.spawnParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 4, 0.1, 0.1, 0.1, 0.02);
            discard();
        }
    }
}
