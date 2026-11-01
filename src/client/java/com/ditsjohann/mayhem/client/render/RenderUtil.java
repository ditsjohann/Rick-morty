package com.ditsjohann.mayhem.client.render;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

final class RenderUtil {
    /** A double-sided unit quad spanning [-1,1] on X/Y at depth z, full-bright. */
    static void quad(MatrixStack matrices, VertexConsumer vc, float z, int argb) {
        MatrixStack.Entry e = matrices.peek();
        v(e, vc, -1, -1, z, 0, 1, argb, 1);
        v(e, vc, 1, -1, z, 1, 1, argb, 1);
        v(e, vc, 1, 1, z, 1, 0, argb, 1);
        v(e, vc, -1, 1, z, 0, 0, argb, 1);
        v(e, vc, -1, 1, z, 0, 0, argb, -1);
        v(e, vc, 1, 1, z, 1, 0, argb, -1);
        v(e, vc, 1, -1, z, 1, 1, argb, -1);
        v(e, vc, -1, -1, z, 0, 1, argb, -1);
    }

    private static void v(MatrixStack.Entry e, VertexConsumer vc, float x, float y, float z, float u, float vv, int argb, float nz) {
        vc.vertex(e, x, y, z).color(argb).texture(u, vv).overlay(OverlayTexture.DEFAULT_UV)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(e, 0, 0, nz);
    }

    static int argb(int a, int rgb) {
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    private RenderUtil() {
    }
}
