package com.ditsjohann.mayhem.client.dev;

import com.ditsjohann.mayhem.MultiverseMayhem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Development-only visual regression helper. Completely inert unless the JVM is started with
 * {@code -Dmayhem.autotest=true} (see the {@code runAutotest} Gradle task). It stages scenes in a
 * singleplayer world via commands and saves screenshots to {@code run/screenshots}.
 */
public final class AutoTest {
    private record Step(int delay, Runnable action) {
    }

    private static final List<Step> STEPS = new ArrayList<>();
    private static int index;
    private static int wait = 200;
    private static int screenTicks;

    public static void init() {
        if (!Boolean.getBoolean("mayhem.autotest")) return;
        MultiverseMayhem.LOGGER.warn("Multiverse Mayhem AUTOTEST mode enabled");
        script();
        ClientTickEvents.END_CLIENT_TICK.register(AutoTest::tick);
    }

    private static void cmd(String... commands) {
        MinecraftClient mc = MinecraftClient.getInstance();
        IntegratedServer server = mc.getServer();
        if (server == null || mc.player == null) return;
        server.execute(() -> {
            ServerPlayerEntity sp = server.getPlayerManager().getPlayer(mc.player.getUuid());
            if (sp == null) return;
            for (String c : commands) {
                server.getCommandManager().executeWithPrefix(sp.getCommandSource().withLevel(4), c);
            }
        });
    }

