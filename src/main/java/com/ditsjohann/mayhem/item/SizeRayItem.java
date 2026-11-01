package com.ditsjohann.mayhem.item;

import com.ditsjohann.mayhem.entity.mob.OmniEyeEntity;
import com.ditsjohann.mayhem.registry.ModComponents;
import com.ditsjohann.mayhem.registry.ModEffects;
import com.ditsjohann.mayhem.registry.ModSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.List;

/**
 * Shrinks or enlarges whatever you point it at (yourself included: look straight down).
 * Sneak-use to switch between Shrink, Enlarge and Restore.
 */
public class SizeRayItem extends Item {
    public static final int DURATION = 20 * 90;

    public SizeRayItem(Settings settings) {
        super(settings);
    }

    private static int mode(ItemStack s) {
        return s.getOrDefault(ModComponents.MODE, 0);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking()) {
            int m = (mode(stack) + 1) % 3;
            stack.set(ModComponents.MODE, m);
            if (!world.isClient) user.sendMessage(Text.translatable("message.mayhem.size_mode." + m).formatted(Formatting.YELLOW), true);
            return TypedActionResult.success(stack, world.isClient);
        }
        if (world instanceof ServerWorld sw) {
            LivingEntity target = GadgetUtil.raycastLiving(user, 24);
            if (target == null && user.getPitch() > 70) target = user;
            int m = mode(stack);
            Vector3f color = m == 0 ? new Vector3f(0.3f, 1f, 0.8f) : m == 1 ? new Vector3f(1f, 0.5f, 0.2f) : new Vector3f(1f, 1f, 1f);
            GadgetUtil.beam(sw, GadgetUtil.muzzle(user), target != null && target != user ? target.getPos().add(0, target.getHeight() / 2, 0) : GadgetUtil.beamEnd(user, 24),
                    new DustParticleEffect(color, 1.0f));
            sw.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.SIZE_RAY, SoundCategory.PLAYERS, 1.0f, m == 0 ? 1.5f : m == 1 ? 0.6f : 1.0f);
            if (target != null && !(target instanceof OmniEyeEntity)) {
                target.removeStatusEffect(ModEffects.SHRUNK);
                target.removeStatusEffect(ModEffects.ENLARGED);
                if (m == 0) target.addStatusEffect(new StatusEffectInstance(ModEffects.SHRUNK, DURATION), user);
                else if (m == 1) target.addStatusEffect(new StatusEffectInstance(ModEffects.ENLARGED, DURATION), user);
                stack.damage(1, user, LivingEntity.getSlotForHand(hand));
            }
            user.getItemCooldownManager().set(this, 12);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.mayhem.size_ray.mode", Text.translatable("message.mayhem.size_mode." + mode(stack))).formatted(Formatting.YELLOW));
        LoreItem.appendLore(this, 2, tooltip);
    }
}
