package com.ditsjohann.mayhem.entity.mob;

import com.ditsjohann.mayhem.registry.ModItems;
import com.ditsjohann.mayhem.registry.ModSounds;
import com.ditsjohann.mayhem.util.InvUtil;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Interdimensional Customs & Excise. Shows up when you open too many unlicensed portals,
 * stuns you with a baton and confiscates your Portal Fluid. Carry a Dimensional Permit to be left alone.
 */
public class CustomsAgentEntity extends HostileEntity {
    private int confiscated;
    private int patrolTicks;

    public CustomsAgentEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 10;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.31)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
                .add(EntityAttributes.GENERIC_ARMOR, 6.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.3);
    }

    public static boolean isLicensed(LivingEntity e) {
        return e instanceof PlayerEntity p && InvUtil.has(p, ModItems.DIMENSIONAL_PERMIT);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(2, new MeleeAttackGoal(this, 1.15, false));
        goalSelector.add(6, new WanderAroundFarGoal(this, 0.8));
        goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
        goalSelector.add(8, new LookAroundGoal(this));
        targetSelector.add(1, new RevengeGoal(this, CustomsAgentEntity.class).setGroupRevenge());
        targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, e -> !isLicensed(e)));
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData);
        equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.STUN_BATON));
        setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.06f);
        return data;
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient) {
            patrolTicks++;
            if (getTarget() != null && isLicensed(getTarget())) {
                setTarget(null);
            }
            // Agents file their paperwork and leave after a while if nobody is around to fine.
            if (patrolTicks > 6000 && getTarget() == null && !isPersistent()) {
                ((ServerWorld) getWorld()).spawnParticles(net.minecraft.particle.ParticleTypes.PORTAL, getX(), getY() + 1, getZ(), 40, 0.3, 0.8, 0.3, 0.5);
                discard();
            }
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof PlayerEntity player) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 2), this);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 80, 1), this);
            if (random.nextInt(3) == 0) {
                ItemStack fluid = InvUtil.find(player, ModItems.PORTAL_FLUID);
                if (!fluid.isEmpty()) {
                    fluid.decrement(1);
                    confiscated++;
                    player.sendMessage(Text.translatable("message.mayhem.confiscated").formatted(Formatting.RED), true);
                    playSound(ModSounds.AGENT_ALERT, 1.0f, 1.2f);
                }
            }
        }
        return hit;
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
        super.dropEquipment(world, source, causedByPlayer);
        if (confiscated > 0) {
            dropStack(new ItemStack(ModItems.PORTAL_FLUID, confiscated));
            confiscated = 0;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.AGENT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.AGENT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.AGENT_DEATH;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Confiscated", confiscated);
        nbt.putInt("PatrolTicks", patrolTicks);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        confiscated = nbt.getInt("Confiscated");
        patrolTicks = nbt.getInt("PatrolTicks");
    }
}
