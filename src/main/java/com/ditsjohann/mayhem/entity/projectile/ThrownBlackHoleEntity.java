package com.ditsjohann.mayhem.entity.projectile;

import com.ditsjohann.mayhem.entity.BlackHoleEntity;
import com.ditsjohann.mayhem.registry.ModEntities;
import com.ditsjohann.mayhem.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ThrownBlackHoleEntity extends ThrownItemEntity {
    public ThrownBlackHoleEntity(EntityType<? extends ThrownBlackHoleEntity> type, World world) {
        super(type, world);
    }

    public ThrownBlackHoleEntity(World world, LivingEntity owner) {
        super(ModEntities.THROWN_BLACK_HOLE, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.POCKET_BLACK_HOLE;
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) {
            getWorld().addParticle(ParticleTypes.REVERSE_PORTAL, getX(), getY(), getZ(), 0, 0, 0);
        }
    }

    @Override
    protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (!getWorld().isClient) {
            BlackHoleEntity hole = ModEntities.BLACK_HOLE.create(getWorld());
            if (hole != null) {
                Vec3d p = hit.getPos();
                hole.refreshPositionAndAngles(p.x, p.y + 0.6, p.z, 0, 0);
                hole.setOwner(getOwner());
                getWorld().spawnEntity(hole);
            }
            discard();
        }
    }
}
