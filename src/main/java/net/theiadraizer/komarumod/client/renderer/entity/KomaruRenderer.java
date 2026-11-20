package net.theiadraizer.komarumod.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.theiadraizer.komarumod.client.model.entity.KomaruModel;
import net.theiadraizer.komarumod.entity.KomaruEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class KomaruRenderer extends GeoEntityRenderer<KomaruEntity> {
    public KomaruRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new KomaruModel());
        this.shadowRadius = 0.3F;
    }

    @Override
    public void preRender(
            PoseStack poseStack,
            KomaruEntity animatable,
            BakedGeoModel model,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha) {
        if (animatable.isBaby()) {
            poseStack.scale(0.4F, 0.4F, 0.4F);
        } else {
            poseStack.scale(0.7F, 0.7F, 0.7F);
        }

        super.preRender(
                poseStack,
                animatable,
                model,
                bufferSource,
                buffer,
                isReRender,
                partialTick,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha);
    }
}
