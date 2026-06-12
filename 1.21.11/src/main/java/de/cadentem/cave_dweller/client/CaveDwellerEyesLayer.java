package de.cadentem.cave_dweller.client;

import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import de.cadentem.cave_dweller.entities.CaveDwellerEntity;
import net.minecraft.resources.Identifier;

public class CaveDwellerEyesLayer extends GeoRenderLayer<CaveDwellerEntity, Void, CaveDwellerRenderState> {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            "cave_dweller", "textures/entity/cave_dweller_eyes_texture.png");

    public CaveDwellerEyesLayer(GeoRenderer<CaveDwellerEntity, Void, CaveDwellerRenderState> renderer) {
        super(renderer);
    }

    @Override
    protected Identifier getTextureResource(CaveDwellerRenderState renderState) {
        return TEXTURE;
    }
}
