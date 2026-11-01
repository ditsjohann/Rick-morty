package com.ditsjohann.mayhem.event;

import com.ditsjohann.mayhem.MultiverseMayhem;
import com.ditsjohann.mayhem.entity.mob.CustomsAgentEntity;
import com.ditsjohann.mayhem.entity.mob.GlooperEntity;
import com.ditsjohann.mayhem.item.FluxPickaxeItem;
import com.ditsjohann.mayhem.registry.ModAttachments;
import com.ditsjohann.mayhem.registry.ModDimensions;
import com.ditsjohann.mayhem.registry.ModEntities;
import com.ditsjohann.mayhem.registry.ModItems;
import com.ditsjohann.mayhem.registry.ModSounds;
import com.ditsjohann.mayhem.util.SafeSpotFinder;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;

import java.util.ArrayList;
import java.util.List;

public final class ModEvents {
    private static final Identifier GLOOP_GRAVITY = MultiverseMayhem.id("gloopverse_gravity");
    private static final Identifier GLOOP_FALL = MultiverseMayhem.id("gloopverse_fall");
    private static final ThreadLocal<Direction> MINING_FACE = new ThreadLocal<>();
    private static boolean areaMining = false;
    public static final int HEAT_THRESHOLD = 6;
    public static final int NOTES_PAGES = 9;

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(ModEvents::onServerTick);

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (source.isOf(DamageTypes.FALL)) {
                if (entity.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.GLOOP_BOOTS)) return false;
                for (Entity p : entity.getPassengerList()) if (p instanceof GlooperEntity) return false;
            }
            return true;
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, be) -> {
            if (!areaMining && player.getMainHandStack().getItem() instanceof FluxPickaxeItem && !player.isSneaking()) {
                HitResult hit = player.raycast(player.getBlockInteractionRange() + 1, 0, false);
                MINING_FACE.set(hit instanceof BlockHitResult bhr ? bhr.getSide() : Direction.UP);
            } else {
                MINING_FACE.remove();
            }
            return true;
        });
        PlayerBlockBreakEvents.AFTER.register(ModEvents::areaMine);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (!player.getAttachedOrElse(ModAttachments.GOT_NOTES, false)) {
                player.setAttached(ModAttachments.GOT_NOTES, true);
                ItemStack notes = createLabNotes();
                if (!player.giveItemStack(notes)) player.dropItem(notes, false);
            }
        });
    }

    public static ItemStack createLabNotes() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        List<RawFilteredPair<Text>> pages = new ArrayList<>();
        for (int i = 0; i < NOTES_PAGES; i++) pages.add(RawFilteredPair.of(Text.translatable("book.mayhem.lab_notes.page" + i)));
        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(RawFilteredPair.of("Lab Notes"), "A Concerned Scientist", 0, pages, true));
        book.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.mayhem.lab_notes").formatted(Formatting.GREEN));
        return book;
    }

    private static void areaMine(net.minecraft.world.World world, PlayerEntity player, BlockPos pos, BlockState state, net.minecraft.block.entity.BlockEntity be) {
        Direction face = MINING_FACE.get();
        MINING_FACE.remove();
        if (face == null || areaMining || !(player instanceof ServerPlayerEntity sp)) return;
        ItemStack tool = player.getMainHandStack();
        if (!(tool.getItem() instanceof FluxPickaxeItem) || !tool.isSuitableFor(state)) return;
        areaMining = true;
        try {
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    if (a == 0 && b == 0) continue;
                    BlockPos p = switch (face.getAxis()) {
                        case Y -> pos.add(a, 0, b);
                        case X -> pos.add(0, a, b);
                        case Z -> pos.add(a, b, 0);
                    };
                    BlockState s = world.getBlockState(p);
                    if (s.isAir() || s.getHardness(world, p) < 0 || !player.getMainHandStack().isSuitableFor(s)) continue;
                    if (player.getMainHandStack().isEmpty()) break;
                    sp.interactionManager.tryBreakBlock(p);
                }
            }
        } finally {
            areaMining = false;
        }
    }

    private static void onServerTick(MinecraftServer server) {
        int tick = server.getTicks();
        if (tick % 20 != 0) return;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            updateGravity(player);
            if (tick % 1200 == 0) ModAttachments.setHeat(player, ModAttachments.getHeat(player) - 1);
            if (tick % 100 == 0) checkCustoms(player);
        }
    }

    private static void updateGravity(ServerPlayerEntity player) {
        boolean inGloop = player.getWorld().getRegistryKey().equals(ModDimensions.GLOOPVERSE);
        EntityAttributeInstance gravity = player.getAttributeInstance(EntityAttributes.GENERIC_GRAVITY);
        EntityAttributeInstance fall = player.getAttributeInstance(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE);
        if (gravity == null || fall == null) return;
        if (inGloop && !gravity.hasModifier(GLOOP_GRAVITY)) {
            gravity.addTemporaryModifier(new EntityAttributeModifier(GLOOP_GRAVITY, -0.5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            fall.addTemporaryModifier(new EntityAttributeModifier(GLOOP_FALL, 7.0, EntityAttributeModifier.Operation.ADD_VALUE));
            player.sendMessage(Text.translatable("message.mayhem.low_gravity").formatted(Formatting.LIGHT_PURPLE), true);
        } else if (!inGloop && gravity.hasModifier(GLOOP_GRAVITY)) {
            gravity.removeModifier(GLOOP_GRAVITY);
            fall.removeModifier(GLOOP_FALL);
        }
    }

    private static void checkCustoms(ServerPlayerEntity player) {
        int heat = ModAttachments.getHeat(player);
        ServerWorld world = player.getServerWorld();
        if (heat < HEAT_THRESHOLD || player.isCreative() || player.isSpectator() || world.getDifficulty() == Difficulty.PEACEFUL) return;
        if (CustomsAgentEntity.isLicensed(player)) return;
        int existing = world.getEntitiesByClass(CustomsAgentEntity.class, player.getBoundingBox().expand(48), e -> true).size();
        if (existing >= 4) return;
        int count = Math.min(1 + (heat - HEAT_THRESHOLD) / 3, 4);
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            double angle = world.random.nextDouble() * Math.PI * 2;
            int dist = 9 + world.random.nextInt(6);
            BlockPos near = player.getBlockPos().add((int) (Math.cos(angle) * dist), 0, (int) (Math.sin(angle) * dist));
            BlockPos feet = SafeSpotFinder.find(world, near);
            CustomsAgentEntity agent = ModEntities.CUSTOMS_AGENT.spawn(world, feet, SpawnReason.EVENT);
            if (agent != null) {
                agent.setTarget(player);
                world.spawnParticles(net.minecraft.particle.ParticleTypes.PORTAL, feet.getX() + 0.5, feet.getY() + 1, feet.getZ() + 0.5, 40, 0.3, 0.8, 0.3, 0.6);
                spawned++;
            }
        }
        if (spawned > 0) {
            world.playSound(null, player.getBlockPos(), ModSounds.AGENT_ALERT, SoundCategory.HOSTILE, 1.5f, 1.0f);
            player.sendMessage(Text.translatable("message.mayhem.customs." + world.random.nextInt(5)).formatted(Formatting.GOLD), false);
            ModAttachments.setHeat(player, heat - 5);
        }
    }

    private ModEvents() {
    }
}
