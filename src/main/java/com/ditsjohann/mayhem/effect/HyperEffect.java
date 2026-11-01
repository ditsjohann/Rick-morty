package com.ditsjohann.mayhem.effect;

import com.ditsjohann.mayhem.registry.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

/**
 * Hyper Seeds make you fast, bouncy and unstoppable... until the sugar crash hits.
 */
public class HyperEffect extends StatusEffect {
    public HyperEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getWorld() instanceof ServerWorld world) {
            if (entity.age % 4 == 0) {
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, entity.getX(), entity.getY() + 0.2, entity.getZ(), 2, 0.3, 0.1, 0.3, 0.05);
            }
            StatusEffectInstance self = entity.getStatusEffect(ModEffects.HYPER);
            if (self != null && self.getDuration() <= 1 && !self.isInfinite()) {
                // THE CRASH
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 1));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 600, 0));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 400, 1));
                world.playSound(null, entity.getBlockPos(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 1.0f, 0.5f);
            }
        }
        return true;
    }
}
