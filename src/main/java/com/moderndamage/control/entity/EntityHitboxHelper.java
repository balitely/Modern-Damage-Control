package com.moderndamage.control.entity;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.attribute.ModAttributes;
import com.moderndamage.control.config.EntityHitboxConfig;
import com.moderndamage.control.config.EntityHitboxConfigLoader;
import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.phys.Vec3;

public class EntityHitboxHelper {

    public static ModDamagePart getHitPart(LivingEntity entity, Vec3 localPos) {
        EntityHitboxConfig cfg = EntityHitboxConfigLoader.getConfig(entity.getType());
        float y = (float) localPos.y;
        float absX = (float) Math.abs(localPos.x);
        if (y >= cfg.headHeight[0] && y <= cfg.headHeight[1]) {
            return ModDamagePart.HEAD;
        }
        if (y >= cfg.bodyHeight[0] && y <= cfg.bodyHeight[1]) {
            float bottom = cfg.bodyHeight[0];
            float top = cfg.bodyHeight[1];
            float chestSplit = bottom + (top - bottom) * cfg.chestRatio;
            if (y >= chestSplit) {
                return ModDamagePart.CHEST;
            } else {
                return ModDamagePart.STOMACH;
            }
        }
        boolean armValid = cfg.armXRange[0] < cfg.armXRange[1];
        if (armValid && absX >= cfg.armXRange[0] && absX <= cfg.armXRange[1]) {
            return localPos.x > 0 ? ModDamagePart.RIGHT_ARM : ModDamagePart.LEFT_ARM;
        }
        if (y < cfg.bodyHeight[0] && absX >= cfg.legXRange[0] && absX <= cfg.legXRange[1]) {
            return localPos.x > 0 ? ModDamagePart.RIGHT_LEG : ModDamagePart.LEFT_LEG;
        }
        return ModDamagePart.CHEST;
    }

    public static int getNaturalArmor(LivingEntity entity, ModDamagePart part) {
        try {
            double value = 0;
            switch (part) {
                case HEAD:
                    value = entity.getAttributeValue(ModAttributes.HEAD_NATURAL_ARMOR.get());
                    break;
                case CHEST:
                    value = entity.getAttributeValue(ModAttributes.CHEST_NATURAL_ARMOR.get());
                    break;
                case STOMACH:
                    value = entity.getAttributeValue(ModAttributes.STOMACH_NATURAL_ARMOR.get());
                    break;
                case LEFT_ARM:
                case RIGHT_ARM:
                    value = entity.getAttributeValue(ModAttributes.ARM_NATURAL_ARMOR.get());
                    break;
                case LEFT_LEG:
                case RIGHT_LEG:
                    value = entity.getAttributeValue(ModAttributes.LEG_NATURAL_ARMOR.get());
                    break;
            }
            return (int) Math.round(value);
        } catch (IllegalArgumentException e) {
            ModernDamage.LOGGER.warn("Failed to get natural armor attribute for {}: {}", part, e.getMessage());
            return 0;
        }
    }

    public static int getNaturalToughness(LivingEntity entity, ModDamagePart part) {
        try {
            AttributeInstance instance = null;
            switch (part) {
                case HEAD:
                    instance = entity.getAttribute(ModAttributes.HEAD_NATURAL_TOUGHNESS.get());
                    break;
                case CHEST:
                    instance = entity.getAttribute(ModAttributes.CHEST_NATURAL_TOUGHNESS.get());
                    break;
                case STOMACH:
                    instance = entity.getAttribute(ModAttributes.STOMACH_NATURAL_TOUGHNESS.get());
                    break;
                case LEFT_ARM:
                case RIGHT_ARM:
                    instance = entity.getAttribute(ModAttributes.ARM_NATURAL_TOUGHNESS.get());
                    break;
                case LEFT_LEG:
                case RIGHT_LEG:
                    instance = entity.getAttribute(ModAttributes.LEG_NATURAL_TOUGHNESS.get());
                    break;
            }
            if (instance == null) {
                if (ModClothConfig.get().debugMode) {
                    ModernDamage.LOGGER.debug("Natural toughness attribute instance missing for {} {}", entity.getName().getString(), part);
                }
                return 0;
            }
            double value = instance.getBaseValue();
            int toughness = (int) Math.round(value);
            ModernDamage.LOGGER.info("getNaturalToughness: {} {} = {}", entity.getName().getString(), part, toughness);
            return toughness;
        } catch (Exception e) {
            ModernDamage.LOGGER.warn("Failed to get natural toughness for {}: {}", part, e.getMessage());
            return 0;
        }
    }

    public static boolean isArmorEnabled(LivingEntity entity) {
        return EntityHitboxConfigLoader.getConfig(entity.getType()).useArmor;
    }
}