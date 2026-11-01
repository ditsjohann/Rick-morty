package com.ditsjohann.mayhem.item;

import com.ditsjohann.mayhem.entity.mob.OmniEyeEntity;
import com.ditsjohann.mayhem.registry.ModSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Set;

/** Quantum Swapper: instantly trade places with whatever creature you point at. */
public class SwapperItem extends LoreItem {
    public SwapperItem(Settings settings) {
        super(settings, 2);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world instanceof ServerWorld sw) {
            LivingEntity target = GadgetUtil.raycastLiving(user, 32);
            if (target == null || target instanceof OmniEyeEntity) {
                sw.playSound(null, user.getBlockPos(), ModSounds.PORTAL_GUN_EMPTY, SoundCategory.PLAYERS, 0.6f, 1.6f);
                return TypedActionResult.fail(stack);
            }
            Vec3d a = user.getPos();
            Vec3d b = target.getPos();
            float ay = user.getYaw(), by = target.getYaw();
            target.teleport(sw, a.x, a.y, a.z, Set.of(), by, target.getPitch());
            user.teleport(sw, b.x, b.y, b.z, Set.of(), ay, user.getPitch());
            target.fallDistance = 0;
            user.fallDistance = 0;
            sw.spawnParticles(ParticleTypes.REVERSE_PORTAL, a.x, a.y + 1, a.z, 30, 0.3, 0.6, 0.3, 0.2);
            sw.spawnParticles(ParticleTypes.REVERSE_PORTAL, b.x, b.y + 1, b.z, 30, 0.3, 0.6, 0.3, 0.2);
            sw.playSound(null, a.x, a.y, a.z, ModSounds.SWAPPER, SoundCategory.PLAYERS, 1.0f, 1.0f);
            sw.playSound(null, b.x, b.y, b.z, ModSounds.SWAPPER, SoundCategory.PLAYERS, 1.0f, 1.2f);
            stack.damage(1, user, LivingEntity.getSlotForHand(hand));
            user.getItemCooldownManager().set(this, 40);
        }
        return TypedActionResult.success(stack, world.isClient);
    }
}
