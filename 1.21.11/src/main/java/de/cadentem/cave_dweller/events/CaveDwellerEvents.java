package de.cadentem.cave_dweller.events;

import de.cadentem.cave_dweller.CaveDweller;
import de.cadentem.cave_dweller.config.ServerConfig;
import de.cadentem.cave_dweller.entities.CaveDwellerEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.concurrent.ConcurrentHashMap;

public class CaveDwellerEvents {
    private static final ConcurrentHashMap<Integer, Integer> HIT_COUNTER = new ConcurrentHashMap<>();

    public static void register() {
        NeoForge.EVENT_BUS.addListener(CaveDwellerEvents::onIncomingDamage);
    }

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof CaveDwellerEntity caveDweller)) return;

        DamageSource source = event.getSource();
        boolean isDrown = source.is(DamageTypes.DROWN);
        boolean isFell = source.is(DamageTypes.FELL_OUT_OF_WORLD);
        boolean isInWall = source.is(DamageTypes.IN_WALL);
        boolean isFall = source.is(DamageTypes.FALL);

        boolean skipDamage = isDrown || isFell || isInWall;
        if (skipDamage) {
            if ((isDrown || isFell) && !caveDweller.level().isClientSide()) {
                HIT_COUNTER.merge(caveDweller.getId(), 1, Integer::sum);
                if (HIT_COUNTER.get(caveDweller.getId()) > 5) {
                    HIT_COUNTER.remove(caveDweller.getId());
                    boolean couldTeleport = caveDweller.teleportToTarget();
                    caveDweller.hurtMarked = true;
                    if (!couldTeleport) {
                        String key = caveDweller.level().dimension().identifier().toString();
                        if (ServerConfig.isValidDimension(key)) {
                            int spawnDelta = (int) (ServerConfig.CAN_SPAWN_MIN * 0.3);
                            int noiseDelta = (int) (ServerConfig.RESET_NOISE_MIN * 0.3);
                            CaveDweller.speedUpTimers(key, spawnDelta, noiseDelta);
                        }
                        caveDweller.disappear();
                    }
                }
            }
            event.setCanceled(true);
            return;
        }
        if (isFall) event.setCanceled(true);
    }
}
