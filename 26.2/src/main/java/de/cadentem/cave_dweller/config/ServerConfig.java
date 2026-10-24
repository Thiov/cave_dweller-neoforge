package de.cadentem.cave_dweller.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.cadentem.cave_dweller.CaveDweller;
import net.neoforged.fml.loading.FMLPaths;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ServerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("cave_dweller-server.json");

    public static int CAN_SPAWN_MIN = 300;
    public static int CAN_SPAWN_MAX = 600;
    public static int CAN_SPAWN_COOLDOWN = 1200;
    public static double CAN_SPAWN_COOLDOWN_CHANCE = 0.4;
    public static int RESET_NOISE_MIN = 240;
    public static int RESET_NOISE_MAX = 360;
    public static int TIME_UNTIL_LEAVE = 300;
    public static int TIME_UNTIL_LEAVE_CHASE = 30;
    public static double SURFACE_TIMER_MULTIPLIER = 1.0;
    public static double SPAWN_CHANCE_PER_TICK = 0.005;
    public static int SPAWN_HEIGHT = 40;
    public static boolean ALLOW_SURFACE_SPAWN = false;
    public static int SKY_LIGHT_LEVEL = 8;
    public static int BLOCK_LIGHT_LEVEL = 15;
    public static int MAXIMUM_AMOUNT = 5;
    public static int SPAWN_DISTANCE = 16;
    public static boolean CHECK_PATH_TO_SPAWN = true;
    public static List<String> DIMENSION_WHITELIST = new ArrayList<>(List.of("minecraft:overworld"));
    public static boolean OVERRIDE_BIOME_DATAPACK_CONFIG = false;
    public static boolean SURFACE_BIOMES_IS_WHITELIST = true;
    public static List<String> SURFACE_BIOMES = new ArrayList<>();
    public static int SPOTTING_RANGE = 60;
    public static boolean CAN_CLIMB = true;
    public static boolean CAN_BREAK_DOOR = true;
    public static int BREAK_DOOR_TIME = 3;
    public static boolean ALLOW_RIDING = false;
    public static boolean TARGET_INVISIBLE = true;
    public static double MAX_HEALTH = 60.0;
    public static double ATTACK_DAMAGE = 6.0;
    public static double ATTACK_SPEED = 0.35;
    public static double MOVEMENT_SPEED = 0.5;
    public static double DEPTH_STRIDER_BONUS = 1.5;

    public static final TagKey<Biome> CAVE_DWELLER_SURFACE_BIOMES = TagKey.create(
            Registries.BIOME, Identifier.fromNamespaceAndPath("cave_dweller", "cave_dweller_surface_biomes"));

    public static void load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                save();
                return;
            }
            JsonObject root = JsonParser.parseString(Files.readString(CONFIG_PATH)).getAsJsonObject();
            CAN_SPAWN_MIN = getInt(root, "can_spawn_min", CAN_SPAWN_MIN);
            CAN_SPAWN_MAX = getInt(root, "can_spawn_max", CAN_SPAWN_MAX);
            CAN_SPAWN_COOLDOWN = getInt(root, "can_spawn_cooldown", CAN_SPAWN_COOLDOWN);
            CAN_SPAWN_COOLDOWN_CHANCE = getDouble(root, "can_spawn_cooldown_chance", CAN_SPAWN_COOLDOWN_CHANCE);
            RESET_NOISE_MIN = getInt(root, "reset_noise_min", RESET_NOISE_MIN);
            RESET_NOISE_MAX = getInt(root, "reset_noise_max", RESET_NOISE_MAX);
            TIME_UNTIL_LEAVE = getInt(root, "time_until_leave", TIME_UNTIL_LEAVE);
            TIME_UNTIL_LEAVE_CHASE = getInt(root, "time_until_leave_chase", TIME_UNTIL_LEAVE_CHASE);
            SURFACE_TIMER_MULTIPLIER = getDouble(root, "surface_timer_multiplier", SURFACE_TIMER_MULTIPLIER);
            SPAWN_CHANCE_PER_TICK = getDouble(root, "spawn_chance_per_tick", SPAWN_CHANCE_PER_TICK);
            SPAWN_HEIGHT = getInt(root, "spawn_height", SPAWN_HEIGHT);
            ALLOW_SURFACE_SPAWN = getBool(root, "allow_surface_spawn", ALLOW_SURFACE_SPAWN);
            SKY_LIGHT_LEVEL = getInt(root, "sky_light_level", SKY_LIGHT_LEVEL);
            BLOCK_LIGHT_LEVEL = getInt(root, "block_light_level", BLOCK_LIGHT_LEVEL);
            MAXIMUM_AMOUNT = getInt(root, "maximum_amount", MAXIMUM_AMOUNT);
            SPAWN_DISTANCE = getInt(root, "spawn_distance", SPAWN_DISTANCE);
            CHECK_PATH_TO_SPAWN = getBool(root, "check_path_to_spawn", CHECK_PATH_TO_SPAWN);
            DIMENSION_WHITELIST = getStringList(root, "dimension_whitelist", DIMENSION_WHITELIST);
            OVERRIDE_BIOME_DATAPACK_CONFIG = getBool(root, "override_biome_datapack_config", OVERRIDE_BIOME_DATAPACK_CONFIG);
            SURFACE_BIOMES_IS_WHITELIST = getBool(root, "surface_biomes_is_whitelist", SURFACE_BIOMES_IS_WHITELIST);
            SURFACE_BIOMES = getStringList(root, "surface_biomes", SURFACE_BIOMES);
            SPOTTING_RANGE = getInt(root, "spotting_range", SPOTTING_RANGE);
            CAN_CLIMB = getBool(root, "can_climb", CAN_CLIMB);
            CAN_BREAK_DOOR = getBool(root, "can_break_door", CAN_BREAK_DOOR);
            BREAK_DOOR_TIME = getInt(root, "break_door_time", BREAK_DOOR_TIME);
            ALLOW_RIDING = getBool(root, "allow_riding", ALLOW_RIDING);
            TARGET_INVISIBLE = getBool(root, "target_invisible", TARGET_INVISIBLE);
            MAX_HEALTH = getDouble(root, "maximum_health", MAX_HEALTH);
            ATTACK_DAMAGE = getDouble(root, "attack_damage", ATTACK_DAMAGE);
            ATTACK_SPEED = getDouble(root, "attack_speed", ATTACK_SPEED);
            MOVEMENT_SPEED = getDouble(root, "movement_speed", MOVEMENT_SPEED);
            DEPTH_STRIDER_BONUS = getDouble(root, "depth_strider_bonus", DEPTH_STRIDER_BONUS);
        } catch (IOException e) {
            CaveDweller.LOG.error("Failed to read cave_dweller config", e);
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("can_spawn_min", CAN_SPAWN_MIN);
            root.addProperty("can_spawn_max", CAN_SPAWN_MAX);
            root.addProperty("can_spawn_cooldown", CAN_SPAWN_COOLDOWN);
            root.addProperty("can_spawn_cooldown_chance", CAN_SPAWN_COOLDOWN_CHANCE);
            root.addProperty("reset_noise_min", RESET_NOISE_MIN);
            root.addProperty("reset_noise_max", RESET_NOISE_MAX);
            root.addProperty("time_until_leave", TIME_UNTIL_LEAVE);
            root.addProperty("time_until_leave_chase", TIME_UNTIL_LEAVE_CHASE);
            root.addProperty("surface_timer_multiplier", SURFACE_TIMER_MULTIPLIER);
            root.addProperty("spawn_chance_per_tick", SPAWN_CHANCE_PER_TICK);
            root.addProperty("spawn_height", SPAWN_HEIGHT);
            root.addProperty("allow_surface_spawn", ALLOW_SURFACE_SPAWN);
            root.addProperty("sky_light_level", SKY_LIGHT_LEVEL);
            root.addProperty("block_light_level", BLOCK_LIGHT_LEVEL);
            root.addProperty("maximum_amount", MAXIMUM_AMOUNT);
            root.addProperty("spawn_distance", SPAWN_DISTANCE);
            root.addProperty("check_path_to_spawn", CHECK_PATH_TO_SPAWN);
            root.add("dimension_whitelist", GSON.toJsonTree(DIMENSION_WHITELIST));
            root.addProperty("override_biome_datapack_config", OVERRIDE_BIOME_DATAPACK_CONFIG);
            root.addProperty("surface_biomes_is_whitelist", SURFACE_BIOMES_IS_WHITELIST);
            root.add("surface_biomes", GSON.toJsonTree(SURFACE_BIOMES));
            root.addProperty("spotting_range", SPOTTING_RANGE);
            root.addProperty("can_climb", CAN_CLIMB);
            root.addProperty("can_break_door", CAN_BREAK_DOOR);
            root.addProperty("break_door_time", BREAK_DOOR_TIME);
            root.addProperty("allow_riding", ALLOW_RIDING);
            root.addProperty("target_invisible", TARGET_INVISIBLE);
            root.addProperty("maximum_health", MAX_HEALTH);
            root.addProperty("attack_damage", ATTACK_DAMAGE);
            root.addProperty("attack_speed", ATTACK_SPEED);
            root.addProperty("movement_speed", MOVEMENT_SPEED);
            root.addProperty("depth_strider_bonus", DEPTH_STRIDER_BONUS);
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(root));
        } catch (IOException e) {
            CaveDweller.LOG.error("Failed to write cave_dweller config", e);
        }
    }

    private static int getInt(JsonObject o, String k, int d) { JsonElement e = o.get(k); return e != null && e.isJsonPrimitive() ? e.getAsInt() : d; }
    private static double getDouble(JsonObject o, String k, double d) { JsonElement e = o.get(k); return e != null && e.isJsonPrimitive() ? e.getAsDouble() : d; }
    private static boolean getBool(JsonObject o, String k, boolean d) { JsonElement e = o.get(k); return e != null && e.isJsonPrimitive() ? e.getAsBoolean() : d; }
    private static List<String> getStringList(JsonObject o, String k, List<String> d) {
        JsonElement e = o.get(k);
        if (e == null || !e.isJsonArray()) return d;
        List<String> out = new ArrayList<>();
        e.getAsJsonArray().forEach(j -> out.add(j.getAsString()));
        return out;
    }

    public static boolean isValidDimension(String key) {
        return DIMENSION_WHITELIST.contains(key);
    }

    public static boolean isInValidBiome(Entity entity) {
        if (entity == null) return false;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return false;
        Holder<Biome> biome = serverLevel.getBiome(entity.blockPosition());
        boolean isWhitelist = SURFACE_BIOMES_IS_WHITELIST;
        boolean isBiomeInList;
        if (OVERRIDE_BIOME_DATAPACK_CONFIG) {
            Identifier resource = serverLevel.registryAccess()
                    .lookupOrThrow(Registries.BIOME).getKey(biome.value());
            isBiomeInList = resource != null && SURFACE_BIOMES.contains(resource.toString());
        } else {
            isBiomeInList = biome.is(CAVE_DWELLER_SURFACE_BIOMES);
        }
        return isWhitelist == isBiomeInList;
    }
}
