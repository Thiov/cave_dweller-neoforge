package de.cadentem.cave_dweller.util;

import de.cadentem.cave_dweller.CaveDweller;
import de.cadentem.cave_dweller.config.ServerConfig;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class Timer {
    @Nullable public Entity currentVictim;
    public int currentSpawn;
    public int currentNoise;
    public int targetSpawn;
    public int targetNoise;

    public Timer() {
        resetSpawnTimer();
        resetNoiseTimer();
    }

    public boolean isSpawnTimerReached() {
        return Utils.isOnSurface(this.currentVictim)
                ? this.currentSpawn >= (int) (this.targetSpawn * ServerConfig.SURFACE_TIMER_MULTIPLIER)
                : this.currentSpawn >= this.targetSpawn;
    }

    public boolean isNoiseTimerReached() {
        return Utils.isOnSurface(this.currentVictim)
                ? this.currentNoise >= (int) (this.targetNoise * ServerConfig.SURFACE_TIMER_MULTIPLIER)
                : this.currentNoise >= this.targetNoise;
    }

    public void resetNoiseTimer() {
        int min = ServerConfig.RESET_NOISE_MIN;
        int max = ServerConfig.RESET_NOISE_MAX;
        if (max < min) {
            int t = min; min = max; max = t;
            CaveDweller.LOG.error("RESET_NOISE: max smaller than min, swapped");
        }
        this.currentNoise = 0;
        this.targetNoise = CaveDweller.RANDOM.nextInt(Utils.secondsToTicks(min), Utils.secondsToTicks(max + 1));
    }

    public void resetSpawnTimer() {
        int spawnTimer;
        if (CaveDweller.RANDOM.nextDouble() <= ServerConfig.CAN_SPAWN_COOLDOWN_CHANCE) {
            spawnTimer = Utils.secondsToTicks(ServerConfig.CAN_SPAWN_COOLDOWN);
        } else {
            int min = ServerConfig.CAN_SPAWN_MIN;
            int max = ServerConfig.CAN_SPAWN_MAX;
            if (max < min) {
                int t = min; min = max; max = t;
                CaveDweller.LOG.error("RESET_CALM: max smaller than min, swapped");
            }
            spawnTimer = CaveDweller.RANDOM.nextInt(Utils.secondsToTicks(min), Utils.secondsToTicks(max + 1));
        }
        this.currentSpawn = 0;
        this.targetSpawn = spawnTimer;
    }

    @Override
    public String toString() {
        String name = this.currentVictim != null ? this.currentVictim.getName().getString() : "NONE";
        return name + " | " + this.currentSpawn + "/" + this.targetSpawn + " | " + this.currentNoise + "/" + this.targetNoise;
    }
}
