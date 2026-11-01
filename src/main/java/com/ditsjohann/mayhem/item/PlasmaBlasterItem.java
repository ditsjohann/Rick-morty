package com.ditsjohann.mayhem.item;

import com.ditsjohann.mayhem.entity.projectile.PlasmaBoltEntity;
import com.ditsjohann.mayhem.registry.ModItems;
import com.ditsjohann.mayhem.registry.ModSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Pew. Pew pew. Repair with Flux Crystals in an anvil. */
public class PlasmaBlasterItem extends LoreItem {
    public PlasmaBlasterItem(Settings settings) {
        super(settings, 2);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            PlasmaBoltEntity bolt = new PlasmaBoltEntity(world, user);
            bolt.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 3.0f, 0.4f);
            world.spawnEntity(bolt);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.PLASMA_SHOT, SoundCategory.PLAYERS, 0.9f, 0.85f + world.random.nextFloat() * 0.3f);
            stack.damage(1, user, LivingEntity.getSlotForHand(hand));
        }
        user.getItemCooldownManager().set(this, 7);
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return ingredient.isOf(ModItems.FLUX_CRYSTAL);
    }
}
