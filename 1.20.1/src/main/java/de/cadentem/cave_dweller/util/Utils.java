package de.cadentem.cave_dweller.util;

import de.cadentem.cave_dweller.CaveDweller;
import de.cadentem.cave_dweller.config.ServerConfig;
import de.cadentem.cave_dweller.entities.CaveDwellerEntity;
import net.minecraftforge.common.Tags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Utils {
    public static int ticksToSeconds(int ticks) { return ticks / 20; }
    public static int secondsToTicks(int seconds) { return seconds * 20; }
    public static int minutesToTicks(int minutes) { return secondsToTicks(minutes * 60); }

    public static String getTextureAppend() { return ""; }

    public static boolean isValidPlayer(Entity entity) {
        if (!(entity instanceof Player player)) return false;
        if (!player.isAlive()) return false;
        if (!ServerConfig.TARGET_INVISIBLE && player.isInvisible()) return false;
        return !player.isCreative() && !player.isSpectator();
    }

    public static LivingEntity getValidTarget(@NotNull CaveDwellerEntity caveDweller) {
        return caveDweller.level().getNearestPlayer(
                caveDweller.position().x, caveDweller.position().y, caveDweller.position().z,
                128.0, Utils::isValidPlayer);
    }

    public static boolean isOnSurface(@Nullable Entity entity) {
        if (entity == null) return false;
        if (entity.level() instanceof ServerLevel serverLevel) {
            BlockPos blockPosition = entity.blockPosition();
            if (serverLevel.canSeeSky(blockPosition)) return true;
            Holder<Biome> biome = serverLevel.getBiome(blockPosition);
            if (biome.is(Tags.Biomes.IS_CAVE) || biome.is(Tags.Biomes.IS_UNDERGROUND)) return false;
            int baseSkyLightLevel = serverLevel.getBrightness(LightLayer.SKY, blockPosition) - serverLevel.getSkyDarken();
            return baseSkyLightLevel > 0;
        }
        return false;
    }

    public static <T extends Mob> Optional<T> trySpawnMob(
            @NotNull Entity currentVictim,
            EntityType<T> entityType,
            MobSpawnType spawnType,
            ServerLevel level,
            BlockPos blockPosition,
            int attempts,
            int xzOffset,
            int yOffset,
            SpawnUtil.Strategy strategy) {
        BlockPos.MutableBlockPos mutableBlockPosition = blockPosition.mutable();

        for (int i = 0; i < attempts; i++) {
            int xOffset = Mth.randomBetweenInclusive(level.getRandom(), -xzOffset, xzOffset);
            int zOffset = Mth.randomBetweenInclusive(level.getRandom(), -xzOffset, xzOffset);
            mutableBlockPosition.setWithOffset(blockPosition, xOffset, yOffset, zOffset);
            if (level.getWorldBorder().isWithinBounds(mutableBlockPosition)
                    && SpawnUtil.moveToPossibleSpawnPosition(level, yOffset, mutableBlockPosition, strategy)) {
                T entity = entityType.create(level, null, null, mutableBlockPosition, spawnType, false, false);
                if (entity instanceof CaveDwellerEntity) {
                    if (entity.checkSpawnRules(level, spawnType) && entity.checkSpawnObstruction(level)) {
                        boolean isValidSpawn = entity.level().getNearestPlayer(entity, ServerConfig.SPAWN_DISTANCE) == null;
                        if (isValidSpawn && ServerConfig.CHECK_PATH_TO_SPAWN) {
                            Path path = entity.getNavigation().createPath(currentVictim, 0);
                            isValidSpawn = path != null && path.canReach();
                        }
                        if (isValidSpawn) {
                            entity.getNavigation().createPath(entity.blockPosition(), 0);
                            entity.getNavigation().stop();
                            level.addFreshEntityWithPassengers(entity);
                            return Optional.of(entity);
                        }
                    }
                    entity.discard();
                }
            }
        }

        CaveDweller.LOG.debug("Cave Dweller could not pass the spawn checks, target: [{}]", currentVictim);
        return Optional.empty();
    }
}
