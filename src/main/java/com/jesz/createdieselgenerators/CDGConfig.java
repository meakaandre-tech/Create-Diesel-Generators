package com.jesz.createdieselgenerators;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON-backed replacement for the NeoForge config spec. Values keep their get() call.
 * Three files are written to the config folder: createdieselgenerators-client.json, -server.json and -common.json.
 */
public class CDGConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final class ConfigValue<T> {
        private final String key;
        private final String comment;
        private T value;
        private final Double min, max;

        private ConfigValue(String key, String comment, T defaultValue, Double min, Double max) {
            this.key = key;
            this.comment = comment;
            this.value = defaultValue;
            this.min = min;
            this.max = max;
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        private void read(JsonObject json) {
            if (!json.has(key))
                return;
            JsonElement e = json.get(key);
            try {
                if (value instanceof Boolean)
                    value = (T) (Boolean) e.getAsBoolean();
                else if (value instanceof Integer) {
                    int v = e.getAsInt();
                    if (min != null)
                        v = (int) Math.max(min, Math.min(max, v));
                    value = (T) (Integer) v;
                } else if (value instanceof Double)
                    value = (T) (Double) e.getAsDouble();
            } catch (Exception ex) {
                CreateDieselGenerators.LOGGER.warn("Invalid config value for '{}', keeping default", key);
            }
        }

        private void write(JsonObject json) {
            if (comment != null && !comment.isEmpty())
                json.addProperty("_comment: " + key, comment);
            if (value instanceof Boolean b)
                json.addProperty(key, b);
            else if (value instanceof Number n)
                json.addProperty(key, n);
        }
    }

    private static final List<ConfigValue<?>> CLIENT = new ArrayList<>();
    private static final List<ConfigValue<?>> SERVER = new ArrayList<>();
    private static final List<ConfigValue<?>> COMMON = new ArrayList<>();

    private static <T> ConfigValue<T> define(List<ConfigValue<?>> list, String comment, String key, T def) {
        ConfigValue<T> v = new ConfigValue<>(key, comment, def, null, null);
        list.add(v);
        return v;
    }

    private static ConfigValue<Integer> defineInRange(List<ConfigValue<?>> list, String comment, String key, int def, int min, int max) {
        ConfigValue<Integer> v = new ConfigValue<>(key, comment, def, (double) min, (double) max);
        list.add(v);
        return v;
    }

    public static final ConfigValue<Boolean> FUEL_TOOLTIPS = define(CLIENT, "Fuel type tooltip on Buckets", "Fuel tooltips", true);
    public static final ConfigValue<Boolean> DIESEL_ENGINE_IN_JEI = define(CLIENT, "Whenever Diesel Engines display in JEI", "Diesel Engine JEI Config", true);
    public static final ConfigValue<Boolean> ENGINES_EMIT_SOUND_ON_TRAINS = define(CLIENT, "Diesel Engines emit sounds on trains", "Diesel Engines emit sounds on trains", true);

    public static final ConfigValue<Double> TURBOCHARGED_ENGINE_MULTIPLIER = define(SERVER, "Turbocharged Diesel Engine Speed Multiplier", "Turbocharged Diesel Engine Speed Multiplier", 2d);
    public static final ConfigValue<Double> TURBOCHARGED_ENGINE_BURN_RATE_MULTIPLIER = define(SERVER, "Turbocharged Diesel Engine Burn Rate Multiplier", "Turbocharged Diesel Engine Burn Rate Multiplier", 1d);
    public static final ConfigValue<Boolean> NORMAL_ENGINES = define(SERVER, "Whenever Normal Diesel Engines are enabled", "Normal Diesel Engines", true);
    public static final ConfigValue<Boolean> MODULAR_ENGINES = define(SERVER, "Whenever Modular Diesel Engines are enabled", "Modular Diesel Engines", true);
    public static final ConfigValue<Boolean> HUGE_ENGINES = define(SERVER, "Whenever Huge Diesel Engines are enabled", "Huge Diesel Engines", true);
    public static final ConfigValue<Boolean> ENGINES_FILLED_WITH_ITEMS = define(SERVER, "Whenever Diesel Engines can be filled with an Item", "Engines filled with a bucket", false);
    public static final ConfigValue<Boolean> ENGINES_DISABLED_WITH_REDSTONE = define(SERVER, "Whenever Diesel Engines can be disabled with redstone", "Engines disabled with redstone", true);
    public static final ConfigValue<Boolean> ANALOG_SPEED_CONTROL = define(SERVER, "If Diesel Engines can be controlled with a analog lever.", "Engines controlled by analog lever", true);

