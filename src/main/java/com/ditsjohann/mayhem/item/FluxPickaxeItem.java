package com.ditsjohann.mayhem.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

/** Mines a 3x3 area (sneak to mine a single block). Logic lives in ModEvents. */
public class FluxPickaxeItem extends PickaxeItem {
    public FluxPickaxeItem(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        LoreItem.appendLore(this, 1, tooltip);
    }
}
