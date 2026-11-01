package com.ditsjohann.mayhem.util;

import com.ditsjohann.mayhem.entity.mob.ZorpTraderEntity;
import com.ditsjohann.mayhem.registry.ModEntities;
import com.ditsjohann.mayhem.registry.ModSounds;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

/** Things that happen to people who travel by portal. */
public final class PortalEvents {
    public static void onPlayerArrived(ServerPlayerEntity player, ServerWorld world) {
        // Zorp the interdimensional salesman occasionally follows portal signatures.
        if (world.random.nextInt(100) < 6) {
            BlockPos near = player.getBlockPos().add(world.random.nextBetween(-6, 6), 0, world.random.nextBetween(-6, 6));
            BlockPos feet = SafeSpotFinder.find(world, near);
            ZorpTraderEntity zorp = ModEntities.ZORP.spawn(world, feet, SpawnReason.EVENT);
            if (zorp != null) {
                zorp.setDespawnDelay(24000);
                world.playSound(null, feet, ModSounds.ZORP_AMBIENT, SoundCategory.NEUTRAL, 1.0f, 1.0f);
                player.sendMessage(Text.translatable("message.mayhem.zorp_arrives").formatted(Formatting.LIGHT_PURPLE), false);
            }
        }
    }

    private PortalEvents() {
    }
}
