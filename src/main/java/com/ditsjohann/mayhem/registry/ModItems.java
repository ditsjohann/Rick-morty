package com.ditsjohann.mayhem.registry;

import com.ditsjohann.mayhem.MultiverseMayhem;
import com.ditsjohann.mayhem.item.*;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Rarity;

import java.util.ArrayList;
import java.util.List;

public final class ModItems {
    public static final List<Item> CREATIVE_ORDER = new ArrayList<>();

    // --- Materials ---
    public static final Item FLUX_CRYSTAL = register("flux_crystal", new LoreItem(new Item.Settings(), 1));
    public static final Item DARK_MATTER = register("dark_matter", new LoreItem(new Item.Settings().rarity(Rarity.UNCOMMON), 1));
    public static final Item GLOOP_BALL = register("gloop_ball", new LoreItem(new Item.Settings(), 1));
    public static final Item PORTAL_FLUID = register("portal_fluid", new LoreItem(new Item.Settings().maxCount(16), 2));
    public static final Item WATCHER_EYE = register("watcher_eye", new LoreItem(new Item.Settings(), 1));
    public static final Item OMNI_LENS = register("omni_lens", new LoreItem(new Item.Settings().rarity(Rarity.EPIC).maxCount(1).fireproof(), 2, true));

    // --- Food ---
    public static final Item RAW_MYSTERY_MEAT = register("raw_mystery_meat", new LoreItem(new Item.Settings().food(new FoodComponent.Builder()
            .nutrition(3).saturationModifier(0.3f).statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200), 0.3f).build()), 1));
    public static final Item COOKED_MYSTERY_MEAT = register("cooked_mystery_meat", new LoreItem(new Item.Settings().food(new FoodComponent.Builder()
            .nutrition(8).saturationModifier(0.8f).build()), 1));
    public static final Item BURNT_TOAST = register("burnt_toast", new LoreItem(new Item.Settings().food(new FoodComponent.Builder()
            .nutrition(4).saturationModifier(0.4f).snack().build()), 1));
    public static final Item HYPER_SEED = register("hyper_seed", new LoreItem(new Item.Settings().rarity(Rarity.UNCOMMON).food(new FoodComponent.Builder()
            .nutrition(1).saturationModifier(0.1f).alwaysEdible().snack()
            .statusEffect(new StatusEffectInstance(ModEffects.HYPER, 20 * 60), 1.0f).build()), 2));
    public static final Item WOBBLE_STEW = register("wobble_stew", new LoreItem(new Item.Settings().maxCount(1).food(new FoodComponent.Builder()
            .nutrition(7).saturationModifier(0.6f)
            .statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 160), 1.0f)
            .statusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 400), 1.0f)
            .usingConvertsTo(Items.BOWL).build()), 1));

    // --- Hidden projectile visuals ---
    public static final Item PORTAL_ORB = registerHidden("portal_orb", new Item(new Item.Settings()));
    public static final Item PLASMA_ORB = registerHidden("plasma_orb", new Item(new Item.Settings()));
    public static final Item EYE_BEAM_ORB = registerHidden("eye_beam_orb", new Item(new Item.Settings()));

    // --- Gadgets ---
    public static final Item PORTAL_GUN = register("portal_gun", new PortalGunItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)
            .component(ModComponents.CHARGE, 8).component(ModComponents.DESTINATION, 0), false));
    public static final Item OMNI_PORTAL_GUN = register("omni_portal_gun", new PortalGunItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC).fireproof()
            .component(ModComponents.DESTINATION, 0), true));
    public static final Item COORDINATE_CARD = register("coordinate_card", new CoordinateCardItem(new Item.Settings().maxCount(16)));
    public static final Item SIZE_RAY = register("size_ray", new SizeRayItem(new Item.Settings().maxDamage(160).rarity(Rarity.UNCOMMON)));
    public static final Item SWAPPER = register("swapper", new SwapperItem(new Item.Settings().maxDamage(96).rarity(Rarity.UNCOMMON)));
    public static final Item PLASMA_BLASTER = register("plasma_blaster", new PlasmaBlasterItem(new Item.Settings().maxDamage(320).rarity(Rarity.UNCOMMON)));
    public static final Item POCKET_BLACK_HOLE = register("pocket_black_hole", new PocketBlackHoleItem(new Item.Settings().maxCount(16).rarity(Rarity.RARE)));
    public static final Item MYSTERY_JAR = register("mystery_jar", new MysteryJarItem(new Item.Settings().maxCount(16)));
    public static final Item TOASTER_CORE = register("toaster_core", new ToasterCoreItem(new Item.Settings().maxCount(4)));
    public static final Item EYE_BAIT = register("eye_bait", new EyeBaitItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)));
    public static final Item MICROVERSE_BATTERY = register("microverse_battery", new LoreItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE), 2, true));
    public static final Item DIMENSIONAL_PERMIT = register("dimensional_permit", new LoreItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON), 2));
    public static final Item STUN_BATON = register("stun_baton", new StunBatonItem(ModToolMaterials.CUSTOMS, new Item.Settings()
            .attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.CUSTOMS, 3, -2.2f))));

    // --- Tools & armour ---
    public static final Item FLUX_SWORD = register("flux_sword", new FluxSwordItem(ModToolMaterials.FLUX, new Item.Settings()
            .attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.FLUX, 3, -2.4f))));
    public static final Item FLUX_PICKAXE = register("flux_pickaxe", new FluxPickaxeItem(ModToolMaterials.FLUX, new Item.Settings()
            .attributeModifiers(PickaxeItem.createAttributeModifiers(ModToolMaterials.FLUX, 1.0f, -2.8f))));
    public static final Item FLUX_AXE = register("flux_axe", new AxeItem(ModToolMaterials.FLUX, new Item.Settings()
            .attributeModifiers(AxeItem.createAttributeModifiers(ModToolMaterials.FLUX, 5.5f, -3.0f))));
    public static final Item FLUX_SHOVEL = register("flux_shovel", new ShovelItem(ModToolMaterials.FLUX, new Item.Settings()
            .attributeModifiers(ShovelItem.createAttributeModifiers(ModToolMaterials.FLUX, 1.5f, -3.0f))));
    public static final Item LAB_GOGGLES = register("lab_goggles", new ModArmorItem(ModArmorMaterials.LAB, ArmorItem.Type.HELMET,
            new Item.Settings().maxDamage(ArmorItem.Type.HELMET.getMaxDamage(20))));
    public static final Item GLOOP_BOOTS = register("gloop_boots", new ModArmorItem(ModArmorMaterials.GLOOP, ArmorItem.Type.BOOTS,
            new Item.Settings().maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(22))));

    // --- Music ---
    public static final Item MUSIC_DISC_INTERDIMENSIONAL_JAM = register("music_disc_interdimensional_jam", new Item(new Item.Settings().maxCount(1).rarity(Rarity.RARE)
            .jukeboxPlayable(RegistryKey.of(RegistryKeys.JUKEBOX_SONG, MultiverseMayhem.id("interdimensional_jam")))));

    // --- Spawn eggs ---
    public static final Item GLOOPER_SPAWN_EGG = egg("glooper", ModEntities.GLOOPER, 0xB04CFF, 0x7CFF5A);
    public static final Item MEAT_CRAWLER_SPAWN_EGG = egg("meat_crawler", ModEntities.MEAT_CRAWLER, 0xC2405A, 0xFFE3A8);
    public static final Item WATCHER_SPAWN_EGG = egg("watcher", ModEntities.WATCHER, 0xF2EEE6, 0xD1202E);
    public static final Item CUSTOMS_AGENT_SPAWN_EGG = egg("customs_agent", ModEntities.CUSTOMS_AGENT, 0x2C3140, 0xF2C230);
    public static final Item ZORP_SPAWN_EGG = egg("zorp", ModEntities.ZORP, 0x3FD6B5, 0xFF7AD9);
    public static final Item DEFENSE_TOASTER_SPAWN_EGG = egg("defense_toaster", ModEntities.DEFENSE_TOASTER, 0xC9CED6, 0x8A4B1F);
    public static final Item THOUGHT_LEECH_SPAWN_EGG = egg("thought_leech", ModEntities.THOUGHT_LEECH, 0x6A2FA8, 0xFF9CF0);
    public static final Item OMNI_EYE_SPAWN_EGG = egg("omni_eye", ModEntities.OMNI_EYE, 0xFFF4E8, 0x8E00C9);

    private static Item register(String name, Item item) {
        Item r = Registry.register(Registries.ITEM, MultiverseMayhem.id(name), item);
        CREATIVE_ORDER.add(r);
        return r;
    }

    private static Item registerHidden(String name, Item item) {
        return Registry.register(Registries.ITEM, MultiverseMayhem.id(name), item);
    }

    private static Item egg(String name, EntityType<? extends MobEntity> type, int primary, int secondary) {
        return register(name + "_spawn_egg", new SpawnEggItem(type, primary, secondary, new Item.Settings()));
    }

    public static void init() {
        ModArmorMaterials.init();
    }

    private ModItems() {
    }
}
