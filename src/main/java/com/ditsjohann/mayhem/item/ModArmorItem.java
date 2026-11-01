package com.ditsjohann.mayhem.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

/** Lab Goggles (night vision) and Gloop Boots (jump boost, fall immunity via ModEvents). */
public class ModArmorItem extends ArmorItem {
    public ModArmorItem(RegistryEntry<ArmorMaterial> material, Type type, Settings settings) {
        super(material, type, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient || !(entity instanceof PlayerEntity player) || world.getTime() % 40 != 0) return;
        EquipmentSlot equip = getSlotType();
        if (player.getEquippedStack(equip) != stack) return;
        if (equip == EquipmentSlot.HEAD) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 300, 0, true, false, true));
        } else if (equip == EquipmentSlot.FEET) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 60, 1, true, false, true));
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        LoreItem.appendLore(this, 1, tooltip);
    }
}
