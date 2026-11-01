package com.ditsjohann.mayhem.item;

import com.ditsjohann.mayhem.entity.mob.OmniEyeEntity;
import com.ditsjohann.mayhem.registry.ModDimensions;
import com.ditsjohann.mayhem.registry.ModEntities;
import com.ditsjohann.mayhem.registry.ModSounds;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Wave this pungent lure around in the Meatverse and something enormous will notice. */
public class EyeBaitItem extends LoreItem {
    public EyeBaitItem(Settings settings) {
        super(settings, 2);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!(world instanceof ServerWorld sw)) return TypedActionResult.success(stack, true);
        if (!world.getRegistryKey().equals(ModDimensions.MEATVERSE)) {
            user.sendMessage(Text.translatable("message.mayhem.bait_wrong_place").formatted(Formatting.RED), true);
            return TypedActionResult.fail(stack);
        }
        BlockPos spawn = user.getBlockPos().up(4);
        for (int i = 0; i < 6 && !sw.isSpaceEmpty(ModEntities.OMNI_EYE.getSpawnBox(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5)); i++) {
            spawn = spawn.down();
        }
        OmniEyeEntity eye = ModEntities.OMNI_EYE.spawn(sw, spawn, SpawnReason.TRIGGERED);
        if (eye == null) return TypedActionResult.fail(stack);
        eye.setPersistent();
        eye.setTarget(user);
        sw.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, eye.getX(), eye.getY() + 1, eye.getZ(), 1, 0, 0, 0, 0);
        sw.spawnParticles(ParticleTypes.REVERSE_PORTAL, eye.getX(), eye.getY() + 1, eye.getZ(), 200, 1.5, 1.5, 1.5, 0.5);
        sw.playSound(null, eye.getBlockPos(), ModSounds.OMNI_ROAR, SoundCategory.HOSTILE, 5.0f, 0.6f);
        for (ServerPlayerEntity p : sw.getPlayers(p -> p.squaredDistanceTo(user) < 64 * 64)) {
            p.sendMessage(Text.translatable("message.mayhem.omni_summoned").formatted(Formatting.DARK_PURPLE, Formatting.BOLD), false);
        }
        stack.decrementUnlessCreative(1, user);
        return TypedActionResult.success(stack, false);
    }
}
