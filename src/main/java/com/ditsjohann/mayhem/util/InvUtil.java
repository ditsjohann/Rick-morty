package com.ditsjohann.mayhem.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class InvUtil {
    public static boolean has(PlayerEntity player, Item item) {
        return find(player, item) != ItemStack.EMPTY;
    }

    public static ItemStack find(PlayerEntity player, Item item) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s.isOf(item)) return s;
        }
        return ItemStack.EMPTY;
    }

    private InvUtil() {
    }
}
