package com.moderndamage.control.armor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.api.ModDamageSubPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
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

                if (dataObj.has("coverage")) {
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
                }

                if (dataObj.has("coverage_sub")) {
                    JsonObject subCoverage = dataObj.getAsJsonObject("coverage_sub");
                    for (Map.Entry<String, JsonElement> subEntry : subCoverage.entrySet()) {
                        String subKey = subEntry.getKey();
                        ModDamageSubPart subPart = ModDamageSubPart.bySubKey(subKey);
                        if (subPart == null) {
                            ModernDamage.LOGGER.warn("Invalid sub-part name: {} in armor config for {}", subKey, itemId);
                            continue;
                        }
                        int level = subEntry.getValue().getAsInt();
                        armorData.setSubProtection(subPart, level);
                    }
                }

                if (dataObj.has("toughness")) {
                    JsonObject toughnessObj = dataObj.getAsJsonObject("toughness");
                    for (Map.Entry<String, JsonElement> toughEntry : toughnessObj.entrySet()) {
                        String partName = toughEntry.getKey().toUpperCase();
                        try {
                            ModDamagePart part = ModDamagePart.valueOf(partName);
                            int value = toughEntry.getValue().getAsInt();
                            armorData.setToughness(part, value);
                        } catch (IllegalArgumentException e) {
                            ModernDamage.LOGGER.warn("Invalid part name for toughness: {} in {}", partName, itemId);
                        }
                    }
                }

                if (dataObj.has("toughness_sub")) {
                    JsonObject subToughness = dataObj.getAsJsonObject("toughness_sub");
                    for (Map.Entry<String, JsonElement> subEntry : subToughness.entrySet()) {
                        String subKey = subEntry.getKey();
                        ModDamageSubPart subPart = ModDamageSubPart.bySubKey(subKey);
                        if (subPart == null) {
                            ModernDamage.LOGGER.warn("Invalid sub-part name for toughness: {} in {}", subKey, itemId);
                            continue;
                        }
                        int value = subEntry.getValue().getAsInt();
                        armorData.setSubToughness(subPart, value);
                    }
                }

                if (dataObj.has("material_factor")) {
                    JsonObject matObj = dataObj.getAsJsonObject("material_factor");
                    for (Map.Entry<String, JsonElement> matEntry : matObj.entrySet()) {
                        String partName = matEntry.getKey().toUpperCase();
                        try {
                            ModDamagePart part = ModDamagePart.valueOf(partName);
                            float factor = matEntry.getValue().getAsFloat();
                            if (factor > 0) {
                                armorData.setMaterialFactor(part, factor);
                            } else {
                                ModernDamage.LOGGER.warn("material_factor for {} must be positive, using default 1.0", partName);
                            }
                        } catch (IllegalArgumentException e) {
                            ModernDamage.LOGGER.warn("Invalid part name for material_factor: {} in {}", partName, itemId);
                        }
                    }
                }

                if (dataObj.has("ricochet_chance")) {
                    JsonObject rcObj = dataObj.getAsJsonObject("ricochet_chance");
                    for (Map.Entry<String, JsonElement> rcEntry : rcObj.entrySet()) {
                        String partName = rcEntry.getKey().toUpperCase();
                        try {
                            ModDamagePart part = ModDamagePart.valueOf(partName);
                            float chance = rcEntry.getValue().getAsFloat();
                            armorData.setRicochetChance(part, chance);
                        } catch (IllegalArgumentException e) {
                            ModernDamage.LOGGER.warn("Invalid part name for ricochet_chance: {} in {}", partName, itemId);
                        }
                    }
                }

                if (dataObj.has("ricochet_sub")) {
                    JsonObject subRc = dataObj.getAsJsonObject("ricochet_sub");
                    for (Map.Entry<String, JsonElement> subEntry : subRc.entrySet()) {
                        String subKey = subEntry.getKey();
                        ModDamageSubPart subPart = ModDamageSubPart.bySubKey(subKey);
                        if (subPart == null) {
                            ModernDamage.LOGGER.warn("Invalid sub-part name for ricochet: {} in {}", subKey, itemId);
                            continue;
                        }
                        float chance = subEntry.getValue().getAsFloat();
                        armorData.setSubRicochetChance(subPart, chance);
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

            // coverage
            JsonObject coverage = new JsonObject();
            coverage.addProperty("chest", 67);
            coverage.addProperty("stomach", 50);
            coverage.addProperty("left_arm", 40);
            coverage.addProperty("right_arm", 40);
            ironChest.add("coverage", coverage);

            // toughness
            JsonObject toughness = new JsonObject();
            toughness.addProperty("chest", 5);
            toughness.addProperty("stomach", 5);
            toughness.addProperty("left_arm", 3);
            toughness.addProperty("right_arm", 3);
            ironChest.add("toughness", toughness);

            JsonObject materialFactor = new JsonObject();
            materialFactor.addProperty("chest", 1.0);
            materialFactor.addProperty("stomach", 1.0);
            materialFactor.addProperty("left_arm", 0.9);
            materialFactor.addProperty("right_arm", 0.9);
            ironChest.add("material_factor", materialFactor);

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