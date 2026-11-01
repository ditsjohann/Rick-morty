package com.ditsjohann.mayhem.entity.mob;

import com.ditsjohann.mayhem.registry.ModEntities;
import com.ditsjohann.mayhem.registry.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

/**
 * A skittering slab of meat with too many legs and far too many eyes.
 * Large ones burst into smaller crawlers when killed.
 */
public class MeatCrawlerEntity extends HostileEntity {
    public MeatCrawlerEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 6;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
                .add(EntityAttributes.GENERIC_ARMOR, 2.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 28.0);
    }

    public static boolean canSpawn(EntityType<MeatCrawlerEntity> type, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        return HostileEntity.canSpawnIgnoreLightLevel(type, world, reason, pos, random);
    }

    public boolean isSmall() {
        return getAttributeBaseValue(EntityAttributes.GENERIC_SCALE) < 0.9;
    }

    public void makeSmall() {
        EntityAttributeInstance scale = getAttributeInstance(EntityAttributes.GENERIC_SCALE);
        if (scale != null) scale.setBaseValue(0.5);
        EntityAttributeInstance hp = getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (hp != null) hp.setBaseValue(8.0);
        EntityAttributeInstance dmg = getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (dmg != null) dmg.setBaseValue(2.5);
        EntityAttributeInstance spd = getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (spd != null) spd.setBaseValue(0.36);
        setHealth(8.0f);
        this.experiencePoints = 2;
    }

    @Override
    protected void initGoals() {
        goalSelector.add(1, new SwimGoal(this));
        goalSelector.add(3, new PounceAtTargetGoal(this, 0.45f));
        goalSelector.add(4, new MeleeAttackGoal(this, 1.25, false));
        goalSelector.add(6, new WanderAroundFarGoal(this, 0.8));
        goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 10.0f));
        goalSelector.add(8, new LookAroundGoal(this));
        targetSelector.add(1, new RevengeGoal(this));
        targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        targetSelector.add(3, new ActiveTargetGoal<>(this, GlooperEntity.class, true));
        targetSelector.add(4, new ActiveTargetGoal<>(this, VillagerEntity.class, true));
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living && random.nextInt(3) == 0) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 140, 0), this);
        }
        return hit;
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (getWorld() instanceof ServerWorld world && !isSmall()) {
            int count = 2 + random.nextInt(2);
            for (int i = 0; i < count; i++) {
                MeatCrawlerEntity baby = ModEntities.MEAT_CRAWLER.create(world);
                if (baby == null) continue;
                baby.refreshPositionAndAngles(getX() + (random.nextDouble() - 0.5), getY() + 0.3, getZ() + (random.nextDouble() - 0.5), random.nextFloat() * 360, 0);
                baby.makeSmall();
                baby.setVelocity((random.nextDouble() - 0.5) * 0.5, 0.35, (random.nextDouble() - 0.5) * 0.5);
                if (getTarget() != null) baby.setTarget(getTarget());
                world.spawnEntity(baby);
            }
        }
    }

    @Override
    public float getSoundPitch() {
        return isSmall() ? 1.5f : super.getSoundPitch();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CRAWLER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CRAWLER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRAWLER_DEATH;
    }
}
