package com.ditsjohann.mayhem.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/** Public-constructor status effect for attribute-only effects. */
public class SimpleEffect extends StatusEffect {
    public SimpleEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }
}
