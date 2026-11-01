package com.ditsjohann.mayhem.client;

import com.ditsjohann.mayhem.MultiverseMayhem;
import com.ditsjohann.mayhem.client.model.*;
import com.ditsjohann.mayhem.client.render.BlackHoleRenderer;
import com.ditsjohann.mayhem.client.render.GlowLayer;
import com.ditsjohann.mayhem.client.render.PortalRenderer;
import com.ditsjohann.mayhem.client.render.SimpleMobRenderer;
import com.ditsjohann.mayhem.entity.mob.CustomsAgentEntity;
import com.ditsjohann.mayhem.entity.mob.GlooperEntity;
import com.ditsjohann.mayhem.registry.ModBlocks;
import com.ditsjohann.mayhem.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class MultiverseMayhemClient implements ClientModInitializer {
    public static final EntityModelLayer CUSTOMS_AGENT_LAYER = new EntityModelLayer(MultiverseMayhem.id("customs_agent"), "main");

    private static Identifier tex(String name) {
        return MultiverseMayhem.id("textures/entity/" + name + ".png");
    }

    @Override
    public void onInitializeClient() {
        com.ditsjohann.mayhem.client.dev.AutoTest.init();
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GLOOP_BLOCK, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WOBBLE_SHROOM, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.FLUX_CLUSTER, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.INTERDIMENSIONAL_TV, RenderLayer.getCutout());

        EntityModelLayerRegistry.registerModelLayer(GlooperModel.LAYER, GlooperModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(MeatCrawlerModel.LAYER, MeatCrawlerModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(WatcherModel.LAYER, WatcherModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(ZorpModel.LAYER, ZorpModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(DefenseToasterModel.LAYER, DefenseToasterModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(ThoughtLeechModel.LAYER, ThoughtLeechModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(OmniEyeModel.LAYER, OmniEyeModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(CUSTOMS_AGENT_LAYER, () -> TexturedModelData.of(BipedEntityModel.getModelData(Dilation.NONE, 0.0f), 64, 64));

        EntityRendererRegistry.register(ModEntities.PORTAL, PortalRenderer::new);
        EntityRendererRegistry.register(ModEntities.BLACK_HOLE, BlackHoleRenderer::new);
        EntityRendererRegistry.register(ModEntities.PORTAL_SHOT, ctx -> new FlyingItemEntityRenderer<>(ctx, 1.2f, true));
        EntityRendererRegistry.register(ModEntities.PLASMA_BOLT, ctx -> new FlyingItemEntityRenderer<>(ctx, 0.9f, true));
        EntityRendererRegistry.register(ModEntities.EYE_BEAM, ctx -> new FlyingItemEntityRenderer<>(ctx, 1.0f, true));
        EntityRendererRegistry.register(ModEntities.TOAST, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.THROWN_BLACK_HOLE, ctx -> new FlyingItemEntityRenderer<>(ctx, 1.0f, true));

        Identifier[] glooperTex = {tex("glooper_purple"), tex("glooper_lime"), tex("glooper_cyan")};
        EntityRendererRegistry.register(ModEntities.GLOOPER, ctx -> new SimpleMobRenderer<GlooperEntity, GlooperModel>(ctx,
                new GlooperModel(ctx.getPart(GlooperModel.LAYER)), 0.5f, e -> glooperTex[Math.floorMod(e.getVariant(), glooperTex.length)], null, 1.0f));
        EntityRendererRegistry.register(ModEntities.MEAT_CRAWLER, ctx -> new SimpleMobRenderer<>(ctx,
                new MeatCrawlerModel(ctx.getPart(MeatCrawlerModel.LAYER)), 0.7f, e -> tex("meat_crawler"), e -> tex("meat_crawler_glow"), 1.0f));
        EntityRendererRegistry.register(ModEntities.WATCHER, ctx -> new SimpleMobRenderer<>(ctx,
                new WatcherModel(ctx.getPart(WatcherModel.LAYER)), 0.4f, e -> tex("watcher"), e -> tex("watcher_glow"), 1.0f));
        EntityRendererRegistry.register(ModEntities.ZORP, ctx -> new SimpleMobRenderer<>(ctx,
                new ZorpModel(ctx.getPart(ZorpModel.LAYER)), 0.5f, e -> tex("zorp"), e -> tex("zorp_glow"), 1.0f));
        EntityRendererRegistry.register(ModEntities.DEFENSE_TOASTER, ctx -> new SimpleMobRenderer<>(ctx,
                new DefenseToasterModel(ctx.getPart(DefenseToasterModel.LAYER)), 0.45f, e -> tex("defense_toaster"), e -> tex("defense_toaster_glow"), 1.0f));
        EntityRendererRegistry.register(ModEntities.THOUGHT_LEECH, ctx -> new SimpleMobRenderer<>(ctx,
                new ThoughtLeechModel(ctx.getPart(ThoughtLeechModel.LAYER)), 0.25f, e -> tex("thought_leech"), e -> tex("thought_leech_glow"), 1.0f));
        EntityRendererRegistry.register(ModEntities.OMNI_EYE, ctx -> new SimpleMobRenderer<>(ctx,
                new OmniEyeModel(ctx.getPart(OmniEyeModel.LAYER)), 1.6f,
                e -> e.isEnraged() ? tex("omni_eye_enraged") : tex("omni_eye"),
                e -> e.isEnraged() ? tex("omni_eye_enraged_glow") : tex("omni_eye_glow"), 1.25f));
        EntityRendererRegistry.register(ModEntities.CUSTOMS_AGENT, ctx -> {
            return new BipedEntityRenderer<CustomsAgentEntity, BipedEntityModel<CustomsAgentEntity>>(ctx,
                    new BipedEntityModel<>(ctx.getPart(CUSTOMS_AGENT_LAYER)), 0.5f) {
                {
                    addFeature(new GlowLayer<>(this, e -> tex("customs_agent_glow")));
                }

                @Override
                public Identifier getTexture(CustomsAgentEntity entity) {
                    return tex("customs_agent");
                }
            };
        });
    }
}
