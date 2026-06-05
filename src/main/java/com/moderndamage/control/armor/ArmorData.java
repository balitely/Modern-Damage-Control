package com.moderndamage.control.armor;

import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.api.ModDamageSubPart;

import java.util.HashMap;
import java.util.Map;

public class ArmorData {
    private final Map<ModDamagePart, Integer> coverage = new HashMap<>();
    private final Map<ModDamagePart, Integer> toughness = new HashMap<>();
    private final Map<ModDamagePart, Float> materialFactor = new HashMap<>();
    private final Map<ModDamagePart, Float> ricochetChance = new HashMap<>();
    private int durability;

    private final Map<ModDamageSubPart, Integer> subCoverage = new HashMap<>();
    private final Map<ModDamageSubPart, Integer> subToughness = new HashMap<>();
    private final Map<ModDamageSubPart, Float> subRicochetChance = new HashMap<>();

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

    public void setSubProtection(ModDamageSubPart part, int level) {
        subCoverage.put(part, level);
    }
    public int getSubProtectionLevel(ModDamageSubPart part) {
        return subCoverage.getOrDefault(part, 0);
    }
    public boolean protectsSub(ModDamageSubPart part) {
        return subCoverage.containsKey(part);
    }

    public void setSubToughness(ModDamageSubPart part, int value) {
        subToughness.put(part, value);
    }
    public int getSubToughness(ModDamageSubPart part) {
        return subToughness.getOrDefault(part, 0);
    }

    public void setSubRicochetChance(ModDamageSubPart part, float chance) {
        subRicochetChance.put(part, Math.min(1.0f, Math.max(0.0f, chance)));
    }
    public float getSubRicochetChance(ModDamageSubPart part) {
        return subRicochetChance.getOrDefault(part, 0.0f);
    }

    public int getEffectiveToughness(ModDamageSubPart subPart, ModDamagePart parent) {
        int subTough = getSubToughness(subPart);
        return subTough > 0 ? subTough : getBaseToughness(parent);
    }

    public float getEffectiveRicochetChance(ModDamageSubPart subPart, ModDamagePart parent) {
        float subChance = getSubRicochetChance(subPart);
        return subChance > 0 ? subChance : getRicochetChance(parent);
    }
}