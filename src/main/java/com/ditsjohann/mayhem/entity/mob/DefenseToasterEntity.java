package com.ditsjohann.mayhem.entity.mob;

import com.ditsjohann.mayhem.entity.projectile.ToastEntity;
import com.ditsjohann.mayhem.registry.ModSounds;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A kitchen appliance that achieved sentience and chose violence (against your enemies).
 * Follows its owner, sits on command, launches flaming toast at monsters. Repair it with bread.
 */
public class DefenseToasterEntity extends TameableEntity implements RangedAttackMob {
    /** Client-side: ticks since last shot, for the toast-pop animation. */
    public int popTicks = 100;

    public DefenseToasterEntity(EntityType<? extends TameableEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.27)
                .add(EntityAttributes.GENERIC_ARMOR, 6.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 20.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(1, new SwimGoal(this));
        goalSelector.add(2, new SitGoal(this));
        goalSelector.add(3, new ProjectileAttackGoal(this, 1.0, 18, 26, 16.0f));
        goalSelector.add(5, new FollowOwnerGoal(this, 1.1, 7.0f, 2.5f));
        goalSelector.add(8, new WanderAroundFarGoal(this, 0.8));
        goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        goalSelector.add(10, new LookAroundGoal(this));
        targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        targetSelector.add(2, new AttackWithOwnerGoal(this));
        targetSelector.add(3, new RevengeGoal(this));
        targetSelector.add(4, new ActiveTargetGoal<>(this, MobEntity.class, 10, true, false,
                e -> e instanceof Monster && !(e instanceof CreeperEntity && squaredDistanceTo(e) < 16)));
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        if (isSitting()) return;
        ToastEntity toast = new ToastEntity(getWorld(), this);
        toast.setPosition(getX(), getY() + 0.75, getZ());
        double dx = target.getX() - getX();
        double dy = target.getEyeY() - 1.1 - toast.getY();
        double dz = target.getZ() - getZ();
        double arc = Math.sqrt(dx * dx + dz * dz) * 0.2;
        toast.setVelocity(dx, dy + arc, dz, 1.4f, 4.0f);
        getWorld().spawnEntity(toast);
        playSound(ModSounds.TOASTER_POP, 1.0f, 0.9f + random.nextFloat() * 0.25f);
        getWorld().sendEntityStatus(this, (byte) 112);
    }

    @Override
    public void handleStatus(byte status) {
        if (status == 112) {
            popTicks = 0;
            for (int i = 0; i < 4; i++) {
                getWorld().addParticle(ParticleTypes.SMOKE, getX() + (random.nextDouble() - 0.5) * 0.4, getY() + 0.8, getZ() + (random.nextDouble() - 0.5) * 0.4, 0, 0.08, 0);
            }
        } else {
            super.handleStatus(status);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) popTicks++;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (getWorld().isClient) return isOwner(player) ? ActionResult.CONSUME : ActionResult.PASS;
        if (isOwner(player)) {
            if (stack.isOf(Items.BREAD) && getHealth() < getMaxHealth()) {
                stack.decrementUnlessCreative(1, player);
                heal(10.0f);
                playSound(SoundEvents.BLOCK_ANVIL_USE, 0.4f, 1.6f);
                getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
                return ActionResult.SUCCESS;
            }
            setSitting(!isSitting());
            navigation.stop();
            setTarget(null);
            playSound(SoundEvents.BLOCK_LEVER_CLICK, 0.8f, isSitting() ? 0.6f : 0.9f);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public boolean canBreedWith(net.minecraft.entity.passive.AnimalEntity other) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.TOASTER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.TOASTER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_IRON_GOLEM_DEATH;
    }

    @Override
    public int getMinAmbientSoundDelay() {
        return 400;
    }
}
