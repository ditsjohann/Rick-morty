package com.ditsjohann.mayhem.item;

import com.ditsjohann.mayhem.entity.PortalEntity;
import com.ditsjohann.mayhem.entity.projectile.PortalShotEntity;
import com.ditsjohann.mayhem.registry.ModAttachments;
import com.ditsjohann.mayhem.registry.ModComponents;
import com.ditsjohann.mayhem.registry.ModItems;
import com.ditsjohann.mayhem.registry.ModSounds;
import com.ditsjohann.mayhem.util.InvUtil;
import com.ditsjohann.mayhem.util.PortalDestination;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

/**
 * The flagship gadget. Fires a blob of portal fluid; wherever it lands, a portal opens to the dialled
 * destination (with a return portal on the other side). Sneak-use to change destination.
 * Hold a Coordinate Card in your other hand to travel to an exact bookmarked location.
 */
public class PortalGunItem extends Item {
    public static final int MAX_CHARGE = 16;
    public static final int CHARGE_PER_FLUID = 4;
    private final boolean omni;

    public PortalGunItem(Settings settings, boolean omni) {
        super(settings);
        this.omni = omni;
    }

    public boolean isOmni() {
        return omni;
    }

    public static PortalDestination getDestination(ItemStack stack) {
        return PortalDestination.byIndex(stack.getOrDefault(ModComponents.DESTINATION, 0));
    }

    public static int getCharge(ItemStack stack) {
        return stack.getOrDefault(ModComponents.CHARGE, 0);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking()) {
            PortalDestination next = getDestination(stack).next(omni);
            stack.set(ModComponents.DESTINATION, next.ordinal());
            if (!world.isClient) {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.PORTAL_GUN_MODE, SoundCategory.PLAYERS, 0.7f, 1.0f + next.ordinal() * 0.08f);
                user.sendMessage(Text.translatable("message.mayhem.destination", next.displayName()), true);
            }
            return TypedActionResult.success(stack, world.isClient);
        }
        if (world.isClient) return TypedActionResult.success(stack, true);

        boolean free = omni || user.getAbilities().creativeMode;
        int charge = getCharge(stack);
        if (!free && charge <= 0) {
            ItemStack fluid = InvUtil.find(user, ModItems.PORTAL_FLUID);
            if (!fluid.isEmpty()) {
                fluid.decrement(1);
                charge = CHARGE_PER_FLUID;
                user.sendMessage(Text.translatable("message.mayhem.reloaded").formatted(Formatting.GREEN), true);
            } else {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.PORTAL_GUN_EMPTY, SoundCategory.PLAYERS, 1.0f, 1.0f);
                user.sendMessage(Text.translatable("message.mayhem.no_fluid").formatted(Formatting.RED), true);
                user.getItemCooldownManager().set(this, 10);
                return TypedActionResult.fail(stack);
            }
        }

        GlobalPos card = null;
        ItemStack other = user.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
        if (other.isOf(ModItems.COORDINATE_CARD)) card = other.get(ModComponents.STORED_POS);

        PortalShotEntity shot = new PortalShotEntity(world, user, getDestination(stack), card, omni ? 600 : 300,
                omni ? PortalEntity.VARIANT_OMNI : PortalEntity.VARIANT_GREEN);
        shot.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, omni ? 3.0f : 2.2f, 0.25f);
        world.spawnEntity(shot);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.PORTAL_GUN_FIRE, SoundCategory.PLAYERS, 1.0f, 0.9f + world.random.nextFloat() * 0.2f);

        if (!free) charge--;
        stack.set(ModComponents.CHARGE, charge);
        user.getItemCooldownManager().set(this, omni ? 6 : 16);
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (user instanceof ServerPlayerEntity sp) ModAttachments.addPortalHeat(sp, card != null ? 2 : 1);
        return TypedActionResult.success(stack, false);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!omni && !world.isClient && world.getTime() % 300 == 0 && entity instanceof PlayerEntity p
                && getCharge(stack) < MAX_CHARGE && InvUtil.has(p, ModItems.MICROVERSE_BATTERY)) {
            stack.set(ModComponents.CHARGE, getCharge(stack) + 1);
        }
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return !omni;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0f * MathHelper.clamp(getCharge(stack), 0, MAX_CHARGE) / MAX_CHARGE);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0x6BFF3A;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return omni;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.mayhem.portal_gun.destination", getDestination(stack).displayName()).formatted(Formatting.GRAY));
        if (omni) {
            tooltip.add(Text.translatable("tooltip.mayhem.portal_gun.infinite").formatted(Formatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Text.translatable("tooltip.mayhem.portal_gun.charge", getCharge(stack), MAX_CHARGE).formatted(Formatting.GREEN));
        }
        tooltip.add(Text.translatable("tooltip.mayhem.portal_gun.help0").formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.translatable("tooltip.mayhem.portal_gun.help1").formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.translatable("tooltip.mayhem.portal_gun.help2").formatted(Formatting.DARK_GRAY));
    }
}
