package com.moderndamage.control.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
            Map<String, EntityHitboxConfig> loaded = GSON.fromJson(reader,
                    new com.google.gson.reflect.TypeToken<Map<String, EntityHitboxConfig>>(){}.getType());
            for (Map.Entry<String, EntityHitboxConfig> entry : loaded.entrySet()) {
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entry.getKey()));
                if (type != null) {
                    CONFIG_CACHE.put(type, entry.getValue());
                } else {
                    ModernDamage.LOGGER.warn("Unknown entity type: {}", entry.getKey());
                }
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
                    cfg.legXRange = new float[]{0.0f, 0.8f};   // 修改此处
                }
                cfg.armXRange = new float[]{0, 0};
                cfg.armXRange = new float[]{0, 0};

                if (type == EntityType.ZOMBIE) {
                    cfg.naturalArmor.put("head", 10);
                    cfg.naturalArmor.put("chest", 20);
                } else if (type == EntityType.SKELETON) {
                    cfg.naturalArmor.put("head", 15);
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