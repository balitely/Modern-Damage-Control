package com.moderndamage.control.armor;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.config.ModClothConfig;
import com.moderndamage.control.entity.EntityHitboxHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ArmorCalculator {
    private static final Random RANDOM = new Random();

    public static class PenetrationResult {
        public final float finalDamage;
        public final boolean penetrated;   // 完全穿透
        public final boolean partial;      // 部分穿透

        public PenetrationResult(float finalDamage, boolean penetrated, boolean partial) {
            this.finalDamage = finalDamage;
            this.penetrated = penetrated;
            this.partial = partial;
        }
    }

    public static PenetrationResult applyArmorPenetration(LivingEntity target, ModDamagePart hitPart, float originalDamage, float penetration) {
        if (originalDamage <= 0) return new PenetrationResult(0, false, false);

        int armorLevel = getArmorLevel(target, hitPart);
        if (armorLevel == 0) {
            return new PenetrationResult(originalDamage, false, false);
        }

        int penetrationValue = (int) (penetration * 100);
        ModClothConfig config = ModClothConfig.get();
        int baseDurabilityLoss = config.durabilityLossBase;
        float extraPerPen = config.durabilityLossExtraPerPenetration;
        int maxLoss = config.maxDurabilityLoss;

        float finalDamage;
        int durabilityLoss;
        boolean penetrated = false;
        boolean partial = false;

        int diff = armorLevel - penetrationValue;

        if (penetrationValue >= armorLevel) {
            // 完全穿透
            finalDamage = originalDamage;
            int extra = (int) Math.ceil((penetrationValue - armorLevel) * extraPerPen);
            durabilityLoss = Math.min(maxLoss, baseDurabilityLoss + extra);
            penetrated = true;
        } else if (diff <= 9) {
            // 部分穿透区域 (差值 1~9)
            double chance = (10.0 - diff) / 10.0;
            if (RANDOM.nextDouble() <= chance) {
                // 部分穿透成功，伤害比例 0.5 ~ 0.8
                float ratio = 0.5f + RANDOM.nextFloat() * 0.3f;
                finalDamage = originalDamage * ratio;
                partial = true;
                durabilityLoss = baseDurabilityLoss;
            } else {
                // 部分穿透失败
                finalDamage = originalDamage * calculateBluntRatio(config, diff);
                durabilityLoss = baseDurabilityLoss;
            }
        } else {
            // 钝伤区域
            finalDamage = originalDamage * calculateBluntRatio(config, diff);
            durabilityLoss = baseDurabilityLoss;
        }

        // 扣除耐久
        ItemStack protectingItem = getProtectingItem(target, hitPart);
        if (protectingItem != null && !protectingItem.isEmpty()) {
            EquipmentSlot slot = getSlotForItem(target, protectingItem);
            if (slot != null) {
                protectingItem.hurtAndBreak(durabilityLoss, target, (p) -> p.broadcastBreakEvent(slot));
            }
        }

        if (config.debugMode) {
            ModernDamage.LOGGER.info("[Penetration] target={}, armor={}, pen={}, diff={}, partial={}, finalDamage={}",
                    target.getName().getString(), armorLevel, penetrationValue, diff, partial, finalDamage);
        }

        return new PenetrationResult(finalDamage, penetrated, partial);
    }

    private static float calculateBluntRatio(ModClothConfig config, int diff) {
        float base = config.bluntDamageRatio;          // diff=10时的基准比例
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
        int dynamic = Math.max(1, Math.round(baseLevel * multiplier));
        return dynamic;
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
        if (levels.size() == 1) return base;
        int otherSum = 0;
        for (int i = 1; i < levels.size(); i++) otherSum += levels.get(i);
        ModClothConfig config = ModClothConfig.get();
        float factor = config.armorStackingFactor;
        int total = base + Math.round(otherSum * factor);
        int cap = config.armorCap;
        if (cap > 0 && total > cap) total = cap;
        return total;
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

    private static EquipmentSlot getSlotForItem(LivingEntity target, ItemStack itemStack) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (target.getItemBySlot(slot) == itemStack) return slot;
        }
        return null;
    }
}