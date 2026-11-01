package com.ditsjohann.mayhem.registry;

import com.ditsjohann.mayhem.MultiverseMayhem;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ModArmorMaterials {
    public static final RegistryEntry<ArmorMaterial> LAB = register("lab", Map.of(
            ArmorItem.Type.HELMET, 2, ArmorItem.Type.CHESTPLATE, 5, ArmorItem.Type.LEGGINGS, 4, ArmorItem.Type.BOOTS, 2, ArmorItem.Type.BODY, 4),
            15, () -> Ingredient.ofItems(Items.GLASS_PANE), 0.0f);
    public static final RegistryEntry<ArmorMaterial> GLOOP = register("gloop", Map.of(
            ArmorItem.Type.HELMET, 2, ArmorItem.Type.CHESTPLATE, 5, ArmorItem.Type.LEGGINGS, 4, ArmorItem.Type.BOOTS, 3, ArmorItem.Type.BODY, 4),
            12, () -> Ingredient.ofItems(ModItems.GLOOP_BALL), 1.0f);

    private static RegistryEntry<ArmorMaterial> register(String name, Map<ArmorItem.Type, Integer> defense, int enchantability,
                                                         java.util.function.Supplier<Ingredient> repair, float toughness) {
        EnumMap<ArmorItem.Type, Integer> map = new EnumMap<>(ArmorItem.Type.class);
        map.putAll(defense);
        return Registry.registerReference(Registries.ARMOR_MATERIAL, MultiverseMayhem.id(name), new ArmorMaterial(map, enchantability,
                SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, repair, List.of(new ArmorMaterial.Layer(MultiverseMayhem.id(name))), toughness, 0.0f));
    }

    public static void init() {
    }

    private ModArmorMaterials() {
    }
}
