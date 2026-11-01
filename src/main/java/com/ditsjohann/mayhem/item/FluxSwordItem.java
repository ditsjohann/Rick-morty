package com.ditsjohann.mayhem.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/** Every hit arcs flux lightning to up to three nearby monsters. */
public class FluxSwordItem extends SwordItem {
    public FluxSwordItem(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getWorld() instanceof ServerWorld sw) {
            List<LivingEntity> chain = sw.getEntitiesByClass(LivingEntity.class, target.getBoundingBox().expand(5),
                    e -> e != target && e != attacker && e instanceof Monster && e.isAlive());
            int n = 0;
            Vec3d from = target.getPos().add(0, target.getHeight() / 2, 0);
            for (LivingEntity e : chain) {
                if (n++ >= 3) break;
                Vec3d to = e.getPos().add(0, e.getHeight() / 2, 0);
                GadgetUtil.beam(sw, from, to, ParticleTypes.ELECTRIC_SPARK);
                e.damage(attacker instanceof PlayerEntity p ? sw.getDamageSources().playerAttack(p) : sw.getDamageSources().mobAttack(attacker), 4.0f);
                from = to;
            }
            if (n > 0) sw.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS, 0.4f, 1.8f);
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        LoreItem.appendLore(this, 1, tooltip);
    }
}
