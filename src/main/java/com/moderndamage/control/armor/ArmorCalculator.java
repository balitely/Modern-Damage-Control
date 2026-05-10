package com.moderndamage.control.armor;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.api.event.ArmorHitEvent;
import com.moderndamage.control.api.event.GetArmorLevelEvent;
import com.moderndamage.control.capability.parthealth.CreaturePartHealthCapability;
import com.moderndamage.control.capability.parthealth.PartHealthCapability;
import com.moderndamage.control.config.ModClothConfig;
import com.moderndamage.control.entity.EntityHitboxHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ArmorCalculator {
    private static final Random RANDOM = new Random();

    public static class PenetrationResult {
        public final float finalDamage;
        public final boolean penetrated;
        public final boolean partial;

        public PenetrationResult(float finalDamage, boolean penetrated, boolean partial) {
            this.finalDamage = finalDamage;
            this.penetrated = penetrated;
            this.partial = partial;
        }
    }

    private static int getTotalToughness(LivingEntity target, ModDamagePart hitPart) {
        int total = EntityHitboxHelper.getNaturalToughness(target, hitPart);
        ModernDamage.LOGGER.info("getTotalToughness: natural toughness = {}", total);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack stack = target.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            ArmorData data = ArmorDataLoader.getArmorData(stack.getItem());
            if (data != null && data.protects(hitPart)) {
                int toughness = getDynamicToughness(stack, hitPart);
                if (toughness > 0) {
                    total += toughness;
                    if (ModClothConfig.get().debugMode) {
                        ModernDamage.LOGGER.debug("[Toughness] Added armor toughness from {} (part {}): +{} → total {}",
                                ForgeRegistries.ITEMS.getKey(stack.getItem()), hitPart, toughness, total);
                    }
                }
            }
        }
        return Math.min(total, 100);
    }

    public static int getDynamicToughness(ItemStack armorStack, ModDamagePart part) {
        ArmorData data = ArmorDataLoader.getArmorData(armorStack.getItem());
        if (data == null) return 0;
        int base = data.getBaseToughness(part);
        if (base <= 0) return 0;
        int maxDurability = armorStack.getMaxDamage();
        if (maxDurability <= 0) return base;
        int currentDurability = maxDurability - armorStack.getDamageValue();
        float durabilityPercent = (float) currentDurability / (float) maxDurability;
        return Math.round(base * durabilityPercent);
    }

    private static float getMaterialFactor(ItemStack armorStack, ModDamagePart part) {
        ArmorData data = ArmorDataLoader.getArmorData(armorStack.getItem());
        if (data == null) return 1.0f;
        return data.getMaterialFactor(part);
    }

    private static ItemStack getProtectingItem(LivingEntity target, ModDamagePart hitPart) {
        int bestLevel = 0;
        ItemStack bestItem = ItemStack.EMPTY;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack stack = target.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            ArmorData data = ArmorDataLoader.getArmorData(stack.getItem());
            if (data != null && data.protects(hitPart)) {
                int level = data.getProtectionLevel(hitPart);
                if (level > bestLevel) {
                    bestLevel = level;
                    bestItem = stack;
                }
            }
        }
        return bestItem;
    }

    public static PenetrationResult applyArmorPenetration(LivingEntity target, ModDamagePart hitPart,
                                                          float originalDamage, float penetration) {
        if (originalDamage <= 0) return new PenetrationResult(0, false, false);

        int armorLevel = getArmorLevel(target, hitPart);
        ModClothConfig config = ModClothConfig.get();
        if (config.enableRicochet) {
            ItemStack protectingItem = getProtectingItem(target, hitPart);
            if (!protectingItem.isEmpty()) {
                ArmorData data = ArmorDataLoader.getArmorData(protectingItem.getItem());
                if (data != null) {
                    float chance = data.getRicochetChance(hitPart);
                    if (chance > 0 && target.getRandom().nextFloat() < chance) {
                        float ricochetDamage = originalDamage * config.ricochetDamageRatio;
                        if (ricochetDamage < 1) ricochetDamage = 1;
                        final float finalRicochetDamage = ricochetDamage;
                        final ModDamagePart finalHitPart = hitPart;
                        if (config.debugMode) {
                            ModernDamage.LOGGER.info("[Ricochet] {} ricocheted off {} {}! Damage: {} -> {}",
                                    target.getName().getString(),
                                    ForgeRegistries.ITEMS.getKey(protectingItem.getItem()),
                                    hitPart, originalDamage, finalRicochetDamage);
                        }
                        if (target instanceof Player && config.damageModel == ModClothConfig.DamageModel.HARDCORE) {
                            target.getCapability(PartHealthCapability.PART_HEALTH_CAP).ifPresent(cap -> cap.damagePart(finalHitPart, finalRicochetDamage));
                        } else if (config.creaturePartHealthEnabled && config.damageModel == ModClothConfig.DamageModel.HARDCORE) {
                            target.getCapability(CreaturePartHealthCapability.CREATURE_PART_HEALTH_CAP).ifPresent(cap -> cap.damagePart(finalHitPart, finalRicochetDamage));
                        } else {
                            target.setHealth(Math.max(0, target.getHealth() - finalRicochetDamage));
                        }
                        return new PenetrationResult(finalRicochetDamage, false, false);
                    }
                }
            }
        }
        if (armorLevel == 0) {
            return new PenetrationResult(originalDamage, false, false);
        }

        int penetrationValue = (int) (penetration * 100);
        int baseDurabilityLoss = config.durabilityLossBase;
        int maxLoss = config.maxDurabilityLoss;

        float partialMin = config.partialPenetrationMinRatio;
        float partialMax = config.partialPenetrationMaxRatio;

        float finalDamageBeforeToughness;
        boolean penetrated = false;
        boolean partial = false;

        if (penetrationValue >= armorLevel + 5) {
            finalDamageBeforeToughness = originalDamage;
            penetrated = true;
        } else if (penetrationValue >= armorLevel - 10) {
            int diff = (armorLevel + 6) - penetrationValue;
            double chance = (16.0 - diff) / 15.0;
            chance = Math.min(1.0, Math.max(0.0, chance));
            if (RANDOM.nextDouble() <= chance) {
                float ratio = partialMin + RANDOM.nextFloat() * (partialMax - partialMin);
                finalDamageBeforeToughness = originalDamage * ratio;
                partial = true;
            } else {
                int bluntDiff = armorLevel - penetrationValue;
                finalDamageBeforeToughness = originalDamage * calculateBluntRatio(config, bluntDiff);
            }
        } else {
            int bluntDiff = armorLevel - penetrationValue;
            finalDamageBeforeToughness = originalDamage * calculateBluntRatio(config, bluntDiff);
        }

        ItemStack protectingItem = getProtectingItem(target, hitPart);
        float materialFactor = 1.0f;
        if (!protectingItem.isEmpty()) {
            materialFactor = getMaterialFactor(protectingItem, hitPart);
        }
        float penRatio = (float) penetrationValue / (float) armorLevel;
        int durabilityLoss = (int) Math.ceil(1 + finalDamageBeforeToughness * penRatio * materialFactor);
        durabilityLoss = Math.min(maxLoss, Math.max(1, durabilityLoss));

        boolean cancelDurabilityLoss = false;
        if (!target.level().isClientSide) {
            ArmorHitEvent event = new ArmorHitEvent(target, hitPart, originalDamage, finalDamageBeforeToughness, armorLevel);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                cancelDurabilityLoss = true;
            }
            finalDamageBeforeToughness = event.getFinalDamage();
        }

        if (protectingItem != null && !protectingItem.isEmpty() && !cancelDurabilityLoss) {
            EquipmentSlot slot = getSlotForItem(target, protectingItem);
            if (slot != null) {
                protectingItem.hurtAndBreak(durabilityLoss, target, (p) -> p.broadcastBreakEvent(slot));
            }
        }

        int totalToughness = getTotalToughness(target, hitPart);
        float finalDamage = finalDamageBeforeToughness;
        if (totalToughness > 0) {
            float reduction = totalToughness / 100.0f;
            finalDamage = finalDamageBeforeToughness * (1 - reduction);
            if (finalDamage < 0) finalDamage = 0;
        }

        if (config.debugMode) {
            ModernDamage.LOGGER.info("[Penetration] target={}, part={}, armor={}, pen={}, dmg: orig={}, beforeTough={}, final={}, toughness={}, durLoss={}, penType={}",
                    target.getName().getString(), hitPart, armorLevel, penetrationValue,
                    originalDamage, finalDamageBeforeToughness, finalDamage, totalToughness, durabilityLoss,
                    penetrated ? "FULL" : (partial ? "PARTIAL" : "BLUNT"));
        }

        return new PenetrationResult(finalDamage, penetrated, partial);
    }

    private static float calculateBluntRatio(ModClothConfig config, int diff) {
        float base = config.bluntDamageRatio;
        float halveDist = config.bluntDamageHalveDistance;
        float minRatio = config.bluntDamageMinRatio;
        float exponent = (diff - 10) / halveDist;
        float ratio = (float) (base * Math.pow(0.5, exponent));
        return Math.max(ratio, minRatio);
    }

    public static int getDynamicProtectionLevel(ItemStack armorStack, ModDamagePart part) {
        ArmorData data = ArmorDataLoader.getArmorData(armorStack.getItem());
        if (data == null) return 0;
        int baseLevel = data.getProtectionLevel(part);
        if (baseLevel == 0) return 0;
        int maxDurability = armorStack.getMaxDamage();
        if (maxDurability <= 0) return baseLevel;
        int currentDurability = maxDurability - armorStack.getDamageValue();
        float durabilityPercent = (float) currentDurability / maxDurability;
        float multiplier = 0.5f + 0.5f * durabilityPercent;
        return Math.max(1, Math.round(baseLevel * multiplier));
    }

    public static int getProtectionLevel(LivingEntity target, ModDamagePart hitPart) {
        return getArmorLevel(target, hitPart);
    }

    private static int getArmorLevel(LivingEntity target, ModDamagePart hitPart) {
        List<Integer> levels = new ArrayList<>();
        int natural = EntityHitboxHelper.getNaturalArmor(target, hitPart);
        if (natural > 0) levels.add(natural);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack stack = target.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            ArmorData data = ArmorDataLoader.getArmorData(stack.getItem());
            if (data != null && data.protects(hitPart)) {
                int level = getDynamicProtectionLevel(stack, hitPart);
                levels.add(level);
            }
        }
        if (levels.isEmpty()) return 0;
        levels.sort(Collections.reverseOrder());
        int base = levels.get(0);
        if (levels.size() == 1) {
            int total = base;
            if (!target.level().isClientSide) {
                GetArmorLevelEvent event = new GetArmorLevelEvent(target, hitPart, total);
                MinecraftForge.EVENT_BUS.post(event);
                total = event.getArmorLevel();
            }
            return total;
        }
        int otherSum = 0;
        for (int i = 1; i < levels.size(); i++) otherSum += levels.get(i);
        ModClothConfig config = ModClothConfig.get();
        float factor = config.armorStackingFactor;
        int total = base + Math.round(otherSum * factor);
        int cap = config.armorCap;
        if (cap > 0 && total > cap) total = cap;

        if (!target.level().isClientSide) {
            GetArmorLevelEvent event = new GetArmorLevelEvent(target, hitPart, total);
            MinecraftForge.EVENT_BUS.post(event);
            total = event.getArmorLevel();
        }
        return total;
    }

    private static EquipmentSlot getSlotForItem(LivingEntity target, ItemStack itemStack) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (target.getItemBySlot(slot) == itemStack) return slot;
        }
        return null;
    }
}