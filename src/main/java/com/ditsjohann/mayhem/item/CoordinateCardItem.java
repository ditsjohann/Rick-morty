package com.ditsjohann.mayhem.item;

import com.ditsjohann.mayhem.registry.ModComponents;
import com.ditsjohann.mayhem.registry.ModSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.util.List;

/** Right-click a block to bookmark it. Hold in your off-hand while firing a portal gun to go there. */
public class CoordinateCardItem extends Item {
    public CoordinateCardItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        if (player == null || player.isSneaking()) return ActionResult.PASS;
        ItemStack stack = context.getStack();
        BlockPos pos = context.getBlockPos();
        if (!world.isClient) {
            ItemStack target = stack;
            if (stack.getCount() > 1) {
                target = stack.split(1);
            }
            target.set(ModComponents.STORED_POS, GlobalPos.create(world.getRegistryKey(), pos));
            if (target != stack && !player.giveItemStack(target)) player.dropItem(target, false);
            world.playSound(null, pos, ModSounds.PORTAL_GUN_MODE, SoundCategory.PLAYERS, 0.8f, 1.6f);
            player.sendMessage(Text.translatable("message.mayhem.card_saved", pos.getX(), pos.getY(), pos.getZ(),
                    world.getRegistryKey().getValue().toString()).formatted(Formatting.AQUA), true);
        }
        return ActionResult.success(world.isClient);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking() && stack.contains(ModComponents.STORED_POS)) {
            stack.remove(ModComponents.STORED_POS);
            if (!world.isClient) user.sendMessage(Text.translatable("message.mayhem.card_cleared").formatted(Formatting.GRAY), true);
            return TypedActionResult.success(stack, world.isClient);
        }
        return TypedActionResult.pass(stack);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.contains(ModComponents.STORED_POS);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        GlobalPos gp = stack.get(ModComponents.STORED_POS);
        if (gp != null) {
            tooltip.add(Text.translatable("tooltip.mayhem.coordinate_card.stored", gp.pos().getX(), gp.pos().getY(), gp.pos().getZ()).formatted(Formatting.AQUA));
            tooltip.add(Text.literal(gp.dimension().getValue().toString()).formatted(Formatting.DARK_AQUA));
        } else {
            tooltip.add(Text.translatable("tooltip.mayhem.coordinate_card.blank").formatted(Formatting.GRAY));
        }
        tooltip.add(Text.translatable("tooltip.mayhem.coordinate_card.help").formatted(Formatting.DARK_GRAY));
    }
}
