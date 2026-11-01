package com.ditsjohann.mayhem.entity;

import com.ditsjohann.mayhem.registry.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A pocket singularity. Drags everything nearby into its maw for a few seconds,
 * then collapses with a bang and spits out whatever it swallowed.
 */
public class BlackHoleEntity extends Entity {
    public static final int LIFETIME = 140;
    private static final double RANGE = 10.0;
    @Nullable
    private UUID ownerUuid;
    private final List<ItemStack> swallowed = new ArrayList<>();

    public BlackHoleEntity(EntityType<? extends BlackHoleEntity> type, World world) {
        super(type, world);
        this.noClip = true;
    }

    public void setOwner(@Nullable Entity owner) {
        this.ownerUuid = owner == null ? null : owner.getUuid();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    public float getPower(float tickDelta) {
        float t = age + tickDelta;
        float grow = MathHelper.clamp(t / 15f, 0, 1);
        float shrink = MathHelper.clamp((LIFETIME - t) / 8f, 0, 1);
        return Math.min(grow, shrink);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3d center = getPos().add(0, 0.5, 0);
        if (getWorld().isClient) {
            for (int i = 0; i < 6; i++) {
                double a = random.nextDouble() * Math.PI * 2;
                double r = 1.5 + random.nextDouble() * 4;
                double px = center.x + Math.cos(a) * r, pz = center.z + Math.sin(a) * r, py = center.y + (random.nextDouble() - 0.5) * 1.5;
                Vec3d to = center.subtract(px, py, pz).multiply(0.12);
                getWorld().addParticle(i % 2 == 0 ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.SMOKE, px, py, pz, to.x, to.y, to.z);
            }
            return;
        }
        ServerWorld world = (ServerWorld) getWorld();
        if (age % 30 == 1) {
            world.playSound(null, getX(), getY(), getZ(), ModSounds.BLACK_HOLE_HUM, SoundCategory.NEUTRAL, 1.2f, 0.8f + random.nextFloat() * 0.2f);
        }
        float power = getPower(0);
        Box area = new Box(center, center).expand(RANGE);
        for (Entity e : world.getOtherEntities(this, area)) {
            if (e instanceof BlackHoleEntity || e instanceof PortalEntity || e.isSpectator()) continue;
            if (e instanceof PlayerEntity p && (p.isCreative() || p.getUuid().equals(ownerUuid))) continue;
            Vec3d diff = center.subtract(e.getPos().add(0, e.getHeight() / 2, 0));
            double dist = diff.length();
            if (dist > RANGE || dist < 0.01) continue;
            if (e instanceof ItemEntity item && dist < 1.2) {
                swallowed.add(item.getStack().copy());
                item.discard();
                continue;
            }
            double strength = 0.16 * power * (1.0 - dist / RANGE) + 0.02;
            e.setVelocity(e.getVelocity().multiply(0.9).add(diff.normalize().multiply(strength)));
            e.velocityModified = true;
            e.fallDistance = 0;
            if (e instanceof LivingEntity living && dist < 1.8 && age % 10 == 0) {
                living.damage(getDamageSources().magic(), 3.0f);
            }
        }
        if (age >= LIFETIME) collapse(world);
    }

    private void collapse(ServerWorld world) {
        world.playSound(null, getX(), getY(), getZ(), ModSounds.BLACK_HOLE_COLLAPSE, SoundCategory.NEUTRAL, 2.0f, 1.0f);
        world.spawnParticles(ParticleTypes.SONIC_BOOM, getX(), getY() + 0.5, getZ(), 1, 0, 0, 0, 0);
        world.spawnParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 0.5, getZ(), 120, 0.5, 0.5, 0.5, 0.6);
        world.createExplosion(this, getX(), getY() + 0.5, getZ(), 2.0f, World.ExplosionSourceType.NONE);
        for (ItemStack stack : swallowed) {
            ItemEntity item = new ItemEntity(world, getX(), getY() + 0.5, getZ(), stack);
            item.setVelocity((random.nextDouble() - 0.5) * 0.8, 0.4 + random.nextDouble() * 0.4, (random.nextDouble() - 0.5) * 0.8);
            world.spawnEntity(item);
        }
        swallowed.clear();
        discard();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < 96 * 96;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.age = nbt.getInt("HoleAge");
        if (nbt.containsUuid("Owner")) ownerUuid = nbt.getUuid("Owner");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("HoleAge", age);
        if (ownerUuid != null) nbt.putUuid("Owner", ownerUuid);
    }
}
