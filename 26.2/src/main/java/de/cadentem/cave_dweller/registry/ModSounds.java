package de.cadentem.cave_dweller.registry;

import de.cadentem.cave_dweller.CaveDweller;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, CaveDweller.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> CAVENOISE_1 = register("cavenoise_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAVENOISE_2 = register("cavenoise_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAVENOISE_3 = register("cavenoise_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAVENOISE_4 = register("cavenoise_4");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHASE_STEP_1 = register("chase_step_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHASE_STEP_2 = register("chase_step_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHASE_STEP_3 = register("chase_step_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHASE_STEP_4 = register("chase_step_4");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHASE_1 = register("chase_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHASE_2 = register("chase_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHASE_3 = register("chase_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHASE_4 = register("chase_4");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLEE_1 = register("flee_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLEE_2 = register("flee_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPOTTED = register("spotted");
    public static final DeferredHolder<SoundEvent, SoundEvent> DISAPPEAR = register("disappear");
    public static final DeferredHolder<SoundEvent, SoundEvent> DWELLER_HURT_1 = register("dweller_hurt_1");
    public static final DeferredHolder<SoundEvent, SoundEvent> DWELLER_HURT_2 = register("dweller_hurt_2");
    public static final DeferredHolder<SoundEvent, SoundEvent> DWELLER_HURT_3 = register("dweller_hurt_3");
    public static final DeferredHolder<SoundEvent, SoundEvent> DWELLER_HURT_4 = register("dweller_hurt_4");
    public static final DeferredHolder<SoundEvent, SoundEvent> DWELLER_DEATH = register("dweller_death");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () ->
                SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CaveDweller.MODID, name)));
    }
}
