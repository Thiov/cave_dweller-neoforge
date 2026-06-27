package de.cadentem.cave_dweller.client;

import com.geckolib.renderer.GeoEntityRenderer;
import de.cadentem.cave_dweller.entities.CaveDwellerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CaveDwellerRenderer extends GeoEntityRenderer<CaveDwellerEntity, CaveDwellerRenderState> {
    public CaveDwellerRenderer(EntityRendererProvider.Context context) {
        super(context, new CaveDwellerModel());
        this.shadowRadius = 0.3F;
        withRenderLayer(new CaveDwellerEyesLayer(this));
    }
}
