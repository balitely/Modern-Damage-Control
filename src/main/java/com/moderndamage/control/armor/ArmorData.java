package com.moderndamage.control.armor;

import com.moderndamage.control.api.ModDamagePart;

import java.util.HashMap;
import java.util.Map;

public class ArmorData {
    private final Map<ModDamagePart, Integer> coverage = new HashMap<>();
    private final Map<ModDamagePart, Integer> toughness = new HashMap<>();
    private final Map<ModDamagePart, Float> materialFactor = new HashMap<>();
    private final Map<ModDamagePart, Float> ricochetChance = new HashMap<>();
    private int durability;

    public ArmorData() {}

    public void setProtection(ModDamagePart part, int level) {
        coverage.put(part, level);
    }
    public int getProtectionLevel(ModDamagePart part) {
        return coverage.getOrDefault(part, 0);
    }
    public boolean protects(ModDamagePart part) {
        return coverage.containsKey(part);
    }
    public Map<ModDamagePart, Integer> getCoverage() {
        return coverage;
    }

    public void setToughness(ModDamagePart part, int value) {
        toughness.put(part, value);
    }
    public int getBaseToughness(ModDamagePart part) {
        return toughness.getOrDefault(part, 0);
    }
    public boolean hasToughness(ModDamagePart part) {
        return toughness.containsKey(part);
    }
    public Map<ModDamagePart, Integer> getToughnessMap() {
        return toughness;
    }

    public void setMaterialFactor(ModDamagePart part, float factor) {
        materialFactor.put(part, factor);
    }
    public float getMaterialFactor(ModDamagePart part) {
        return materialFactor.getOrDefault(part, 1.0f);
    }
    public Map<ModDamagePart, Float> getMaterialFactorMap() {
        return materialFactor;
    }

    public void setRicochetChance(ModDamagePart part, float chance) {
        ricochetChance.put(part, Math.min(1.0f, Math.max(0.0f, chance)));
    }
    public float getRicochetChance(ModDamagePart part) {
        return ricochetChance.getOrDefault(part, 0.0f);
    }

    public int getDurability() {
        return durability;
    }
    public void setDurability(int durability) {
        this.durability = durability;
    }
}