package com.ditsjohann.mayhem.registry;

import com.ditsjohann.mayhem.MultiverseMayhem;
import com.ditsjohann.mayhem.effect.BrainDrainEffect;
import com.ditsjohann.mayhem.effect.HyperEffect;
import com.ditsjohann.mayhem.effect.SimpleEffect;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public final class ModEffects {
    public static final RegistryEntry<StatusEffect> SHRUNK = register("shrunk",
            new SimpleEffect(StatusEffectCategory.NEUTRAL, 0x7CFFCB)
                    .addAttributeModifier(EntityAttributes.GENERIC_SCALE, MultiverseMayhem.id("effect.shrunk.scale"), -0.5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, MultiverseMayhem.id("effect.shrunk.damage"), -0.4, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, MultiverseMayhem.id("effect.shrunk.fall"), 6.0, EntityAttributeModifier.Operation.ADD_VALUE));
    public static final RegistryEntry<StatusEffect> ENLARGED = register("enlarged",
            new SimpleEffect(StatusEffectCategory.NEUTRAL, 0xFF7A3C)
                    .addAttributeModifier(EntityAttributes.GENERIC_SCALE, MultiverseMayhem.id("effect.enlarged.scale"), 1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, MultiverseMayhem.id("effect.enlarged.damage"), 3.0, EntityAttributeModifier.Operation.ADD_VALUE)
                    .addAttributeModifier(EntityAttributes.GENERIC_STEP_HEIGHT, MultiverseMayhem.id("effect.enlarged.step"), 0.6, EntityAttributeModifier.Operation.ADD_VALUE)
                    .addAttributeModifier(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE, MultiverseMayhem.id("effect.enlarged.reach"), 2.0, EntityAttributeModifier.Operation.ADD_VALUE)
                    .addAttributeModifier(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, MultiverseMayhem.id("effect.enlarged.ereach"), 2.0, EntityAttributeModifier.Operation.ADD_VALUE));
    public static final RegistryEntry<StatusEffect> HYPER = register("hyper",
            new HyperEffect(StatusEffectCategory.BENEFICIAL, 0xFF2BD6)
                    .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, MultiverseMayhem.id("effect.hyper.speed"), 0.6, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(EntityAttributes.GENERIC_JUMP_STRENGTH, MultiverseMayhem.id("effect.hyper.jump"), 0.25, EntityAttributeModifier.Operation.ADD_VALUE)
                    .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, MultiverseMayhem.id("effect.hyper.attack_speed"), 0.5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED, MultiverseMayhem.id("effect.hyper.mining"), 0.75, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final RegistryEntry<StatusEffect> BRAIN_DRAIN = register("brain_drain",
            new BrainDrainEffect(StatusEffectCategory.HARMFUL, 0x9B3BFF));

    private static RegistryEntry<StatusEffect> register(String name, StatusEffect effect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, MultiverseMayhem.id(name), effect);
    }

    public static void init() {
    }

    private ModEffects() {
    }
}
