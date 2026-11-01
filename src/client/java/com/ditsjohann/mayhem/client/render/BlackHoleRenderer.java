package com.ditsjohann.mayhem.client.render;

import com.ditsjohann.mayhem.MultiverseMayhem;
import com.ditsjohann.mayhem.entity.BlackHoleEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/** Event horizon (billboarded) wrapped in a tilted, spinning accretion disk. */
public class BlackHoleRenderer extends EntityRenderer<BlackHoleEntity> {
    private static final Identifier HOLE = MultiverseMayhem.id("textures/entity/black_hole.png");
    private static final Identifier DISK = MultiverseMayhem.id("textures/entity/accretion_disk.png");

    public BlackHoleRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 0;
    }

    @Override
    public void render(BlackHoleEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp, int light) {
        float p = entity.getPower(tickDelta);
        if (p > 0.01f) {
            float t = entity.age + tickDelta;
            matrices.push();
            matrices.translate(0, 0.5, 0);
            // Accretion disk
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(70));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(t * 9f));
            matrices.scale(2.6f * p, 2.6f * p, 1);
            RenderUtil.quad(matrices, vcp.getBuffer(RenderLayer.getEntityTranslucentEmissive(DISK)), 0, RenderUtil.argb(240, 0xFFFFFF));
            matrices.pop();
            // Event horizon, always facing the camera
            matrices.push();
            matrices.multiply(this.dispatcher.getRotation());
            float s = 1.05f * p * (1.0f + (float) Math.sin(t * 0.6f) * 0.04f);
            matrices.scale(s, s, s);
            RenderUtil.quad(matrices, vcp.getBuffer(RenderLayer.getEntityTranslucent(HOLE)), 0, RenderUtil.argb(255, 0xFFFFFF));
            matrices.pop();
            matrices.pop();
        }
        super.render(entity, yaw, tickDelta, matrices, vcp, light);
    }

    @Override
    public Identifier getTexture(BlackHoleEntity entity) {
        return HOLE;
    }
}
