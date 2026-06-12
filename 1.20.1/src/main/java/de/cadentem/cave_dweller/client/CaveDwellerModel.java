package de.cadentem.cave_dweller.client;

import de.cadentem.cave_dweller.entities.CaveDwellerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class CaveDwellerModel extends GeoModel<CaveDwellerEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation("cave_dweller", "geo/cave_dweller.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation("cave_dweller", "textures/entity/cave_dweller_texture.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation("cave_dweller", "animations/cave_dweller.animation.json");

    @Override
    public ResourceLocation getModelResource(CaveDwellerEntity ignored) { return MODEL; }

    @Override
    public ResourceLocation getTextureResource(CaveDwellerEntity ignored) { return TEXTURE; }

    @Override
    public ResourceLocation getAnimationResource(CaveDwellerEntity ignored) { return ANIMATION; }

    @Override
    public void setCustomAnimations(CaveDwellerEntity animatable, long instanceId, AnimationState<CaveDwellerEntity> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");

        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * ((float) (Math.PI / 180.0)));
            head.setRotY(entityData.netHeadYaw() * (float) (Math.PI / 180.0));
        }

        super.setCustomAnimations(animatable, instanceId, animationState);
    }
}
