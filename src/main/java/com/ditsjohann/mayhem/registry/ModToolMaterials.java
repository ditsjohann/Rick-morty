package com.ditsjohann.mayhem.registry;

import com.google.common.base.Suppliers;
import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

import java.util.function.Supplier;

public enum ModToolMaterials implements ToolMaterial {
    FLUX(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1900, 10.0f, 3.5f, 18, () -> Ingredient.ofItems(ModItems.FLUX_CRYSTAL)),
    CUSTOMS(BlockTags.INCORRECT_FOR_IRON_TOOL, 400, 6.0f, 2.0f, 10, () -> Ingredient.ofItems(Items.IRON_INGOT));

    private final TagKey<Block> inverseTag;
    private final int durability;
    private final float speed;
    private final float damage;
    private final int enchantability;
    private final Supplier<Ingredient> repair;

    ModToolMaterials(TagKey<Block> inverseTag, int durability, float speed, float damage, int enchantability, Supplier<Ingredient> repair) {
        this.inverseTag = inverseTag;
        this.durability = durability;
        this.speed = speed;
        this.damage = damage;
        this.enchantability = enchantability;
        this.repair = Suppliers.memoize(repair::get);
    }

    @Override
    public int getDurability() {
        return durability;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return speed;
    }

    @Override
    public float getAttackDamage() {
        return damage;
    }

    @Override
    public TagKey<Block> getInverseTag() {
        return inverseTag;
    }

    @Override
    public int getEnchantability() {
        return enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repair.get();
    }
}
