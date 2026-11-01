package com.ditsjohann.mayhem.client.render;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.util.function.Function;

/** Renders a full-bright emissive overlay (eyes, LEDs, visors) on top of a mob. */
public class GlowLayer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
    private final Function<T, Identifier> texture;

    public GlowLayer(FeatureRendererContext<T, M> context, Function<T, Identifier> texture) {
        super(context);
        this.texture = texture;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (entity.isInvisible()) return;
        getContextModel().render(matrices, vertexConsumers.getBuffer(RenderLayer.getEyes(texture.apply(entity))), 0xF00000, OverlayTexture.DEFAULT_UV);
    }
}
