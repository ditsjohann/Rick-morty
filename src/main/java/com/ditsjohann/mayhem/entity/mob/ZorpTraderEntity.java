package com.ditsjohann.mayhem.entity.mob;

import com.ditsjohann.mayhem.registry.ModBlocks;
import com.ditsjohann.mayhem.registry.ModItems;
import com.ditsjohann.mayhem.registry.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Zorp: three eyes, two antennae, one questionable business licence.
 * Accepts Flux Crystals and emeralds for an assortment of interdimensional goods.
 */
public class ZorpTraderEntity extends MerchantEntity {
    private int despawnDelay;

    public ZorpTraderEntity(EntityType<? extends MerchantEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5);
    }

    public void setDespawnDelay(int ticks) {
        this.despawnDelay = ticks;
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new StopFollowingCustomerGoal(this));
        goalSelector.add(1, new FleeEntityGoal<>(this, HostileEntity.class, 8.0f, 0.6, 0.75));
        goalSelector.add(1, new EscapeDangerGoal(this, 0.75));
        goalSelector.add(1, new LookAtCustomerGoal(this));
        goalSelector.add(8, new WanderAroundFarGoal(this, 0.4));
        goalSelector.add(9, new StopAndLookAtEntityGoal(this, PlayerEntity.class, 3.0f, 1.0f));
        goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0f));
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public boolean isLeveledMerchant() {
        return false;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isOf(ModItems.ZORP_SPAWN_EGG) && isAlive() && !hasCustomer() && !isBaby()) {
            if (!getWorld().isClient) {
                if (getOffers().isEmpty()) return ActionResult.CONSUME;
                setCustomer(player);
                sendOffers(player, getDisplayName(), 1);
                playSound(ModSounds.ZORP_AMBIENT, 1.0f, 1.0f);
            }
            return ActionResult.success(getWorld().isClient);
        }
        return super.interactMob(player, hand);
    }

    private static TradeOffer offer(ItemConvertible buy, int buyCount, @Nullable ItemConvertible buy2, int buy2Count, ItemConvertible sell, int sellCount, int uses) {
        return new TradeOffer(new TradedItem(buy, buyCount),
                buy2 == null ? Optional.empty() : Optional.of(new TradedItem(buy2, buy2Count)),
                new ItemStack(sell, sellCount), uses, 4, 0.05f);
    }

    @Override
    protected void fillRecipes() {
        Item flux = ModItems.FLUX_CRYSTAL;
        List<Supplier<TradeOffer>> common = new ArrayList<>(List.of(
                () -> offer(flux, 3, null, 0, ModItems.PORTAL_FLUID, 4, 12),
                () -> offer(Items.EMERALD, 6, null, 0, ModItems.PORTAL_FLUID, 3, 12),
                () -> offer(ModItems.GLOOP_BALL, 10, null, 0, flux, 1, 16),
                () -> offer(ModItems.RAW_MYSTERY_MEAT, 8, null, 0, flux, 2, 16),
                () -> offer(flux, 4, null, 0, ModItems.HYPER_SEED, 2, 8),
                () -> offer(flux, 2, null, 0, ModItems.COORDINATE_CARD, 1, 8),
                () -> offer(Items.EMERALD, 12, null, 0, ModItems.MYSTERY_JAR, 1, 6),
                () -> offer(flux, 6, null, 0, ModItems.MYSTERY_JAR, 1, 6),
                () -> offer(flux, 3, null, 0, ModBlocks.WOBBLE_SHROOM, 4, 12),
                () -> offer(Items.DIAMOND, 1, null, 0, flux, 5, 8),
                () -> offer(flux, 8, Items.IRON_INGOT, 4, ModItems.TOASTER_CORE, 1, 3)
        ));
        List<Supplier<TradeOffer>> rare = new ArrayList<>(List.of(
                () -> offer(flux, 24, Items.PAPER, 1, ModItems.DIMENSIONAL_PERMIT, 1, 2),
                () -> offer(flux, 16, ModItems.DARK_MATTER, 2, ModItems.MICROVERSE_BATTERY, 1, 2),
                () -> offer(ModItems.DARK_MATTER, 2, null, 0, ModItems.MUSIC_DISC_INTERDIMENSIONAL_JAM, 1, 1),
                () -> offer(flux, 12, null, 0, ModBlocks.INTERDIMENSIONAL_TV, 1, 2),
                () -> offer(flux, 20, ModItems.WATCHER_EYE, 2, ModItems.POCKET_BLACK_HOLE, 2, 3)
        ));
        TradeOfferList offers = getOffers();
        java.util.Collections.shuffle(common, new java.util.Random(random.nextLong()));
        java.util.Collections.shuffle(rare, new java.util.Random(random.nextLong()));
        for (int i = 0; i < 6; i++) offers.add(common.get(i).get());
        for (int i = 0; i < 2; i++) offers.add(rare.get(i).get());
    }

    @Override
    protected void afterUsing(TradeOffer offer) {
        if (offer.shouldRewardPlayerExperience()) {
            getWorld().spawnEntity(new ExperienceOrbEntity(getWorld(), getX(), getY() + 0.5, getZ(), 3 + random.nextInt(4)));
        }
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!getWorld().isClient && despawnDelay > 0 && !hasCustomer() && --despawnDelay == 0) {
            ((ServerWorld) getWorld()).spawnParticles(net.minecraft.particle.ParticleTypes.PORTAL, getX(), getY() + 1, getZ(), 40, 0.3, 0.8, 0.3, 0.5);
            discard();
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return hasCustomer() ? ModSounds.ZORP_YES : ModSounds.ZORP_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ZORP_NO;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ZORP_NO;
    }

    @Override
    protected SoundEvent getTradingSound(boolean sold) {
        return sold ? ModSounds.ZORP_YES : ModSounds.ZORP_NO;
    }

    @Override
    public SoundEvent getYesSound() {
        return ModSounds.ZORP_YES;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("DespawnDelay", despawnDelay);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        despawnDelay = nbt.getInt("DespawnDelay");
    }
}
