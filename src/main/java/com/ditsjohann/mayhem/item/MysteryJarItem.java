package com.ditsjohann.mayhem.item;

import com.ditsjohann.mayhem.entity.mob.GlooperEntity;
import com.ditsjohann.mayhem.entity.mob.ZorpTraderEntity;
import com.ditsjohann.mayhem.registry.ModEffects;
import com.ditsjohann.mayhem.registry.ModEntities;
import com.ditsjohann.mayhem.registry.ModItems;
import com.ditsjohann.mayhem.registry.ModSounds;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** A jar from somewhere. Contents: yes. */
public class MysteryJarItem extends LoreItem {
    private static final int[] WEIGHTS = {14, 5, 9, 6, 6, 10, 6, 6, 10, 8, 6, 6, 3, 2, 3};

    public MysteryJarItem(Settings settings) {
        super(settings, 2);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world instanceof ServerWorld sw) {
            int outcome = roll(sw.random);
            sw.playSound(null, user.getBlockPos(), ModSounds.JAR_OPEN, SoundCategory.PLAYERS, 1.0f, 1.0f);
            user.sendMessage(Text.translatable("message.mayhem.jar." + outcome).formatted(Formatting.LIGHT_PURPLE), false);
            apply(sw, user, outcome);
            stack.decrementUnlessCreative(1, user);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    private static int roll(Random r) {
        int total = 0;
        for (int w : WEIGHTS) total += w;
        int x = r.nextInt(total);
        for (int i = 0; i < WEIGHTS.length; i++) {
            x -= WEIGHTS[i];
            if (x < 0) return i;
        }
        return 0;
    }

    private static void give(PlayerEntity p, ItemStack s) {
        if (!p.giveItemStack(s)) p.dropItem(s, false);
    }

    private static void apply(ServerWorld w, PlayerEntity p, int outcome) {
        Random r = w.random;
        switch (outcome) {
            case 0 -> give(p, new ItemStack(ModItems.FLUX_CRYSTAL, 3 + r.nextInt(4)));
            case 1 -> give(p, new ItemStack(Items.DIAMOND, 1 + r.nextInt(2)));
            case 2 -> give(p, new ItemStack(ModItems.HYPER_SEED, 2));
            case 3 -> {
                GlooperEntity g = ModEntities.GLOOPER.spawn(w, p.getBlockPos(), SpawnReason.TRIGGERED);
                if (g != null) g.setOwner(p);
            }
            case 4 -> {
                for (int i = 0; i < 3; i++) ModEntities.THOUGHT_LEECH.spawn(w, p.getBlockPos().add(r.nextBetween(-2, 2), 0, r.nextBetween(-2, 2)), SpawnReason.TRIGGERED);
            }
            case 5 -> {
                w.spawnParticles(ParticleTypes.FIREWORK, p.getX(), p.getY() + 1.5, p.getZ(), 80, 0.6, 0.6, 0.6, 0.25);
                w.spawnParticles(ParticleTypes.NOTE, p.getX(), p.getY() + 2, p.getZ(), 12, 0.8, 0.3, 0.8, 1);
                w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
            case 6 -> p.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 100));
            case 7 -> p.addStatusEffect(new StatusEffectInstance(ModEffects.SHRUNK, 1200));
            case 8 -> give(p, new ItemStack(ModItems.PORTAL_FLUID, 4));
            case 9 -> give(p, new ItemStack(ModItems.COOKED_MYSTERY_MEAT, 6));
            case 10 -> { /* the jar was empty. it was a metaphor. */ }
            case 11 -> give(p, new ItemStack(Items.EXPERIENCE_BOTTLE, 6));
            case 12 -> {
                ZorpTraderEntity z = ModEntities.ZORP.spawn(w, p.getBlockPos().add(2, 0, 0), SpawnReason.TRIGGERED);
                if (z != null) z.setDespawnDelay(12000);
            }
            case 13 -> give(p, new ItemStack(ModItems.DARK_MATTER, 1));
            case 14 -> w.createExplosion(null, p.getX(), p.getY() + 0.5, p.getZ(), 1.5f, World.ExplosionSourceType.NONE);
            default -> {
            }
        }
    }
}
