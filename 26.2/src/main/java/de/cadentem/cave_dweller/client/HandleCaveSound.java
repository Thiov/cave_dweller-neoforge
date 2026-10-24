package de.cadentem.cave_dweller.client;

import de.cadentem.cave_dweller.CaveDweller;
import de.cadentem.cave_dweller.network.CaveSoundPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class HandleCaveSound {
    public static void handle(CaveSoundPayload packet) {
        SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.getValue(packet.soundResource());
        if (soundEvent == null) {
            CaveDweller.LOG.error("Sound Event [{}] was null while handling packet", packet.soundResource());
            return;
        }
        Minecraft.getInstance().getSoundManager().play(
                new SimpleSoundInstance(soundEvent, SoundSource.AMBIENT,
                        2.0F, 1.0F, RandomSource.create(), packet.playerPosition()));
    }
}
