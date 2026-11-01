package com.ditsjohann.mayhem.entity.mob;

import com.ditsjohann.mayhem.registry.ModBlocks;
import com.ditsjohann.mayhem.registry.ModEntities;
import com.ditsjohann.mayhem.registry.ModItems;
import com.ditsjohann.mayhem.registry.ModSounds;
import com.ditsjohann.mayhem.util.RideHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A cheerful jelly blob with eyes on stalks. Tame it with Flux Crystals; it follows you,
 * body-slams your enemies into the air and (sneak + right-click) rides on your head,
 * cushioning every fall.
 */
public class GlooperEntity extends TameableEntity {
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(GlooperEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final int VARIANTS = 3;

    /** Client-side squish animation state. */
    public float squish;
    public float prevSquish;
    private int headTicks;

    public GlooperEntity(EntityType<? extends TameableEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 18.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 20.0)
                .add(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, 64.0);
    }

    public static boolean canSpawn(EntityType<GlooperEntity> type, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        BlockState below = world.getBlockState(pos.down());
        return below.isOf(ModBlocks.GLOOP_GRASS) || below.isOf(ModBlocks.GLOOP_BLOCK) || below.isOf(ModBlocks.GLOOPSTONE);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, 0);
    }

    public int getVariant() {
        return dataTracker.get(VARIANT);
    }

    public void setVariant(int v) {
        dataTracker.set(VARIANT, Math.floorMod(v, VARIANTS));
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new SitGoal(this));
        this.goalSelector.add(3, new PounceAtTargetGoal(this, 0.5f));
        this.goalSelector.add(4, new MeleeAttackGoal(this, 1.25, true));
        this.goalSelector.add(5, new FollowOwnerGoal(this, 1.15, 8.0f, 2.5f));
        this.goalSelector.add(6, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(7, new TemptGoal(this, 1.1, s -> s.isOf(ModItems.FLUX_CRYSTAL) || s.isOf(ModBlocks.WOBBLE_SHROOM.asItem()), false));
        this.goalSelector.add(8, new FollowParentGoal(this, 1.1));
        this.goalSelector.add(9, new WanderAroundFarGoal(this, 0.9));
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(11, new LookAroundGoal(this));
        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
        this.targetSelector.add(3, new RevengeGoal(this).setGroupRevenge());
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        setVariant(random.nextInt(VARIANTS));
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) {
            prevSquish = squish;
            float target = isOnGround() ? 0f : (float) Math.max(-0.5, Math.min(0.5, getVelocity().y * 1.5));
            squish += (target - squish) * 0.35f;
            if (random.nextInt(25) == 0) {
                getWorld().addParticle(ParticleTypes.ITEM_SLIME, getX(), getY() + 0.1, getZ(), 0, 0, 0);
            }
            return;
        }
        // Head-riding behaviour.
        if (getVehicle() instanceof ServerPlayerEntity player) {
            headTicks++;
            if (headTicks > 20 && player.isSneaking()) {
                RideHelper.dismount(this);
                headTicks = 0;
            }
            player.fallDistance = 0;
        } else {
            headTicks = 0;
        }
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        // Gloopers move by hopping.
        if (isOnGround() && !isSitting() && getVelocity().horizontalLengthSquared() > 0.0012 && random.nextInt(6) == 0) {
            getJumpControl().setActive();
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit) {
            target.addVelocity(0, 0.55, 0);
            target.velocityModified = true;
            playSound(SoundEvents.BLOCK_SLIME_BLOCK_FALL, 1.0f, 0.7f);
        }
        return hit;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (getWorld().isClient) {
            boolean consume = isOwner(player) || (!isTamed() && stack.isOf(ModItems.FLUX_CRYSTAL));
            return consume ? ActionResult.CONSUME : ActionResult.PASS;
        }
        if (!isTamed() && stack.isOf(ModItems.FLUX_CRYSTAL)) {
            stack.decrementUnlessCreative(1, player);
            if (random.nextInt(3) == 0) {
                setOwner(player);
                navigation.stop();
                setTarget(null);
                setSitting(true);
                getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
            } else {
                getWorld().sendEntityStatus(this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
            }
            return ActionResult.SUCCESS;
        }
        if (isTamed() && isOwner(player)) {
            if (stack.isOf(ModItems.GLOOP_BALL) && getHealth() < getMaxHealth()) {
                stack.decrementUnlessCreative(1, player);
                heal(6.0f);
                return ActionResult.SUCCESS;
            }
            if (stack.isEmpty() && player.isSneaking() && !isBaby() && player instanceof ServerPlayerEntity sp) {
                setSitting(false);
                if (RideHelper.mountOnHead(this, sp)) {
                    playSound(ModSounds.GLOOPER_AMBIENT, 1.0f, 1.4f);
                    return ActionResult.SUCCESS;
                }
            }
            ActionResult result = super.interactMob(player, hand);
            if (!result.isAccepted()) {
                setSitting(!isSitting());
                jumping = false;
                navigation.stop();
                setTarget(null);
                return ActionResult.SUCCESS_NO_ITEM_USED;
            }
            return result;
        }
        return super.interactMob(player, hand);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isOf(ModBlocks.WOBBLE_SHROOM.asItem());
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity mate) {
        GlooperEntity child = ModEntities.GLOOPER.create(world);
        if (child != null) {
            child.setVariant(random.nextBoolean() ? getVariant() : (mate instanceof GlooperEntity g ? g.getVariant() : random.nextInt(VARIANTS)));
            if (isTamed() && getOwnerUuid() != null) {
                child.setOwnerUuid(getOwnerUuid());
                child.setTamed(true, true);
            }
        }
        return child;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.GLOOPER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.GLOOPER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GLOOPER_DEATH;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Variant", getVariant());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setVariant(nbt.getInt("Variant"));
    }
}
