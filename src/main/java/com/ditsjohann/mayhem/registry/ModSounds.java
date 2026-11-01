package com.ditsjohann.mayhem.registry;

import com.ditsjohann.mayhem.MultiverseMayhem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModSounds {
    public static final SoundEvent PORTAL_GUN_FIRE = register("item.portal_gun.fire");
    public static final SoundEvent PORTAL_GUN_EMPTY = register("item.portal_gun.empty");
    public static final SoundEvent PORTAL_GUN_MODE = register("item.portal_gun.mode");
    public static final SoundEvent PORTAL_OPEN = register("entity.portal.open");
    public static final SoundEvent PORTAL_CLOSE = register("entity.portal.close");
    public static final SoundEvent PORTAL_TRAVEL = register("entity.portal.travel");
    public static final SoundEvent PLASMA_SHOT = register("item.plasma_blaster.fire");
    public static final SoundEvent SIZE_RAY = register("item.size_ray.zap");
    public static final SoundEvent SWAPPER = register("item.swapper.swap");
    public static final SoundEvent BLACK_HOLE_HUM = register("entity.black_hole.hum");
    public static final SoundEvent BLACK_HOLE_COLLAPSE = register("entity.black_hole.collapse");
    public static final SoundEvent JAR_OPEN = register("item.mystery_jar.open");
    public static final SoundEvent TV_CHANNEL = register("block.interdimensional_tv.channel");

    public static final SoundEvent GLOOPER_AMBIENT = register("entity.glooper.ambient");
    public static final SoundEvent GLOOPER_HURT = register("entity.glooper.hurt");
    public static final SoundEvent GLOOPER_DEATH = register("entity.glooper.death");
    public static final SoundEvent CRAWLER_AMBIENT = register("entity.meat_crawler.ambient");
    public static final SoundEvent CRAWLER_HURT = register("entity.meat_crawler.hurt");
    public static final SoundEvent CRAWLER_DEATH = register("entity.meat_crawler.death");
    public static final SoundEvent WATCHER_AMBIENT = register("entity.watcher.ambient");
    public static final SoundEvent WATCHER_HURT = register("entity.watcher.hurt");
    public static final SoundEvent WATCHER_DEATH = register("entity.watcher.death");
    public static final SoundEvent WATCHER_SHOOT = register("entity.watcher.shoot");
    public static final SoundEvent AGENT_AMBIENT = register("entity.customs_agent.ambient");
    public static final SoundEvent AGENT_HURT = register("entity.customs_agent.hurt");
    public static final SoundEvent AGENT_DEATH = register("entity.customs_agent.death");
    public static final SoundEvent AGENT_ALERT = register("entity.customs_agent.alert");
    public static final SoundEvent ZORP_AMBIENT = register("entity.zorp.ambient");
    public static final SoundEvent ZORP_YES = register("entity.zorp.yes");
    public static final SoundEvent ZORP_NO = register("entity.zorp.no");
    public static final SoundEvent TOASTER_POP = register("entity.defense_toaster.pop");
    public static final SoundEvent TOASTER_AMBIENT = register("entity.defense_toaster.ambient");
    public static final SoundEvent TOASTER_HURT = register("entity.defense_toaster.hurt");
    public static final SoundEvent LEECH_ATTACH = register("entity.thought_leech.attach");
    public static final SoundEvent LEECH_AMBIENT = register("entity.thought_leech.ambient");
    public static final SoundEvent OMNI_ROAR = register("entity.omni_eye.roar");
    public static final SoundEvent OMNI_HURT = register("entity.omni_eye.hurt");
    public static final SoundEvent OMNI_DEATH = register("entity.omni_eye.death");
    public static final SoundEvent OMNI_LASER = register("entity.omni_eye.laser");

    public static final RegistryEntry.Reference<SoundEvent> MUSIC_DISC_JAM = registerReference("music_disc.interdimensional_jam");

    private static SoundEvent register(String name) {
        Identifier id = MultiverseMayhem.id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    private static RegistryEntry.Reference<SoundEvent> registerReference(String name) {
        Identifier id = MultiverseMayhem.id(name);
        return Registry.registerReference(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void init() {
    }

    private ModSounds() {
    }
}
