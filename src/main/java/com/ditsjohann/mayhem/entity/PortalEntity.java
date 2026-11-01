package com.ditsjohann.mayhem.entity;

import com.ditsjohann.mayhem.entity.mob.OmniEyeEntity;
import com.ditsjohann.mayhem.registry.ModSounds;
import com.ditsjohann.mayhem.util.PortalEvents;
import com.ditsjohann.mayhem.util.TeleportHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A swirling hole in reality. Anything that touches it is flung to the linked destination.
 */
public class PortalEntity extends Entity {
    private static final TrackedData<Integer> LIFETIME = DataTracker.registerData(PortalEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(PortalEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public static final int VARIANT_GREEN = 0;
    public static final int VARIANT_OMNI = 1;
    public static final int VARIANT_MEAT = 2;

    @Nullable
    private RegistryKey<World> destWorld;
    private Vec3d destPos = Vec3d.ZERO;
    private float destYaw;
    private boolean announcedArrival;
    @Nullable
    private UUID linked;
    /** Entities that just arrived through this portal; ignored until they step out of it. */
    private final Set<UUID> arrivals = new HashSet<>();

    public PortalEntity(EntityType<? extends PortalEntity> type, World world) {
        super(type, world);
        this.noClip = true;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(LIFETIME, 300);
        builder.add(VARIANT, VARIANT_GREEN);
    }

    public void setDestination(RegistryKey<World> world, Vec3d pos, float yaw) {
        this.destWorld = world;
        this.destPos = pos;
        this.destYaw = yaw;
    }

    public void setLinked(@Nullable UUID linked) {
        this.linked = linked;
    }

    public void markArrival(UUID uuid) {
        arrivals.add(uuid);
    }

    public int getLifetime() {
        return dataTracker.get(LIFETIME);
    }

    public void setLifetime(int ticks) {
        dataTracker.set(LIFETIME, ticks);
    }

    public int getVariant() {
        return dataTracker.get(VARIANT);
    }

    public void setVariant(int v) {
        dataTracker.set(VARIANT, v);
    }

    /** 0..1 scale used for the open/close animation. */
    public float getOpenness(float tickDelta) {
        float t = this.age + tickDelta;
        float open = MathHelper.clamp(t / 10f, 0f, 1f);
        float close = MathHelper.clamp((getLifetime() - t) / 10f, 0f, 1f);
        return Math.min(open, close);
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) {
            spawnClientParticles();
            return;
        }
        ServerWorld world = (ServerWorld) getWorld();
        if (!announcedArrival) {
            announcedArrival = true;
            world.playSound(null, getX(), getY() + 1, getZ(), ModSounds.PORTAL_OPEN, SoundCategory.NEUTRAL, 1.0f, 1.0f);
        }
        if (this.age >= getLifetime()) {
            world.playSound(null, getX(), getY() + 1, getZ(), ModSounds.PORTAL_CLOSE, SoundCategory.NEUTRAL, 1.0f, 1.0f);
            discard();
            return;
        }
        if (destWorld == null || this.age < 6) return;
        ServerWorld target = world.getServer().getWorld(destWorld);
        if (target == null) return;

        List<Entity> touching = world.getOtherEntities(this, getBoundingBox().contract(0.15), e ->
                !(e instanceof PortalEntity) && !(e instanceof BlackHoleEntity) && !(e instanceof OmniEyeEntity)
                        && !e.hasVehicle() && e.isAlive() && !e.isSpectator());
        arrivals.removeIf(uuid -> touching.stream().noneMatch(e -> e.getUuid().equals(uuid)));
        for (Entity e : touching) {
            if (e.hasPortalCooldown() || arrivals.contains(e.getUuid())) continue;
            boolean wasPlayer = e instanceof ServerPlayerEntity;
            Entity moved = TeleportHelper.teleport(e, target, destPos, destYaw);
            if (moved == null) continue;
            if (linked != null && target.getEntity(linked) instanceof PortalEntity exit) {
                exit.markArrival(moved.getUuid());
            }
            if (moved instanceof ServerPlayerEntity player && wasPlayer) {
                PortalEvents.onPlayerArrived(player, target);
            }
        }
    }

    private void spawnClientParticles() {
        float open = getOpenness(0);
        if (open <= 0.05f) return;
        Vector3f color = switch (getVariant()) {
            case VARIANT_OMNI -> new Vector3f(0.75f, 0.3f, 1.0f);
            case VARIANT_MEAT -> new Vector3f(1.0f, 0.25f, 0.3f);
            default -> new Vector3f(0.45f, 1.0f, 0.3f);
        };
        float yawRad = getYaw() * MathHelper.RADIANS_PER_DEGREE;
        double rightX = Math.cos(yawRad), rightZ = Math.sin(yawRad);
        for (int i = 0; i < 3; i++) {
            double a = random.nextDouble() * Math.PI * 2;
            double rx = Math.cos(a) * 0.6 * open;
            double ry = Math.sin(a) * 1.05 * open;
            getWorld().addParticle(new DustParticleEffect(color, 1.0f),
                    getX() + rightX * rx, getY() + 1.15 + ry, getZ() + rightZ * rx,
                    0, 0, 0);
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean canHit() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < 128 * 128;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        setLifetime(nbt.getInt("Lifetime"));
        setVariant(nbt.getInt("Variant"));
        this.age = nbt.getInt("PortalAge");
        if (nbt.containsUuid("Linked")) linked = nbt.getUuid("Linked");
        if (nbt.contains("DestWorld", NbtElement.STRING_TYPE)) {
            Identifier id = Identifier.tryParse(nbt.getString("DestWorld"));
            if (id != null) destWorld = RegistryKey.of(RegistryKeys.WORLD, id);
            destPos = new Vec3d(nbt.getDouble("DestX"), nbt.getDouble("DestY"), nbt.getDouble("DestZ"));
            destYaw = nbt.getFloat("DestYaw");
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Lifetime", getLifetime());
        nbt.putInt("Variant", getVariant());
        nbt.putInt("PortalAge", this.age);
        if (linked != null) nbt.putUuid("Linked", linked);
        if (destWorld != null) {
            nbt.putString("DestWorld", destWorld.getValue().toString());
            nbt.putDouble("DestX", destPos.x);
            nbt.putDouble("DestY", destPos.y);
            nbt.putDouble("DestZ", destPos.z);
            nbt.putFloat("DestYaw", destYaw);
        }
    }
}
