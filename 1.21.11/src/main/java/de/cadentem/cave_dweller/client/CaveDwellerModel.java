package de.cadentem.cave_dweller.client;

import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import de.cadentem.cave_dweller.entities.CaveDwellerEntity;
import net.minecraft.resources.Identifier;

public class CaveDwellerModel extends GeoModel<CaveDwellerEntity> {
    private static final Identifier MODEL_ID = Identifier.fromNamespaceAndPath("cave_dweller", "cave_dweller");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("cave_dweller", "textures/entity/cave_dweller_texture.png");

    @Override
    public Identifier getModelResource(GeoRenderState state) { return MODEL_ID; }

    @Override
    public Identifier getTextureResource(GeoRenderState state) { return TEXTURE; }

    @Override
    public Identifier getAnimationResource(CaveDwellerEntity animatable) { return MODEL_ID; }
}
