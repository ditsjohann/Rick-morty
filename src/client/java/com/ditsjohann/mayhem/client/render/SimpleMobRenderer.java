package com.ditsjohann.mayhem.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class SimpleMobRenderer<T extends MobEntity, M extends EntityModel<T>> extends MobEntityRenderer<T, M> {
    private final Function<T, Identifier> texture;
    private final float scale;

    public SimpleMobRenderer(EntityRendererFactory.Context ctx, M model, float shadow, Function<T, Identifier> texture,
                             @Nullable Function<T, Identifier> glow, float scale) {
        super(ctx, model, shadow);
        this.texture = texture;
        this.scale = scale;
        if (glow != null) addFeature(new GlowLayer<>(this, glow));
    }

    @Override
    public Identifier getTexture(T entity) {
        return texture.apply(entity);
    }

    @Override
    protected void scale(T entity, MatrixStack matrices, float amount) {
        if (scale != 1.0f) matrices.scale(scale, scale, scale);
    }
}
