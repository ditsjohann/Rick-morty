package com.ditsjohann.mayhem.registry;

import com.ditsjohann.mayhem.MultiverseMayhem;
import com.ditsjohann.mayhem.entity.BlackHoleEntity;
import com.ditsjohann.mayhem.entity.PortalEntity;
import com.ditsjohann.mayhem.entity.mob.*;
import com.ditsjohann.mayhem.entity.projectile.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.Heightmap;

public final class ModEntities {
    public static final EntityType<PortalEntity> PORTAL = register("portal",
            EntityType.Builder.<PortalEntity>create(PortalEntity::new, SpawnGroup.MISC).dimensions(1.3f, 2.3f).maxTrackingRange(10).trackingTickInterval(20).makeFireImmune());
    public static final EntityType<BlackHoleEntity> BLACK_HOLE = register("black_hole",
            EntityType.Builder.<BlackHoleEntity>create(BlackHoleEntity::new, SpawnGroup.MISC).dimensions(1.0f, 1.0f).maxTrackingRange(10).trackingTickInterval(10).makeFireImmune());

    public static final EntityType<PortalShotEntity> PORTAL_SHOT = projectile("portal_shot", PortalShotEntity::new);
    public static final EntityType<PlasmaBoltEntity> PLASMA_BOLT = projectile("plasma_bolt", PlasmaBoltEntity::new);
    public static final EntityType<ToastEntity> TOAST = projectile("toast", ToastEntity::new);
    public static final EntityType<EyeBeamEntity> EYE_BEAM = projectile("eye_beam", EyeBeamEntity::new);
    public static final EntityType<ThrownBlackHoleEntity> THROWN_BLACK_HOLE = projectile("thrown_black_hole", ThrownBlackHoleEntity::new);

    public static final EntityType<GlooperEntity> GLOOPER = register("glooper",
            EntityType.Builder.create(GlooperEntity::new, SpawnGroup.CREATURE).dimensions(0.9f, 0.85f).eyeHeight(0.6f).maxTrackingRange(10));
    public static final EntityType<MeatCrawlerEntity> MEAT_CRAWLER = register("meat_crawler",
            EntityType.Builder.create(MeatCrawlerEntity::new, SpawnGroup.MONSTER).dimensions(1.2f, 0.7f).eyeHeight(0.5f).maxTrackingRange(8));
    public static final EntityType<WatcherEntity> WATCHER = register("watcher",
            EntityType.Builder.create(WatcherEntity::new, SpawnGroup.MONSTER).dimensions(0.9f, 0.9f).eyeHeight(0.45f).maxTrackingRange(10).makeFireImmune());
    public static final EntityType<CustomsAgentEntity> CUSTOMS_AGENT = register("customs_agent",
            EntityType.Builder.create(CustomsAgentEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.95f).eyeHeight(1.62f).maxTrackingRange(10));
    public static final EntityType<ZorpTraderEntity> ZORP = register("zorp",
            EntityType.Builder.create(ZorpTraderEntity::new, SpawnGroup.CREATURE).dimensions(0.7f, 2.3f).eyeHeight(2.0f).maxTrackingRange(10));
    public static final EntityType<DefenseToasterEntity> DEFENSE_TOASTER = register("defense_toaster",
            EntityType.Builder.create(DefenseToasterEntity::new, SpawnGroup.MISC).dimensions(0.75f, 0.8f).eyeHeight(0.55f).maxTrackingRange(10));
    public static final EntityType<ThoughtLeechEntity> THOUGHT_LEECH = register("thought_leech",
            EntityType.Builder.create(ThoughtLeechEntity::new, SpawnGroup.MONSTER).dimensions(0.5f, 0.35f).eyeHeight(0.2f).maxTrackingRange(8));
    public static final EntityType<OmniEyeEntity> OMNI_EYE = register("omni_eye",
            EntityType.Builder.create(OmniEyeEntity::new, SpawnGroup.MONSTER).dimensions(2.6f, 2.6f).eyeHeight(1.3f).maxTrackingRange(16).makeFireImmune());

    private static <T extends net.minecraft.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        return Registry.register(Registries.ENTITY_TYPE, MultiverseMayhem.id(name), builder.build(name));
    }

    private static <T extends net.minecraft.entity.Entity> EntityType<T> projectile(String name, EntityType.EntityFactory<T> factory) {
        return register(name, EntityType.Builder.create(factory, SpawnGroup.MISC).dimensions(0.3f, 0.3f).maxTrackingRange(6).trackingTickInterval(10));
    }

    public static void init() {
        FabricDefaultAttributeRegistry.register(GLOOPER, GlooperEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(MEAT_CRAWLER, MeatCrawlerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(WATCHER, WatcherEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CUSTOMS_AGENT, CustomsAgentEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ZORP, ZorpTraderEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(DEFENSE_TOASTER, DefenseToasterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(THOUGHT_LEECH, ThoughtLeechEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(OMNI_EYE, OmniEyeEntity.createAttributes());

        SpawnRestriction.register(GLOOPER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, GlooperEntity::canSpawn);
        SpawnRestriction.register(MEAT_CRAWLER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MeatCrawlerEntity::canSpawn);
        SpawnRestriction.register(WATCHER, SpawnLocationTypes.UNRESTRICTED, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, WatcherEntity::canSpawn);
        SpawnRestriction.register(THOUGHT_LEECH, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ThoughtLeechEntity::canSpawn);
        SpawnRestriction.register(ZORP, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canMobSpawn);
    }

    private ModEntities() {
    }
}
