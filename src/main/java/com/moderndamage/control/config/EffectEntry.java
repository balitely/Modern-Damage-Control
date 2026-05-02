package com.moderndamage.control.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectEntry {
    public String effectId;
    public int duration;
    public int amplifier;
    public float threshold;
    public double chance;

    public EffectEntry() {}

    public EffectEntry(String effectId, int duration, int amplifier, float threshold, double chance) {
        this.effectId = effectId;
        this.duration = duration;
        this.amplifier = amplifier;
        this.threshold = threshold;
        this.chance = chance;
    }

    public MobEffect getEffect() {
        if (effectId == null || effectId.isEmpty()) return null;
        return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectId));
    }

    public int getDuration() { return duration; }
    public int getAmplifier() { return amplifier; }
    public float getThreshold() { return threshold; }
    public double getChance() { return chance; }

    public boolean isValid() {
        return effectId != null && !effectId.isEmpty() && getEffect() != null;
    }
}