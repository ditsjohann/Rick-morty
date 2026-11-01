package com.ditsjohann.mayhem.item;

import com.ditsjohann.mayhem.entity.mob.DefenseToasterEntity;
import com.ditsjohann.mayhem.registry.ModEntities;
import com.ditsjohann.mayhem.registry.ModSounds;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

/** Place on the ground to boot up a loyal Defense Toaster. */
public class ToasterCoreItem extends LoreItem {
    public ToasterCoreItem(Settings settings) {
        super(settings, 2);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (!(context.getWorld() instanceof ServerWorld world) || player == null) return ActionResult.success(context.getWorld().isClient);
        BlockPos pos = context.getBlockPos().offset(context.getSide());
        DefenseToasterEntity toaster = ModEntities.DEFENSE_TOASTER.spawn(world, pos, SpawnReason.SPAWN_EGG);
        if (toaster == null) return ActionResult.FAIL;
        toaster.setOwner(player);
        toaster.setYaw(player.getYaw() + 180);
        world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 25, 0.3, 0.3, 0.3, 0.2);
        world.playSound(null, pos, ModSounds.TOASTER_POP, SoundCategory.NEUTRAL, 1.0f, 1.4f);
        player.sendMessage(Text.translatable("message.mayhem.toaster_online").formatted(Formatting.GOLD), true);
        context.getStack().decrementUnlessCreative(1, player);
        return ActionResult.CONSUME;
    }
}
