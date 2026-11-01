package com.ditsjohann.mayhem.entity.projectile;

import com.ditsjohann.mayhem.registry.ModEntities;
import com.ditsjohann.mayhem.registry.ModItems;
import com.ditsjohann.mayhem.util.PortalDestination;
import com.ditsjohann.mayhem.util.PortalLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/** The glowing blob fired by a portal gun. Where it lands, reality tears open. */
public class PortalShotEntity extends ThrownItemEntity {
    private PortalDestination destination = PortalDestination.HOME;
    @Nullable
    private GlobalPos card;
    private int portalLifetime = 300;
    private int variant = 0;
    private boolean done;

    public PortalShotEntity(EntityType<? extends PortalShotEntity> type, World world) {
        super(type, world);
    }

    public PortalShotEntity(World world, LivingEntity owner, PortalDestination destination, @Nullable GlobalPos card, int lifetime, int variant) {
        super(ModEntities.PORTAL_SHOT, owner, world);
        this.destination = destination;
        this.card = card;
        this.portalLifetime = lifetime;
        this.variant = variant;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.PORTAL_ORB;
    }

    @Override
    protected double getGravity() {
        return 0.004;
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) {
            Vector3f c = variant == 1 ? new Vector3f(0.8f, 0.35f, 1f) : new Vector3f(0.45f, 1f, 0.3f);
            getWorld().addParticle(new DustParticleEffect(c, 1.2f), getX(), getY(), getZ(), 0, 0, 0);
        } else if (age > 70 && !done) {
            open(getPos(), defaultYaw());
        }
    }

    private float defaultYaw() {
        Entity owner = getOwner();
        return owner != null ? owner.getYaw() + 180f : getYaw();
    }

    @Override
    protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (getWorld().isClient || done) return;
        if (hit instanceof BlockHitResult bhr) {
            Direction side = bhr.getSide();
            Vec3d p = bhr.getPos();
            if (side == Direction.UP) {
                open(p, defaultYaw());
            } else if (side == Direction.DOWN) {
                open(p.subtract(0, 2.35, 0), defaultYaw());
            } else {
                Vec3d out = p.add(side.getOffsetX() * 0.55, 0, side.getOffsetZ() * 0.55);
                double floor = findFloor(out);
                open(new Vec3d(out.x, floor, out.z), side.asRotation());
            }
        } else if (hit instanceof EntityHitResult ehr) {
            open(ehr.getEntity().getPos(), defaultYaw());
        }
    }

    private double findFloor(Vec3d out) {
        BlockPos.Mutable m = BlockPos.ofFloored(out).mutableCopy();
        for (int i = 0; i < 4; i++) {
            var state = getWorld().getBlockState(m.down());
            if (!state.getCollisionShape(getWorld(), m.down()).isEmpty()) {
                return m.getY();
            }
            m.move(Direction.DOWN);
        }
        return out.y - 1.15;
    }

    private void open(Vec3d at, float yaw) {
        done = true;
        if (getWorld() instanceof ServerWorld sw) {
            ServerPlayerEntity owner = getOwner() instanceof ServerPlayerEntity p ? p : null;
            PortalLogic.openPair(sw, at, yaw, owner, destination, card, portalLifetime, variant);
        }
        discard();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Destination", destination.ordinal());
        nbt.putInt("PortalLifetime", portalLifetime);
        nbt.putInt("Variant", variant);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        destination = PortalDestination.byIndex(nbt.getInt("Destination"));
        portalLifetime = Math.max(40, nbt.getInt("PortalLifetime"));
        variant = nbt.getInt("Variant");
    }
}