    public static final ConfigValue<Integer> OIL_CHUNK_INFINITE_THRESHOLD = define(SERVER, "", "Infinite oil chunk threshold", 10_000_000);
    public static final ConfigValue<Integer> OIL_CHUNK_THRESHOLD = define(SERVER, "", "Oil chunk threshold", 4_000_000);
    public static final ConfigValue<Boolean> DISABLE_NORMAL_OIL_CHUNKS = define(SERVER, "", "Disable normal oil chunks", false);
    public static final ConfigValue<Boolean> DISABLE_HIGH_OIL_CHUNKS = define(SERVER, "", "Disable high oil chunks", false);
    public static final ConfigValue<Double> OIL_MULTIPLIER = define(SERVER, "", "Normal oil chunks oil amount multiplier", 1.3d);
    public static final ConfigValue<Double> HIGH_OIL_MULTIPLIER = define(SERVER, "", "High oil chunks oil amount multiplier", 2d);
    public static final ConfigValue<Double> OIL_CHUNK_SCALE = define(SERVER, "", "Oil chunk map scale", 1.0d);
    public static final ConfigValue<Integer> MAX_OIL_SCANNER_LEVEL = define(SERVER, "", "Max Oil Scanner Level", 10000);

    public static final ConfigValue<Integer> MAX_OIL_BARREL_WIDTH = define(SERVER, "Maximum width of Oil Barrels", "Max Oil Barrel Width", 3);
    public static final ConfigValue<Integer> MAX_OIL_BARREL_LENGTH_PER_WIDTH = define(SERVER, "Maximum Oil Barrel length for each unit of width", "Max Oil Barrel Length (per width)", 4);
    public static final ConfigValue<Boolean> CANISTER_SPOUT_FILLING = define(SERVER, "Canister can be filled by spouts", "Canister can be filled by spouts", true);
    public static final ConfigValue<Boolean> COMBUSTIBLES_BLOW_UP = define(SERVER, "Combustibles do boom boom when on fire", "Combustibles blow up", true);
    public static final ConfigValue<Integer> DISTILLATION_MIN_HEIGHT = defineInRange(SERVER, "Minimum height of the Distillation Tower required to process recipes", "Distillation Tower Minimum Height", 3, 2, 7);

    public static final ConfigValue<Integer> TOOL_CAPACITY = define(COMMON, "Capacity of Tools requiring Fluids in mB", "Capacity of Tools requiring Fluids", 200);
    public static final ConfigValue<Integer> TOOL_CAPACITY_ENCHANTMENT = define(COMMON, "Tool Capacity Enchantment Capacity Addition in mB", "Capacity Addition of Tools with Capacity Enchantment", 100);
    public static final ConfigValue<Integer> CANISTER_CAPACITY = define(COMMON, "Canister Capacity in mB", "Capacity of Canisters", 4000);
    public static final ConfigValue<Integer> CANISTER_CAPACITY_ENCHANTMENT = define(COMMON, "Canister Capacity Enchantment Capacity Addition in mB", "Capacity Addition of Capacity Enchantment in Canisters", 1000);

    public static void loadCommon() {
        load(CreateDieselGenerators.ID + "-server.json", SERVER);
        load(CreateDieselGenerators.ID + "-common.json", COMMON);
    }

    public static void loadClient() {
        load(CreateDieselGenerators.ID + "-client.json", CLIENT);
    }

    private static void load(String fileName, List<ConfigValue<?>> values) {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(fileName);
        JsonObject json = new JsonObject();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                JsonObject read = GSON.fromJson(reader, JsonObject.class);
                if (read != null)
                    json = read;
            } catch (Exception e) {
                CreateDieselGenerators.LOGGER.error("Could not read {}, using defaults", fileName, e);
            }
        }
        JsonObject out = new JsonObject();
        for (ConfigValue<?> v : values) {
            v.read(json);
            v.write(out);
        }
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(out, writer);
        } catch (Exception e) {
            CreateDieselGenerators.LOGGER.error("Could not write {}", fileName, e);
        }
    }
}
