package com.ditsjohann.mayhem.registry;

import com.ditsjohann.mayhem.MultiverseMayhem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItemGroups {
    public static final ItemGroup MAYHEM = Registry.register(Registries.ITEM_GROUP, MultiverseMayhem.id("mayhem"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.PORTAL_GUN))
                    .displayName(Text.translatable("itemGroup.mayhem"))
                    .entries((ctx, entries) -> {
                        for (Item item : ModItems.CREATIVE_ORDER) entries.add(item);
                        for (Block block : ModBlocks.BLOCKS_WITH_ITEMS) entries.add(block);
                    })
                    .build());

    public static void init() {
    }

    private ModItemGroups() {
    }
}
