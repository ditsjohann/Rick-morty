package com.ditsjohann.mayhem.entity.mob;

import com.ditsjohann.mayhem.entity.projectile.EyeBeamEntity;
import com.ditsjohann.mayhem.registry.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

import java.util.EnumSet;

/** A floating eyeball with dangling nerve tentacles. It stares. Then it shoots. */
public class WatcherEntity extends HostileEntity {
    public WatcherEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.moveControl = new FlightMoveControl(this, 20, true);
        this.setNoGravity(true);
        this.experiencePoints = 8;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.5)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0);
    }

    public static boolean canSpawn(EntityType<WatcherEntity> type, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        return HostileEntity.canSpawnIgnoreLightLevel(type, world, reason, pos, random) && random.nextInt(3) == 0;
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation nav = new BirdNavigation(this, world);
        nav.setCanPathThroughDoors(false);
        nav.setCanSwim(true);
        nav.setCanEnterOpenDoors(true);
        return nav;
    }

    @Override
    protected void initGoals() {
        goalSelector.add(2, new HoverAndShootGoal(this));
        goalSelector.add(5, new FlyGoal(this, 1.0));
        goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 16.0f));
        goalSelector.add(8, new LookAroundGoal(this));
        targetSelector.add(1, new RevengeGoal(this));
        targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, net.minecraft.block.BlockState state, BlockPos landedPosition) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.WATCHER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WATCHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WATCHER_DEATH;
    }

    /** Circles the target at a respectful staring distance and fires eye beams. */
    static class HoverAndShootGoal extends Goal {
        private final WatcherEntity watcher;
        private int cooldown;
        private float orbit;

        HoverAndShootGoal(WatcherEntity watcher) {
            this.watcher = watcher;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity t = watcher.getTarget();
            return t != null && t.isAlive();
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void start() {
            cooldown = 30;
            orbit = watcher.random.nextFloat() * 6.28f;
        }

        @Override
        public void tick() {
            LivingEntity t = watcher.getTarget();
            if (t == null) return;
            orbit += 0.03f;
            double radius = 6.0;
            Vec3d goal = t.getPos().add(MathHelper.cos(orbit) * radius, 3.0 + MathHelper.sin(orbit * 2) * 0.8, MathHelper.sin(orbit) * radius);
            watcher.getMoveControl().moveTo(goal.x, goal.y, goal.z, 1.0);
            watcher.getLookControl().lookAt(t, 30f, 30f);
            if (--cooldown <= 0 && watcher.canSee(t) && watcher.squaredDistanceTo(t) < 24 * 24) {
                cooldown = 35 + watcher.random.nextInt(20);
                shoot(watcher, t, 5.0f, 1.3f);
            }
        }
    }

    static void shoot(LivingEntity shooter, LivingEntity target, float damage, float speed) {
        EyeBeamEntity beam = new EyeBeamEntity(shooter.getWorld(), shooter, damage);
        beam.setPosition(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
        double dx = target.getX() - shooter.getX();
        double dy = target.getBodyY(0.5) - beam.getY();
        double dz = target.getZ() - shooter.getZ();
        beam.setVelocity(dx, dy, dz, speed, 2.0f);
        shooter.getWorld().spawnEntity(beam);
        shooter.playSound(ModSounds.WATCHER_SHOOT, 1.0f, 0.9f + shooter.getRandom().nextFloat() * 0.2f);
    }
}
