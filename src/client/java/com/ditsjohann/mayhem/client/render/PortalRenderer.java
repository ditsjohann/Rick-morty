package com.ditsjohann.mayhem.client.render;

import com.ditsjohann.mayhem.MultiverseMayhem;
import com.ditsjohann.mayhem.entity.PortalEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/** Layered, counter-rotating swirls squashed into an oval. Glows in the dark. */
public class PortalRenderer extends EntityRenderer<PortalEntity> {
    private static final Identifier SWIRL = MultiverseMayhem.id("textures/entity/portal_swirl.png");
    private static final Identifier RIM = MultiverseMayhem.id("textures/entity/portal_rim.png");
    private static final Identifier CORE = MultiverseMayhem.id("textures/entity/portal_core.png");

    public PortalRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 0;
    }

    private static int[] palette(int variant) {
        return switch (variant) {
            case PortalEntity.VARIANT_OMNI -> new int[]{0xC060FF, 0x8A2BE2, 0xF0C8FF};
            case PortalEntity.VARIANT_MEAT -> new int[]{0xFF4060, 0xA01030, 0xFFC0C8};
            default -> new int[]{0x7CFF3A, 0x2FBF2A, 0xE8FFB0};
        };
    }

    @Override
    public void render(PortalEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp, int light) {
        float open = entity.getOpenness(tickDelta);
        if (open > 0.01f) {
            float t = entity.age + tickDelta;
            float pulse = 1.0f + MathHelper.sin(t * 0.25f) * 0.03f;
            float w = 0.68f * open * pulse, h = 1.12f * open * pulse;
            int[] pal = palette(entity.getVariant());
            matrices.push();
            matrices.translate(0, 1.15, 0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-entity.getYaw()));

            // outer rim glow
            matrices.push();
            matrices.scale(w * 1.18f, h * 1.1f, 1);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(t * 2f));
            RenderUtil.quad(matrices, vcp.getBuffer(RenderLayer.getEntityTranslucentEmissive(RIM)), 0, RenderUtil.argb(230, pal[0]));
            matrices.pop();

            VertexConsumer swirl = vcp.getBuffer(RenderLayer.getEntityTranslucentEmissive(SWIRL));
            float[] speeds = {7f, -11f, 16f};
            float[] sizes = {1.0f, 0.78f, 0.55f};
            int[] colors = {pal[1], pal[0], pal[2]};
            for (int i = 0; i < 3; i++) {
                matrices.push();
                matrices.scale(w * sizes[i], h * sizes[i], 1);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(t * speeds[i]));
                RenderUtil.quad(matrices, swirl, 0.004f * (i + 1), RenderUtil.argb(235, colors[i]));
                RenderUtil.quad(matrices, swirl, -0.004f * (i + 1), RenderUtil.argb(235, colors[i]));
                matrices.pop();
            }
            matrices.push();
            matrices.scale(w * 0.35f, h * 0.35f, 1);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-t * 20f));
            RenderUtil.quad(matrices, vcp.getBuffer(RenderLayer.getEntityTranslucentEmissive(CORE)), 0.02f, RenderUtil.argb(255, 0xFFFFFF));
            RenderUtil.quad(matrices, vcp.getBuffer(RenderLayer.getEntityTranslucentEmissive(CORE)), -0.02f, RenderUtil.argb(255, 0xFFFFFF));
            matrices.pop();
            matrices.pop();
        }
        super.render(entity, yaw, tickDelta, matrices, vcp, light);
    }

    @Override
    public Identifier getTexture(PortalEntity entity) {
        return SWIRL;
    }
}
