package com.moderndamage.control.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moderndamage.control.ModernDamage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class EntityHitboxConfigLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<EntityType<?>, EntityHitboxConfig> CONFIG_CACHE = new HashMap<>();
    private static Path configPath;

    public static void init(Path configDir) {
        configPath = configDir.resolve("moderndamage/entity_config.json");
        load();
    }

    public static void load() {
        CONFIG_CACHE.clear();
        if (!Files.exists(configPath)) {
            generateDefaultConfig();
        }
        try (Reader reader = Files.newBufferedReader(configPath)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                String entityId = entry.getKey();
                JsonObject cfgObj = entry.getValue().getAsJsonObject();
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityId));
                if (type == null) {
                    ModernDamage.LOGGER.warn("Unknown entity type: {}", entityId);
                    continue;
                }
                EntityHitboxConfig cfg = new EntityHitboxConfig();

                if (cfgObj.has("useArmor")) {
                    cfg.useArmor = cfgObj.get("useArmor").getAsBoolean();
                }

                if (cfgObj.has("headHeight")) {
                    var arr = cfgObj.getAsJsonArray("headHeight");
                    if (arr.size() >= 2) {
                        cfg.headHeight[0] = arr.get(0).getAsFloat();
                        cfg.headHeight[1] = arr.get(1).getAsFloat();
                    }
                }

                if (cfgObj.has("bodyHeight")) {
                    var arr = cfgObj.getAsJsonArray("bodyHeight");
                    if (arr.size() >= 2) {
                        cfg.bodyHeight[0] = arr.get(0).getAsFloat();
                        cfg.bodyHeight[1] = arr.get(1).getAsFloat();
                    }
                }

                if (cfgObj.has("chestRatio")) {
                    cfg.chestRatio = cfgObj.get("chestRatio").getAsFloat();
                }

                if (cfgObj.has("armXRange")) {
                    var arr = cfgObj.getAsJsonArray("armXRange");
                    if (arr.size() >= 2) {
                        cfg.armXRange[0] = arr.get(0).getAsFloat();
                        cfg.armXRange[1] = arr.get(1).getAsFloat();
                    }
                }

                if (cfgObj.has("legXRange")) {
                    var arr = cfgObj.getAsJsonArray("legXRange");
                    if (arr.size() >= 2) {
                        cfg.legXRange[0] = arr.get(0).getAsFloat();
                        cfg.legXRange[1] = arr.get(1).getAsFloat();
                    }
                }

                if (cfgObj.has("vitalPartsLethal")) {
                    cfg.vitalPartsLethal = cfgObj.get("vitalPartsLethal").getAsBoolean();
                }

                if (cfgObj.has("naturalArmor")) {
                    JsonObject armorObj = cfgObj.getAsJsonObject("naturalArmor");
                    for (Map.Entry<String, JsonElement> armorEntry : armorObj.entrySet()) {
                        cfg.naturalArmor.put(armorEntry.getKey(), armorEntry.getValue().getAsInt());
                    }
                }

                if (cfgObj.has("naturalToughness")) {
                    JsonObject toughObj = cfgObj.getAsJsonObject("naturalToughness");
                    for (Map.Entry<String, JsonElement> toughEntry : toughObj.entrySet()) {
                        cfg.naturalToughness.put(toughEntry.getKey(), toughEntry.getValue().getAsInt());
                    }
                }

                CONFIG_CACHE.put(type, cfg);
            }
            ModernDamage.LOGGER.info("Loaded {} entity hitbox configs", CONFIG_CACHE.size());
        } catch (Exception e) {
            ModernDamage.LOGGER.error("Failed to load entity config", e);
        }
    }

    public static void generateDefaultConfig() {
        try {
            Files.createDirectories(configPath.getParent());
            Map<String, EntityHitboxConfig> defaults = new HashMap<>();
            for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES) {
                EntityHitboxConfig cfg = new EntityHitboxConfig();
                var dimensions = type.getDimensions();
                float height = dimensions.height;
                if (height > 0) {
                    float headMin = height * 0.75f;
                    cfg.headHeight = new float[]{headMin, height};
                    float bodyMin = height * 0.3f;
                    cfg.bodyHeight = new float[]{bodyMin, headMin};
                    cfg.chestRatio = 0.5f;
                    cfg.legXRange = new float[]{0.0f, 0.8f};
                }
                cfg.armXRange = new float[]{0, 0};

                if (type == EntityType.ZOMBIE) {
                    cfg.naturalArmor.put("head", 10);
                    cfg.naturalArmor.put("chest", 20);
                    cfg.naturalToughness.put("head", 5);
                    cfg.naturalToughness.put("chest", 10);
                } else if (type == EntityType.SKELETON) {
                    cfg.naturalArmor.put("head", 15);
                    cfg.naturalToughness.put("head", 8);
                }

                ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(type);
                if (key != null) {
                    defaults.put(key.toString(), cfg);
                }
            }
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(defaults, writer);
            }
            ModernDamage.LOGGER.info("Generated default entity config at {}", configPath);
        } catch (IOException e) {
            ModernDamage.LOGGER.error("Failed to generate default entity config", e);
        }
    }

    public static EntityHitboxConfig getConfig(EntityType<?> type) {
        return CONFIG_CACHE.getOrDefault(type, new EntityHitboxConfig());
    }
}