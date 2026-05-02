package com.moderndamage.control.armor;

import com.moderndamage.control.api.ModDamagePart;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ArmorData {
    private final Map<ModDamagePart, Integer> coverage = new HashMap<>();
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

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }
}