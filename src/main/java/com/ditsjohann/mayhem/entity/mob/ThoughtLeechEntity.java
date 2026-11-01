package com.ditsjohann.mayhem.entity.mob;

import com.ditsjohann.mayhem.registry.ModEffects;
import com.ditsjohann.mayhem.registry.ModSounds;
import com.ditsjohann.mayhem.util.RideHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * A squishy psychic slug that leaps onto your head and sips your thoughts (and experience).
 * Shake it off by rapidly sneaking three times, jumping into water, or portalling away.
 */
public class ThoughtLeechEntity extends HostileEntity {
    private int attachedTicks;
    private int shakeCount;
    private int shakeWindow;
    private boolean lastSneak;
    private int boredTicks;

    public ThoughtLeechEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 4;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 20.0);
    }

    public static boolean canSpawn(EntityType<ThoughtLeechEntity> type, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        return HostileEntity.canSpawnIgnoreLightLevel(type, world, reason, pos, random);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(1, new SwimGoal(this));
        goalSelector.add(2, new LatchGoal(this));
        goalSelector.add(6, new WanderAroundFarGoal(this, 0.8));
        goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        goalSelector.add(8, new LookAroundGoal(this));
        targetSelector.add(1, new RevengeGoal(this));
        targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, e -> boredTicks <= 0));
    }

    public boolean isAttached() {
        return getVehicle() instanceof PlayerEntity;
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) return;
        if (boredTicks > 0) boredTicks--;
        if (getVehicle() instanceof ServerPlayerEntity player) {
            attachedTicks++;
            player.addStatusEffect(new StatusEffectInstance(ModEffects.BRAIN_DRAIN, 60, 0, false, true), this);
            if (attachedTicks % 100 == 1) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 120, 0), this);
            }
            if (attachedTicks % 50 == 0) {
                player.damage(getDamageSources().mobAttack(this), 1.0f);
                playSound(ModSounds.LEECH_AMBIENT, 0.8f, 1.3f);
            }
            boolean sneak = player.isSneaking();
            if (sneak && !lastSneak) {
                shakeCount++;
                shakeWindow = 50;
            }
            lastSneak = sneak;
            if (shakeWindow > 0 && --shakeWindow == 0) shakeCount = 0;

            boolean shaken = shakeCount >= 3;
            if (shaken || player.isTouchingWater() || attachedTicks > 800 || player.isCreative() || player.isSpectator()) {
                RideHelper.dismount(this);
                setPosition(player.getX(), player.getY() + 1.9, player.getZ());
                setVelocity((random.nextDouble() - 0.5) * 0.9, 0.4, (random.nextDouble() - 0.5) * 0.9);
                velocityModified = true;
                boredTicks = 160;
                setTarget(null);
                if (shaken) {
                    damage(getDamageSources().generic(), 4.0f);
                    player.sendMessage(Text.translatable("message.mayhem.leech_shaken").formatted(Formatting.GREEN), true);
                }
                ((ServerWorld) getWorld()).spawnParticles(ParticleTypes.SQUID_INK, getX(), getY(), getZ(), 10, 0.2, 0.2, 0.2, 0.05);
                attachedTicks = 0;
                shakeCount = 0;
            }
        } else {
            attachedTicks = 0;
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        // Being on someone's head means fall and suffocation damage are not your problem.
        if (isAttached() && (source.isOf(net.minecraft.entity.damage.DamageTypes.IN_WALL) || source.isOf(net.minecraft.entity.damage.DamageTypes.FALL))) return true;
        return super.isInvulnerableTo(source);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.LEECH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CRAWLER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRAWLER_DEATH;
    }

    @Override
    public float getSoundPitch() {
        return 1.6f + random.nextFloat() * 0.2f;
    }

    /** Scuttle towards the target and leap onto their head. */
    static class LatchGoal extends Goal {
        private final ThoughtLeechEntity leech;
        private int leapCooldown;

        LatchGoal(ThoughtLeechEntity leech) {
            this.leech = leech;
            setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
        }

        @Override
        public boolean canStart() {
            LivingEntity t = leech.getTarget();
            return t instanceof PlayerEntity && t.isAlive() && !leech.hasVehicle() && leech.boredTicks <= 0;
        }

        @Override
        public boolean shouldContinue() {
            return canStart();
        }

        @Override
        public void tick() {
            LivingEntity t = leech.getTarget();
            if (t == null) return;
            leech.getLookControl().lookAt(t, 30, 30);
            leech.getNavigation().startMovingTo(t, 1.3);
            if (leapCooldown > 0) leapCooldown--;
            double d = leech.squaredDistanceTo(t);
            if (d < 9 && leech.isOnGround() && leapCooldown <= 0) {
                leapCooldown = 25;
                var dir = t.getPos().subtract(leech.getPos()).normalize();
                leech.setVelocity(dir.x * 0.6, 0.55, dir.z * 0.6);
            }
            if (d < 2.2 && t instanceof ServerPlayerEntity sp && !sp.isCreative() && !sp.isSpectator()) {
                if (RideHelper.mountOnHead(leech, sp)) {
                    leech.playSound(ModSounds.LEECH_ATTACH, 1.0f, 1.0f);
                    sp.sendMessage(Text.translatable("message.mayhem.leech_attached").formatted(Formatting.DARK_PURPLE), true);
                }
            }
        }
    }
}
