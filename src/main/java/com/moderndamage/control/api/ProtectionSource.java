package com.moderndamage.control.api;

import net.minecraft.world.item.ItemStack;
import java.util.Map;
import java.util.function.IntConsumer;

public class ProtectionSource {
    private final ItemStack sourceStack;
    private final Map<ModDamageSubPart, Integer> subProtection;
    private final Map<ModDamageSubPart, Integer> subToughness;
    private final Map<ModDamageSubPart, Float> subRicochetChance;
    private final float materialFactor;
    private final IntConsumer durabilityConsumer;
    private final int primaryProtectionLevel;
    private final int toughness;
    private final float ricochetChance;
    private final boolean isNatural;

    public ProtectionSource(ItemStack sourceStack,
                            Map<ModDamageSubPart, Integer> subProtection,
                            Map<ModDamageSubPart, Integer> subToughness,
                            Map<ModDamageSubPart, Float> subRicochetChance,
                            float materialFactor,
                            IntConsumer durabilityConsumer,
                            int primaryProtectionLevel,
                            int toughness,
                            float ricochetChance,
                            boolean isNatural) {
        this.sourceStack = sourceStack;
        this.subProtection = subProtection;
        this.subToughness = subToughness;
        this.subRicochetChance = subRicochetChance;
        this.materialFactor = materialFactor;
        this.durabilityConsumer = durabilityConsumer;
        this.primaryProtectionLevel = primaryProtectionLevel;
        this.toughness = toughness;
        this.ricochetChance = ricochetChance;
        this.isNatural = isNatural;
    }

    public ProtectionSource(ItemStack sourceStack,
                            Map<ModDamageSubPart, Integer> subProtection,
                            Map<ModDamageSubPart, Integer> subToughness,
                            Map<ModDamageSubPart, Float> subRicochetChance,
                            float materialFactor,
                            IntConsumer durabilityConsumer) {
        this(sourceStack, subProtection, subToughness, subRicochetChance,
                materialFactor, durabilityConsumer, 0, 0, 0.0f, false);
    }

    public ProtectionSource(ItemStack sourceStack,
                            float materialFactor,
                            IntConsumer durabilityConsumer,
                            int primaryProtectionLevel,
                            int toughness,
                            float ricochetChance,
                            boolean isNatural) {
        this(sourceStack, Map.of(), Map.of(), Map.of(),
                materialFactor, durabilityConsumer,
                primaryProtectionLevel, toughness, ricochetChance, isNatural);
    }

    public ItemStack getSourceStack() { return sourceStack; }
    public Map<ModDamageSubPart, Integer> getSubProtection() { return subProtection; }
    public Map<ModDamageSubPart, Integer> getSubToughness() { return subToughness; }
    public Map<ModDamageSubPart, Float> getSubRicochetChance() { return subRicochetChance; }
    public float getMaterialFactor() { return materialFactor; }
    public IntConsumer getDurabilityConsumer() { return durabilityConsumer; }
    public int getPrimaryProtectionLevel() { return primaryProtectionLevel; }
    public int getToughness() { return toughness; }
    public float getRicochetChance() { return ricochetChance; }
    public boolean isNatural() { return isNatural; }
}