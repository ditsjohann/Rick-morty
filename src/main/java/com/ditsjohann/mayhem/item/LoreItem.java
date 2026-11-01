package com.ditsjohann.mayhem.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** An item with flavour-text tooltip lines: {@code tooltip.mayhem.<id>.0 .. n-1}. */
public class LoreItem extends Item {
    private final int lines;
    private final boolean glint;

    public LoreItem(Settings settings, int lines) {
        this(settings, lines, false);
    }

    public LoreItem(Settings settings, int lines, boolean glint) {
        super(settings);
        this.lines = lines;
        this.glint = glint;
    }

    public static void appendLore(Item item, int lines, List<Text> tooltip) {
        String base = item.getTranslationKey().replace("item.mayhem.", "tooltip.mayhem.");
        for (int i = 0; i < lines; i++) {
            tooltip.add(Text.translatable(base + "." + i).formatted(i == 0 ? Formatting.GRAY : Formatting.DARK_GRAY));
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        appendLore(this, lines, tooltip);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return glint || super.hasGlint(stack);
    }
}
