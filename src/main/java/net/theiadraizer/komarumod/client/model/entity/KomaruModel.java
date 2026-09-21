package net.theiadraizer.komarumod.client.model.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.theiadraizer.komarumod.entity.KomaruEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class KomaruModel extends GeoModel<KomaruEntity> {
    public KomaruModel() {
    }

    public ResourceLocation getModelResource(KomaruEntity animatable) {
        return new ResourceLocation("komarumod", "geo/komaru.geo.json");
    }

    public ResourceLocation getTextureResource(KomaruEntity animatable) {
        return new ResourceLocation("komarumod", "textures/entity/komaru_texture.png");
    }

    public ResourceLocation getAnimationResource(KomaruEntity animatable) {
        return new ResourceLocation("komarumod", "animations/komaru.animation.json");
    }

    @Override
    public void setCustomAnimations(KomaruEntity animatable, long instanceId, AnimationState<KomaruEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        CoreGeoBone head = this.getAnimationProcessor().getBone("head");
        if (head == null) {
            return;
        }

        if (!animatable.isSleeping() && !animatable.isLying()) {
            EntityModelData extraData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            int unpausedMultiplier = Minecraft.getInstance().isPaused() && !animatable.shouldPlayAnimsWhileGamePaused() ? 0 : 1;
            if (extraData != null) {
                head.setRotX(extraData.headPitch() * Mth.DEG_TO_RAD * (float) unpausedMultiplier);
                head.setRotY(extraData.netHeadYaw() * Mth.DEG_TO_RAD * (float) unpausedMultiplier);
            }
        }
    }
}
