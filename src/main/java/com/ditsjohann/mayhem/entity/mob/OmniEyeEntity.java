package com.ditsjohann.mayhem.entity.mob;

import com.ditsjohann.mayhem.entity.projectile.EyeBeamEntity;
import com.ditsjohann.mayhem.registry.ModEntities;
import com.ditsjohann.mayhem.registry.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;

/**
 * THE OMNI-EYE. Ancient overseer of the Meatverse. Two phases:
 * 1) beam volleys and crawler summons; 2) (below half health) it gets angry, blinks around,
 * drags you towards its pupil with a crushing gaze and calls Watchers to its side.
 */
public class OmniEyeEntity extends HostileEntity {
    private static final TrackedData<Boolean> ENRAGED = DataTracker.registerData(OmniEyeEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private final ServerBossBar bossBar = (ServerBossBar) new ServerBossBar(Text.translatable("entity.mayhem.omni_eye").formatted(Formatting.LIGHT_PURPLE),
            BossBar.Color.PURPLE, BossBar.Style.NOTCHED_10).setDarkenSky(true).setThickenFog(true);

    public OmniEyeEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.moveControl = new FlightMoveControl(this, 10, true);
        this.setNoGravity(true);
        this.experiencePoints = 120;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 360.0)
                .add(EntityAttributes.GENERIC_ARMOR, 10.0)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 4.0)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.55)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ENRAGED, false);
    }

    public boolean isEnraged() {
        return dataTracker.get(ENRAGED);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation nav = new BirdNavigation(this, world);
        nav.setCanPathThroughDoors(false);
        nav.setCanSwim(true);
        return nav;
    }

    @Override
    protected void initGoals() {
        goalSelector.add(1, new OmniAttackGoal(this));
        goalSelector.add(5, new FlyGoal(this, 1.0));
        goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 32.0f));
        targetSelector.add(1, new RevengeGoal(this));
        targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        bossBar.setPercent(getHealth() / getMaxHealth());
        if (!isEnraged() && getHealth() < getMaxHealth() * 0.5f) {
            dataTracker.set(ENRAGED, true);
            bossBar.setColor(BossBar.Color.RED);
            playSound(ModSounds.OMNI_ROAR, 4.0f, 0.7f);
            ServerWorld world = (ServerWorld) getWorld();
            world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1.3, getZ(), 1, 0, 0, 0, 0);
            world.spawnParticles(ParticleTypes.CRIMSON_SPORE, getX(), getY() + 1.3, getZ(), 200, 2, 2, 2, 0.3);
            for (ServerPlayerEntity p : world.getPlayers(p -> p.squaredDistanceTo(this) < 48 * 48)) {
                p.sendMessage(Text.translatable("message.mayhem.omni_enraged").formatted(Formatting.DARK_RED, Formatting.BOLD), false);
            }
            addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 2));
        }
        // Crushing contact damage.
        if (age % 20 == 0) {
            for (LivingEntity e : getWorld().getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(0.8), e -> e != this && !(e instanceof OmniEyeEntity) && !(e instanceof WatcherEntity) && !(e instanceof MeatCrawlerEntity))) {
                tryAttack(e);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient && random.nextInt(3) == 0) {
            getWorld().addParticle(isEnraged() ? ParticleTypes.CRIMSON_SPORE : ParticleTypes.REVERSE_PORTAL,
                    getParticleX(1.2), getRandomBodyY(), getParticleZ(1.2), 0, -0.05, 0);
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.isOf(DamageTypes.IN_WALL) || source.isOf(DamageTypes.DROWN) || source.isOf(DamageTypes.FALL) || source.isOf(DamageTypes.CACTUS) || super.isInvulnerableTo(source);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, net.minecraft.block.BlockState state, BlockPos landedPosition) {
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public boolean canUsePortals(boolean allowVehicles) {
        return false;
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void setCustomName(Text name) {
        super.setCustomName(name);
        bossBar.setName(getDisplayName());
    }

    @Override
    protected void updatePostDeath() {
        super.updatePostDeath();
        if (getWorld() instanceof ServerWorld sw && deathTime % 3 == 0) {
            sw.spawnParticles(ParticleTypes.EXPLOSION, getParticleX(1.5), getRandomBodyY(), getParticleZ(1.5), 1, 0, 0, 0, 0);
            sw.spawnParticles(ParticleTypes.CRIMSON_SPORE, getX(), getY() + 1.3, getZ(), 20, 1, 1, 1, 0.2);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.OMNI_ROAR;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.OMNI_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.OMNI_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 3.0f;
    }

    @Override
    public int getMinAmbientSoundDelay() {
        return 200;
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        dataTracker.set(ENRAGED, nbt.getBoolean("Enraged"));
        if (hasCustomName()) bossBar.setName(getDisplayName());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("Enraged", isEnraged());
    }

    static class OmniAttackGoal extends Goal {
        private final OmniEyeEntity eye;
        private int volley = 40;
        private int summon = 200;
        private int gaze = 240;
        private int blink = 160;
        private float orbit;

        OmniAttackGoal(OmniEyeEntity eye) {
            this.eye = eye;
            setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity t = eye.getTarget();
            return t != null && t.isAlive();
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity t = eye.getTarget();
            if (t == null) return;
            ServerWorld world = (ServerWorld) eye.getWorld();
            boolean rage = eye.isEnraged();
            orbit += rage ? 0.035f : 0.02f;
            double r = rage ? 7 : 10;
            Vec3d goal = t.getPos().add(MathHelper.cos(orbit) * r, 5 + MathHelper.sin(orbit * 3) * 1.5, MathHelper.sin(orbit) * r);
            eye.getMoveControl().moveTo(goal.x, goal.y, goal.z, 1.0);
            eye.getLookControl().lookAt(t, 20, 20);

            if (--volley <= 0) {
                volley = rage ? 22 : 40;
                int shots = rage ? 5 : 3;
                for (int i = 0; i < shots; i++) {
                    EyeBeamEntity beam = new EyeBeamEntity(world, eye, rage ? 9f : 7f);
                    beam.setPosition(eye.getX(), eye.getY() + 1.3, eye.getZ());
                    Vec3d d = t.getPos().add(0, t.getHeight() * 0.5, 0).subtract(beam.getPos());
                    beam.setVelocity(d.x, d.y, d.z, 1.5f, 6.0f + i * 2.0f);
                    world.spawnEntity(beam);
                }
                eye.playSound(ModSounds.OMNI_LASER, 2.0f, rage ? 0.8f : 1.0f);
            }
            if (--summon <= 0) {
                summon = rage ? 260 : 320;
                int nearby = world.getEntitiesByClass(HostileEntity.class, eye.getBoundingBox().expand(24), e -> e instanceof MeatCrawlerEntity || e instanceof WatcherEntity).size();
                if (nearby < 6) {
                    for (int i = 0; i < 2; i++) {
                        BlockPos p = t.getBlockPos().add(eye.random.nextBetween(-5, 5), 1, eye.random.nextBetween(-5, 5));
                        Entity minion = (rage ? ModEntities.WATCHER : ModEntities.MEAT_CRAWLER).spawn(world, p, SpawnReason.MOB_SUMMONED);
                        if (minion instanceof HostileEntity h) h.setTarget(t);
                        world.spawnParticles(ParticleTypes.REVERSE_PORTAL, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 40, 0.4, 0.6, 0.4, 0.3);
                    }
                    eye.playSound(ModSounds.OMNI_ROAR, 3.0f, 1.3f);
                }
            }
            if (rage && --gaze <= 0) {
                gaze = 200;
                eye.playSound(ModSounds.OMNI_ROAR, 4.0f, 0.5f);
                List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, eye.getBoundingBox().expand(24), p -> !p.isCreative() && !p.isSpectator());
                for (PlayerEntity p : players) {
                    Vec3d pull = eye.getPos().subtract(p.getPos()).normalize().multiply(1.6);
                    p.setVelocity(pull.x, Math.max(0.4, pull.y), pull.z);
                    p.velocityModified = true;
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 80, 0), eye);
                    p.sendMessage(Text.translatable("message.mayhem.omni_gaze").formatted(Formatting.RED), true);
                }
            }
            if (rage && --blink <= 0) {
                blink = 140 + eye.random.nextInt(60);
                world.spawnParticles(ParticleTypes.REVERSE_PORTAL, eye.getX(), eye.getY() + 1.3, eye.getZ(), 80, 1, 1, 1, 0.4);
                Vec3d to = t.getPos().add(eye.random.nextBetween(-10, 10), 4 + eye.random.nextInt(4), eye.random.nextBetween(-10, 10));
                if (world.isSpaceEmpty(eye, eye.getBoundingBox().offset(to.subtract(eye.getPos())))) {
                    eye.requestTeleport(to.x, to.y, to.z);
                    world.playSound(null, to.x, to.y, to.z, ModSounds.PORTAL_TRAVEL, SoundCategory.HOSTILE, 2.0f, 0.6f);
                }
            }
        }
    }
}
