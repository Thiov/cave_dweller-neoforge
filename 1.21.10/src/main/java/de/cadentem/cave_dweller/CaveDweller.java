package de.cadentem.cave_dweller;

import com.mojang.logging.LogUtils;
import de.cadentem.cave_dweller.client.HandleCaveSound;
import de.cadentem.cave_dweller.config.ServerConfig;
import de.cadentem.cave_dweller.entities.CaveDwellerEntity;
import de.cadentem.cave_dweller.events.CaveDwellerEvents;
import de.cadentem.cave_dweller.network.CaveSoundPayload;
import de.cadentem.cave_dweller.registry.ModEntityTypes;
import de.cadentem.cave_dweller.registry.ModItems;
import de.cadentem.cave_dweller.registry.ModSounds;
import de.cadentem.cave_dweller.util.Timer;
import de.cadentem.cave_dweller.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Mod(CaveDweller.MODID)
public class CaveDweller {
    public static final String MODID = "cave_dweller";
    public static final Logger LOG = LogUtils.getLogger();
    public static final Random RANDOM = new Random();
    private static final HashMap<String, Timer> TIMERS = new HashMap<>();
    public static boolean RELOAD_ALL = false;
    public static boolean RELOAD_MISSING = false;

    public CaveDweller(IEventBus modEventBus) {
        ServerConfig.load();

        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);

        modEventBus.addListener(this::registerPayloads);
        modEventBus.addListener(this::createAttributes);
        modEventBus.addListener(this::addCreative);

