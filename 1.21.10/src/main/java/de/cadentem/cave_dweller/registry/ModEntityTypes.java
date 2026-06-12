package de.cadentem.cave_dweller.registry;

import de.cadentem.cave_dweller.CaveDweller;
import de.cadentem.cave_dweller.entities.CaveDwellerEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, CaveDweller.MODID);

    public static final ResourceKey<EntityType<?>> CAVE_DWELLER_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(CaveDweller.MODID, "cave_dweller"));

    public static final DeferredHolder<EntityType<?>, EntityType<CaveDwellerEntity>> CAVE_DWELLER =
            ENTITY_TYPES.register("cave_dweller", () ->
                    EntityType.Builder.<CaveDwellerEntity>of(CaveDwellerEntity::new, MobCategory.MONSTER)
                            .sized(0.5F, 2.7F)
                            .build(CAVE_DWELLER_KEY));
}