    private static void shot(String name) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ScreenshotRecorder.saveScreenshot(mc.runDirectory, "autotest_" + name + ".png", mc.getFramebuffer(),
                t -> MultiverseMayhem.LOGGER.info("[autotest] {}", t.getString()));
    }

    private static void step(int delay, Runnable r) {
        STEPS.add(new Step(delay, r));
    }

    private static void script() {
        String clear = "kill @e[type=!player]";
        step(20, () -> cmd("gamemode creative", "time set 6000", "weather clear", "gamerule doDaylightCycle false", "gamerule doMobSpawning false",
                "effect give @s night_vision infinite 0 true", "difficulty peaceful"));
        // Scene 1: mob line-up on a stage
        step(20, () -> cmd("tp @s 0 120 -8 0 15", "fill -12 118 -4 12 118 10 minecraft:smooth_stone", "fill -12 119 -4 12 130 10 air", clear,
                "summon mayhem:glooper -6 119 2 {NoAI:1b,Rotation:[180f,0f],Variant:0}",
                "summon mayhem:glooper -4 119 2 {NoAI:1b,Rotation:[180f,0f],Variant:1}",
                "summon mayhem:glooper -2 119 2 {NoAI:1b,Rotation:[180f,0f],Variant:2}",
                "summon mayhem:meat_crawler 1 119 2 {NoAI:1b,Rotation:[180f,0f]}",
                "summon mayhem:defense_toaster 4 119 2 {NoAI:1b,Rotation:[180f,0f]}",
                "summon mayhem:thought_leech 6 119 1 {NoAI:1b,Rotation:[180f,0f]}",
                "summon mayhem:zorp -3 119 6 {NoAI:1b,Rotation:[180f,0f]}",
                "summon mayhem:customs_agent 0 119 6 {NoAI:1b,Rotation:[180f,0f]}",
                "summon mayhem:watcher 3 121 6 {NoAI:1b,Rotation:[180f,0f]}"));
        step(80, () -> shot("mobs"));
        // Scene 2: boss
        step(10, () -> cmd(clear, "summon mayhem:omni_eye 0 121 6 {NoAI:1b,Rotation:[180f,0f]}"));
        step(60, () -> shot("omni_eye"));
        step(5, () -> cmd(clear, "summon mayhem:omni_eye 0 121 6 {NoAI:1b,Rotation:[180f,0f],Enraged:1b,Health:100f}"));
        step(60, () -> shot("omni_eye_enraged"));
        // Scene 3: portal + black hole, at night for glow
        step(5, () -> cmd(clear, "time set 18000", "effect clear @s night_vision",
                "summon mayhem:portal -2 119 3 {Lifetime:100000,Rotation:[0f,0f]}",
                "summon mayhem:portal 2 119 3 {Lifetime:100000,Variant:1,Rotation:[30f,0f]}",
                "summon mayhem:black_hole 0 122 8 {NoGravity:1b}"));
        step(40, () -> shot("portals"));
        // Scene 4: blocks
        step(5, () -> cmd(clear, "time set 6000", "effect give @s night_vision infinite 0 true",
                "setblock -7 119 3 mayhem:flux_crystal_ore", "setblock -6 119 3 mayhem:deepslate_flux_crystal_ore", "setblock -5 119 3 mayhem:flux_block",
                "setblock -4 119 3 mayhem:dark_matter_block", "setblock -3 119 3 mayhem:gloop_block", "setblock -2 119 3 mayhem:gloopstone",
                "setblock -1 119 3 mayhem:gloopstone_bricks", "setblock 0 119 3 mayhem:gloop_grass", "setblock 1 119 3 mayhem:flesh_block",
                "setblock 2 119 3 mayhem:eye_flesh", "setblock 3 119 3 mayhem:gristle", "setblock 4 119 3 mayhem:dark_matter_ore",
                "setblock 5 119 3 mayhem:landing_pad", "setblock 6 119 3 mayhem:interdimensional_tv[facing=south,screen=1]",
                "setblock 7 119 3 mayhem:interdimensional_tv[facing=south,screen=3]",
                "setblock -2 120 3 mayhem:wobble_shroom", "setblock 0 120 3 mayhem:flux_cluster", "tp @s 0 121 -3 0 25"));
        step(60, () -> shot("blocks"));
        // Scene 5: held portal gun + hotbar of gadgets
        step(5, () -> cmd("clear @s", "item replace entity @s hotbar.0 with mayhem:portal_gun", "item replace entity @s hotbar.1 with mayhem:omni_portal_gun",
                "item replace entity @s hotbar.2 with mayhem:size_ray", "item replace entity @s hotbar.3 with mayhem:plasma_blaster",
                "item replace entity @s hotbar.4 with mayhem:swapper", "item replace entity @s hotbar.5 with mayhem:pocket_black_hole",
                "item replace entity @s hotbar.6 with mayhem:mystery_jar", "item replace entity @s hotbar.7 with mayhem:flux_sword",
                "item replace entity @s hotbar.8 with mayhem:toaster_core", "item replace entity @s weapon.offhand with mayhem:coordinate_card"));
        step(10, () -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) mc.player.getInventory().selectedSlot = 0;
        });
        step(30, () -> shot("held_gun"));
        step(5, () -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) mc.player.getInventory().selectedSlot = 3;
        });
        step(20, () -> shot("held_blaster"));
        step(5, () -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            mc.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_FRONT);
            if (mc.player != null) mc.player.getInventory().selectedSlot = 0;
        });
        step(30, () -> shot("third_person_gun"));
        step(5, () -> MinecraftClient.getInstance().options.setPerspective(net.minecraft.client.option.Perspective.FIRST_PERSON));
        // Scene 6: crashed saucer
        step(5, () -> cmd("place feature mayhem:crashed_saucer 40 ~ 40", "tp @s 30 100 26 -40 35"));
        step(140, () -> shot("saucer"));
        // Scene 7: the Gloopverse
        step(5, () -> cmd("execute in mayhem:gloopverse run tp @s 128 170 0 0 30"));
        step(260, () -> shot("gloopverse"));
        step(5, () -> cmd("execute in mayhem:gloopverse run tp @s 0 175 0 135 20"));
        step(220, () -> shot("gloopverse2"));
        // Scene 8: the Meatverse
        step(5, () -> cmd("execute in mayhem:meatverse run tp @s 8 40 8 0 10"));
        step(260, () -> shot("meatverse"));
        step(5, () -> cmd("execute in mayhem:meatverse run tp @s 64 20 64 90 0"));
        step(220, () -> shot("meatverse2"));
        step(40, () -> MinecraftClient.getInstance().scheduleStop());
    }

    private static void tick(MinecraftClient mc) {
        // Click through the vanilla "experimental settings" warning that custom dimensions trigger.
        if (mc.currentScreen != null) {
            if (mc.world == null && (++screenTicks % 100) == 1) {
                MultiverseMayhem.LOGGER.info("[autotest] screen: {}", mc.currentScreen.getClass().getName());
            }
            for (var e : mc.currentScreen.children()) {
                if (e instanceof net.minecraft.client.gui.widget.ButtonWidget b && b.getMessage().getString().contains("know what")) {
                    MultiverseMayhem.LOGGER.info("[autotest] confirming experimental-settings prompt");
                    b.onPress();
                    return;
                }
            }
        }
        if (mc.world == null || mc.player == null || index >= STEPS.size()) return;
        if (--wait > 0) return;
        Step s = STEPS.get(index++);
        s.action().run();
        wait = index < STEPS.size() ? STEPS.get(index).delay() : 0;
    }

    private AutoTest() {
    }
}
