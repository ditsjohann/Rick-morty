package com.ditsjohann.mayhem.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

/** Slowly siphons your experience and appetite. Courtesy of the Thought Leech. */
public class BrainDrainEffect extends StatusEffect {
    public BrainDrainEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int interval = 40 >> amplifier;
        return interval <= 0 || duration % interval == 0;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity instanceof PlayerEntity player) {
            player.addExhaustion(1.5f);
            if (player.totalExperience > 0) {
                player.addExperience(-1);
            }
        }
        return true;
    }
}
