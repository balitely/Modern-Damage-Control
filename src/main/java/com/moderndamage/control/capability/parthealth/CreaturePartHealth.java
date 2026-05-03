package com.moderndamage.control.capability.parthealth;

import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.config.EffectEntry;
import com.moderndamage.control.config.EntityBodyPartEffectConfig;
import com.moderndamage.control.config.EntityEffectConfig;
import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumMap;
import java.util.Map;

public class CreaturePartHealth implements IPartHealth {
    private final LivingEntity entity;
    private final Map<ModDamagePart, Float> health = new EnumMap<>(ModDamagePart.class);
    private final Map<ModDamagePart, Boolean> destroyed = new EnumMap<>(ModDamagePart.class);
    private boolean dead = false;
    private boolean initialized = false;
    private int lastDamageTick = 0;

    public CreaturePartHealth(LivingEntity entity) {
        this.entity = entity;
    }

    private boolean tryInit() {
        if (initialized) return true;
        if (entity.getAttributes() == null) return false;
        if (entity.getAttribute(Attributes.MAX_HEALTH) == null) return false;
        for (ModDamagePart part : ModDamagePart.values()) {
            float max = calculateMaxHealth(part);
            health.put(part, max);
            destroyed.put(part, false);
        }
        initialized = true;
        return true;
    }

    private ModClothConfig.CreaturePartRatios getPartRatios() {
        ModClothConfig config = ModClothConfig.get();
        String key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
        return config.entityPartRatios.getOrDefault(key, null);
    }

    private float calculateMaxHealth(ModDamagePart part) {
        float total = entity.getMaxHealth();
        ModClothConfig.CreaturePartRatios custom = getPartRatios();
        if (custom != null) {
            switch (part) {
                case HEAD: return total * custom.head;
                case CHEST: return total * custom.chest;
                case STOMACH: return total * custom.stomach;
                case LEFT_ARM: return total * custom.leftArm;
                case RIGHT_ARM: return total * custom.rightArm;
                case LEFT_LEG: return total * custom.leftLeg;
                case RIGHT_LEG: return total * custom.rightLeg;
                default: return 0;
            }
        } else {
            ModClothConfig config = ModClothConfig.get();
            switch (part) {
                case HEAD: return total * config.creatureHeadRatio;
                case CHEST: return total * config.creatureChestRatio;
                case STOMACH: return total * config.creatureStomachRatio;
                case LEFT_ARM: return total * config.creatureLeftArmRatio;
                case RIGHT_ARM: return total * config.creatureRightArmRatio;
                case LEFT_LEG: return total * config.creatureLeftLegRatio;
                case RIGHT_LEG: return total * config.creatureRightLegRatio;
                default: return 0;
            }
        }
    }

    @Override
    public float getMaxHealth(ModDamagePart part) {
        if (!tryInit()) return 0;
        return calculateMaxHealth(part);
    }

    @Override
    public float getHealth(ModDamagePart part) {
        if (!tryInit()) return 0;
        return health.getOrDefault(part, calculateMaxHealth(part));
    }

    @Override
    public float getTotalHealthPercent() {
        if (!tryInit()) return 1.0f;
        float total = 0, maxTotal = 0;
        for (ModDamagePart part : ModDamagePart.values()) {
            total += getHealth(part);
            maxTotal += getMaxHealth(part);
        }
        return maxTotal == 0 ? 0 : total / maxTotal;
    }

    @Override
    public boolean isPartDestroyed(ModDamagePart part) {
        if (!tryInit()) return false;
        return destroyed.getOrDefault(part, false);
    }

    @Override
    public void reset() {
        if (entity.getAttributes() == null) return;
        initialized = false;
        dead = false;
        tryInit();
    }

    private void applyDestroyEffects(ModDamagePart part) {
        EntityEffectConfig entityConfig = ModClothConfig.getEntityEffectConfig(entity.getType());
        if (!entityConfig.enabled) return;
        EntityBodyPartEffectConfig partConfig = entityConfig.getPartConfig(part);
        if (!partConfig.enabled) return;
        for (EffectEntry entry : partConfig.destroy) {
            if (entry.isValid() && entity.getRandom().nextDouble() <= entry.getChance()) {
                entity.addEffect(new MobEffectInstance(entry.getEffect(), entry.getDuration(), entry.getAmplifier()));
            }
        }
    }

    @Override
    public boolean damagePart(ModDamagePart part, float amount) {
        if (!tryInit()) return false;
        if (dead || amount <= 0) return false;

        lastDamageTick = entity.tickCount;
        float current = getHealth(part);
        float newHealth = current - amount;
        if (newHealth <= 0) {
            if (current > 0) {
                applyDestroyEffects(part);
            }
            health.put(part, 0f);
            destroyed.put(part, true);
            float overflow = -newHealth;
            if (overflow > 0 && part != ModDamagePart.HEAD && part != ModDamagePart.CHEST) {
                ModClothConfig config = ModClothConfig.get();
                float factor = (part == ModDamagePart.LEFT_ARM || part == ModDamagePart.RIGHT_ARM)
                        ? config.armOverflowToChest
                        : config.legOverflowToChest;
                damagePart(ModDamagePart.CHEST, overflow * factor);
            }
        } else {
            health.put(part, newHealth);
        }

        if (getHealth(ModDamagePart.HEAD) <= 0 || getHealth(ModDamagePart.CHEST) <= 0 || getTotalHealthPercent() <= 0) {
            dead = true;
            entity.setHealth(0);
            return true;
        }
        entity.setHealth(getTotalHealthPercent() * entity.getMaxHealth());
        return false;
    }

    @Override
    public boolean damageAll(float amount) {
        return damagePart(ModDamagePart.CHEST, amount);
    }

    @Override
    public void healAll(float amount) {
        if (dead) return;
        float maxTotal = 0, currentTotal = 0;
        for (ModDamagePart part : ModDamagePart.values()) {
            maxTotal += getMaxHealth(part);
            currentTotal += getHealth(part);
        }
        float newTotal = Math.min(maxTotal, currentTotal + amount);
        float ratio = newTotal / currentTotal;
        for (ModDamagePart part : ModDamagePart.values()) {
            float newVal = getHealth(part) * ratio;
            health.put(part, newVal);
            if (newVal <= 0) destroyed.put(part, true);
            else if (newVal > 0) destroyed.put(part, false);
        }
        entity.setHealth(getTotalHealthPercent() * entity.getMaxHealth());
    }

    @Override
    public int getLastDamageTick() { return lastDamageTick; }

    @Override
    public void tick() {
        if (!initialized) tryInit();
    }
}