        CaveDwellerEvents.register();

        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> RELOAD_ALL = true);
        NeoForge.EVENT_BUS.addListener(this::serverTick);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> RELOAD_MISSING = true);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(CaveSoundPayload.TYPE, CaveSoundPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> HandleCaveSound.handle(payload)));
    }

    private void createAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.CAVE_DWELLER.get(), CaveDwellerEntity.createAttributes());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.CAVE_DWELLER_SPAWN_EGG);
        }
    }

    private void serverTick(ServerTickEvent.Pre event) {
        MinecraftServer server = event.getServer();

        if (RELOAD_ALL) {
            TIMERS.clear();
            RELOAD_ALL = false;
        }

        Iterable<ServerLevel> levels = server.getAllLevels();
        if (TIMERS.isEmpty()) {
            for (ServerLevel level : levels) {
                String key = level.dimension().location().toString();
                if (ServerConfig.DIMENSION_WHITELIST.contains(key)) TIMERS.put(key, new Timer());
            }
            if (TIMERS.isEmpty() && server.getTickCount() % 6000 == 0) {
                LOG.debug("There are currently no timers present - are the dimensions properly configured?");
            }
            RELOAD_MISSING = false;
        } else if (RELOAD_MISSING) {
            for (ServerLevel level : levels) {
                String key = level.dimension().location().toString();
                if (TIMERS.get(key) == null && ServerConfig.DIMENSION_WHITELIST.contains(key)) {
                    TIMERS.put(key, new Timer());
                }
            }
            RELOAD_MISSING = false;
        }

        for (ServerLevel level : levels) {
            String key = level.dimension().location().toString();
            if (TIMERS.get(key) != null) handleLogic(level);
        }
    }

    private void handleLogic(ServerLevel level) {
        List<ServerPlayer> players = level.getPlayers(this::isRelevantPlayer);
        if (players.isEmpty()) return;
        String key = level.dimension().location().toString();
        Timer timer = TIMERS.get(key);
        if (timer.currentVictim == null
                || players.stream().noneMatch(p -> p.getStringUUID().equals(timer.currentVictim.getStringUUID()))) {
            timer.currentVictim = players.get(RANDOM.nextInt(players.size()));
        }

        AtomicInteger caveDwellerCount = new AtomicInteger();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof CaveDwellerEntity) caveDwellerCount.getAndAdd(1);
        }
        timer.currentSpawn++;
        timer.currentNoise++;
        if (timer.isNoiseTimerReached()
                && (caveDwellerCount.get() > 0 || timer.currentSpawn >= Utils.secondsToTicks(ServerConfig.CAN_SPAWN_MAX) / 2)) {
            players.forEach(this::playCaveSoundToSpelunkers);
            timer.resetNoiseTimer();
        }

        if (timer.isSpawnTimerReached()
                && caveDwellerCount.get() < ServerConfig.MAXIMUM_AMOUNT
                && RANDOM.nextDouble() <= ServerConfig.SPAWN_CHANCE_PER_TICK
                && timer.currentVictim != null) {
            level.getPlayers(this::playCaveSoundToSpelunkers);
            timer.resetNoiseTimer();
            Optional<CaveDwellerEntity> optionalEntity = Utils.trySpawnMob(
                    timer.currentVictim,
                    ModEntityTypes.CAVE_DWELLER.get(),
                    EntitySpawnReason.TRIGGERED,
                    level,
                    timer.currentVictim.blockPosition(),
                    40,
                    35,
                    6,
                    SpawnUtil.Strategy.ON_TOP_OF_COLLIDER);
            if (optionalEntity.isPresent()) {
                CaveDwellerEntity dweller = optionalEntity.get();
                dweller.setInvisible(true);
                dweller.hasSpawned = true;
                timer.resetSpawnTimer();
            } else {
                timer.currentVictim = null;
            }
        }
    }

    private boolean playCaveSoundToSpelunkers(ServerPlayer player) {
        ResourceLocation soundLocation = switch (RANDOM.nextInt(4)) {
            case 1 -> ModSounds.CAVENOISE_2.get().location();
            case 2 -> ModSounds.CAVENOISE_3.get().location();
            case 3 -> ModSounds.CAVENOISE_4.get().location();
            default -> ModSounds.CAVENOISE_1.get().location();
        };
        PacketDistributor.sendToPlayer(player, new CaveSoundPayload(soundLocation, player.blockPosition(), 2.0F, 1.0F));
        return true;
    }

    private boolean isRelevantPlayer(ServerPlayer player) {
        if (!Utils.isValidPlayer(player)) return false;
        if (player.position().y > ServerConfig.SPAWN_HEIGHT) return false;

        Level serverLevel = player.level();
        int actualSkyLightLevel = serverLevel.getBrightness(LightLayer.SKY, player.blockPosition()) - serverLevel.getSkyDarken();
        // 26.1.2 removed Level.getSunAngle — compute from game time
        long dayTime = serverLevel.getLevelData().getGameTime();
        float fractionalDay = ((float) (dayTime % 24000L)) / 24000.0F - 0.25F;
        if (fractionalDay < 0.0F) fractionalDay += 1.0F;
        if (fractionalDay > 1.0F) fractionalDay -= 1.0F;
        float sunAngle = fractionalDay * 2.0F * (float) Math.PI;
        if (actualSkyLightLevel > 0) {
            float f1 = sunAngle < (float) Math.PI ? 0.0F : (float) (Math.PI * 2);
            sunAngle += (f1 - sunAngle) * 0.2F;
            actualSkyLightLevel = Math.round(actualSkyLightLevel * Mth.cos(sunAngle));
        }
        actualSkyLightLevel = Mth.clamp(actualSkyLightLevel, 0, 15);
        if (actualSkyLightLevel > ServerConfig.SKY_LIGHT_LEVEL) return false;

        LayerLightEventListener blockLighting = serverLevel.getLightEngine().getLayerListener(LightLayer.BLOCK);
        if (blockLighting.getLightValue(player.blockPosition()) > ServerConfig.BLOCK_LIGHT_LEVEL) return false;

        boolean isOnSurface = Utils.isOnSurface(player);
        if (isOnSurface) {
            return ServerConfig.ALLOW_SURFACE_SPAWN && ServerConfig.isInValidBiome(player);
        }
        return true;
    }

    public static void speedUpTimers(String key, int spawnDelta, int noiseDelta) {
        Timer timer = TIMERS.get(key);
        if (timer != null) {
            timer.currentSpawn += spawnDelta;
            timer.currentNoise += noiseDelta;
        }
    }
}
