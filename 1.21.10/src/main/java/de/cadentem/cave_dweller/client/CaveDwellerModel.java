package de.cadentem.cave_dweller.client;

import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import de.cadentem.cave_dweller.entities.CaveDwellerEntity;
import net.minecraft.resources.ResourceLocation;

public class CaveDwellerModel extends GeoModel<CaveDwellerEntity> {
    private static final ResourceLocation MODEL_ID = ResourceLocation.fromNamespaceAndPath("cave_dweller", "cave_dweller");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("cave_dweller", "textures/entity/cave_dweller_texture.png");

    @Override
    public ResourceLocation getModelResource(GeoRenderState state) { return MODEL_ID; }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState state) { return TEXTURE; }

    @Override
    public ResourceLocation getAnimationResource(CaveDwellerEntity animatable) { return MODEL_ID; }
}
