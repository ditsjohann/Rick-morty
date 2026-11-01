package com.ditsjohann.mayhem.item;

import com.ditsjohann.mayhem.entity.projectile.ThrownBlackHoleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class PocketBlackHoleItem extends LoreItem {
    public PocketBlackHoleItem(Settings settings) {
        super(settings, 2);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.NEUTRAL, 0.6f, 0.4f);
        if (!world.isClient) {
            ThrownBlackHoleEntity e = new ThrownBlackHoleEntity(world, user);
            e.setItem(stack);
            e.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 1.3f, 1.0f);
            world.spawnEntity(e);
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        user.getItemCooldownManager().set(this, 60);
        stack.decrementUnlessCreative(1, user);
        return TypedActionResult.success(stack, world.isClient);
    }
}
