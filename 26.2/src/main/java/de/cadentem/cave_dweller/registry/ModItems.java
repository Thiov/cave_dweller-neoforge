package de.cadentem.cave_dweller.registry;

import de.cadentem.cave_dweller.CaveDweller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CaveDweller.MODID);

    public static final DeferredItem<Item> CAVE_DWELLER_SPAWN_EGG = ITEMS.registerItem("cave_dweller_spawn_egg",
            properties -> new SpawnEggItem(properties.spawnEgg(ModEntityTypes.CAVE_DWELLER.get())));
}
