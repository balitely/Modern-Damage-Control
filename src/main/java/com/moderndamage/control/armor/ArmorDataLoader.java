package com.moderndamage.control.armor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.api.ModDamagePart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ArmorDataLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<Item, ArmorData> ARMOR_DATA_CACHE = new HashMap<>();
    private static Path configPath;

    public static void init(Path configDir) {
        configPath = configDir.resolve("moderndamage/armor_properties.json");
        load();
    }

    public static void load() {
        ARMOR_DATA_CACHE.clear();
        if (!Files.exists(configPath)) {
            createDefaultConfig();
        }
        try (Reader reader = Files.newBufferedReader(configPath)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                String itemId = entry.getKey();
                JsonObject dataObj = entry.getValue().getAsJsonObject();
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
                if (item == null) {
                    ModernDamage.LOGGER.warn("Unknown item in armor config: {}", itemId);
                    continue;
                }
                ArmorData armorData = new ArmorData();
                JsonObject coverage = dataObj.getAsJsonObject("coverage");
                for (Map.Entry<String, JsonElement> covEntry : coverage.entrySet()) {
                    String partName = covEntry.getKey().toUpperCase();
                    try {
                        ModDamagePart part = ModDamagePart.valueOf(partName);
                        int level = covEntry.getValue().getAsInt();
                        armorData.setProtection(part, level);
                    } catch (IllegalArgumentException e) {
                        ModernDamage.LOGGER.warn("Invalid part name: {} in armor config for {}", partName, itemId);
                    }
                }
                if (dataObj.has("durability")) {
                    armorData.setDurability(dataObj.get("durability").getAsInt());
                }
                ARMOR_DATA_CACHE.put(item, armorData);
            }
            ModernDamage.LOGGER.info("Loaded {} armor definitions", ARMOR_DATA_CACHE.size());
        } catch (Exception e) {
            ModernDamage.LOGGER.error("Failed to load armor config", e);
        }
    }

    private static void createDefaultConfig() {
        try {
            Files.createDirectories(configPath.getParent());
            JsonObject example = new JsonObject();
            JsonObject ironChest = new JsonObject();
            JsonObject coverage = new JsonObject();
            coverage.addProperty("chest", 67);   // 原6级 → 60
            coverage.addProperty("stomach", 50);
            coverage.addProperty("left_arm", 40);
            coverage.addProperty("right_arm", 40);
            ironChest.add("coverage", coverage);
            example.add("minecraft:iron_chestplate", ironChest);
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(example, writer);
            }
            ModernDamage.LOGGER.info("Created default armor config at {}", configPath);
        } catch (IOException e) {
            ModernDamage.LOGGER.error("Failed to create default armor config", e);
        }
    }

    public static ArmorData getArmorData(Item item) {
        return ARMOR_DATA_CACHE.get(item);
    }

    public static boolean hasArmorData(Item item) {
        return ARMOR_DATA_CACHE.containsKey(item);
    }
}