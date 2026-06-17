package com.moderndamage.control.capability.armstamina;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.attribute.ModAttributes;
import com.moderndamage.control.config.ModClothConfig;
import com.moderndamage.control.config.ModClothConfig.PenaltyModifier;
import com.moderndamage.control.network.Networking;
import com.moderndamage.control.network.SyncArmStaminaPacket;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class ArmStamina implements IArmStamina {
    private final LivingEntity entity;
    private float stamina;
    private int lastDrainTick = -1000;
    private boolean initialized = false;
    private boolean lastSyncedAiming = false;

    private final Map<UUID, AttributeModifier> activeLowPenalties = new HashMap<>();
    private final Map<UUID, AttributeModifier> activeCriticalPenalties = new HashMap<>();
    private boolean wasLow = false;
    private boolean wasCritical = false;
    private long lastCriticalEffectTime = 0;

    public ArmStamina(LivingEntity entity) {
        this.entity = entity;
    }

    private void init() {
        if (initialized) return;
        stamina = getMaxStamina();
        initialized = true;
    }

    @Override
    public float getStamina() {
        init();
        return stamina;
    }

    @Override
    public float getMaxStamina() {
        return (float) entity.getAttributeValue(ModAttributes.MAX_ARM_STAMINA.get());
    }

    @Override
    public void setStamina(float value) {
        init();
        float max = getMaxStamina();
        this.stamina = Math.max(0, Math.min(value, max));
        if (!entity.level().isClientSide) {
            updatePenalties();
        }
        sync();
    }

    @Override
    public void addStamina(float amount) {
        setStamina(getStamina() + amount);
    }

    @Override
    public boolean consumeStamina(float cost, boolean simulate) {
        if (entity instanceof Player && ((Player) entity).isSpectator()) {
            return true;
        }
        init();
        float multiplier = (float) entity.getAttributeValue(ModAttributes.ARM_STAMINA_DRAIN_MULTIPLIER.get());
        float actualCost = cost * multiplier;
        if (getStamina() >= actualCost) {
            if (!simulate) {
                setStamina(getStamina() - actualCost);
                lastDrainTick = entity.tickCount;
            }
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        if (entity.level().isClientSide) return;
        init();

        ModClothConfig config = ModClothConfig.get();
        if (!config.enableArmStamina) return;

        int delay = config.staminaRegenDelayTicks;
        if (entity.tickCount - lastDrainTick > delay) {
            float regenPerTick = (float) entity.getAttributeValue(ModAttributes.ARM_STAMINA_REGEN.get()) / 20f;
            addStamina(regenPerTick);
        }

        boolean currentAiming = false;
        try {
            IGunOperator operator = IGunOperator.fromLivingEntity(entity);
            if (operator != null) {
                ShooterDataHolder data = operator.getDataHolder();
                currentAiming = data != null && data.isAiming;
            }
        } catch (Throwable ignored) {}
        if (currentAiming != lastSyncedAiming) {
            lastSyncedAiming = currentAiming;
            sync();
        }

        updatePenalties();
    }

    private void removeAllPossiblePenalties() {
        try {
            ModClothConfig config = ModClothConfig.get();
            if (config == null) return;
            for (PenaltyModifier mod : config.lowStaminaPenalties.modifiers) {
                removePenaltyByModifier(mod, false);
            }
            for (PenaltyModifier mod : config.criticallyLowStaminaPenalties.modifiers) {
                removePenaltyByModifier(mod, true);
            }
        } catch (Exception e) {
            ModernDamage.LOGGER.error("Error in removeAllPossiblePenalties for {}", entity.getName().getString(), e);
        }
    }

    private void removePenaltyByModifier(PenaltyModifier mod, boolean isCritical) {
        if (mod == null || mod.attribute == null) return;
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(mod.attribute));
        if (attribute == null) return;
        UUID uuid = UUID.nameUUIDFromBytes(("arm_stamina_penalty_" + mod.attribute + (isCritical ? "_critical" : "_low")).getBytes());
        var instance = entity.getAttribute(attribute);
        if (instance != null && instance.getModifier(uuid) != null) {
            instance.removeModifier(uuid);
        }
    }

    private void updatePenalties() {
        try {
            ModClothConfig config = ModClothConfig.get();
            if (config == null) return;
            float ratio = getStamina() / getMaxStamina();
            boolean isLow = ratio <= config.lowStaminaThreshold;
            boolean isCritical = ratio <= config.criticallyLowStaminaThreshold;

            if (!isLow) {
                removeAllPossiblePenalties();
                activeLowPenalties.clear();
                activeCriticalPenalties.clear();
                wasLow = false;
                wasCritical = false;
                return;
            }

            if (!isCritical) {
                applyPenalty(config.lowStaminaPenalties.modifiers, activeLowPenalties, false);
                wasLow = true;
            }

            if (isCritical) {
                applyPenalty(config.criticallyLowStaminaPenalties.modifiers, activeCriticalPenalties, true);
                if (entity instanceof Player player && config.criticallyLowStaminaPenalties.effect != null) {
                    long now = entity.tickCount;
                    if (now - lastCriticalEffectTime > 100) {
                        lastCriticalEffectTime = now;
                        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(config.criticallyLowStaminaPenalties.effect));
                        if (effect != null) {
                            player.addEffect(new MobEffectInstance(effect,
                                    config.criticallyLowStaminaPenalties.effectDuration,
                                    config.criticallyLowStaminaPenalties.effectAmplifier,
                                    false, true, true));
                        }
                    }
                }
                wasCritical = true;
                wasLow = true;
            }
        } catch (Exception e) {
            ModernDamage.LOGGER.error("Error in updatePenalties for {}", entity.getName().getString(), e);
        }
    }

    private void applyPenalty(List<PenaltyModifier> modifiers, Map<UUID, AttributeModifier> cache, boolean isCritical) {
        if (modifiers == null) return;
        for (PenaltyModifier mod : modifiers) {
            if (mod == null || mod.attribute == null) continue;
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(mod.attribute));
            if (attribute == null) continue;
            AttributeModifier.Operation op;
            switch (mod.operation.toLowerCase()) {
                case "multiply_base":
                    op = AttributeModifier.Operation.MULTIPLY_BASE;
                    break;
                case "multiply_total":
                    op = AttributeModifier.Operation.MULTIPLY_TOTAL;
                    break;
                default:
                    op = AttributeModifier.Operation.ADDITION;
            }
            UUID uuid = UUID.nameUUIDFromBytes(("arm_stamina_penalty_" + mod.attribute + (isCritical ? "_critical" : "_low")).getBytes());
            if (!cache.containsKey(uuid)) {
                AttributeModifier modifier = new AttributeModifier(uuid, "arm_stamina_penalty", mod.amount, op);
                var instance = entity.getAttribute(attribute);
                if (instance != null && !instance.hasModifier(modifier)) {
                    instance.addTransientModifier(modifier);
                    cache.put(uuid, modifier);
                }
            }
        }
    }

    public void sync() {
        if (entity.level().isClientSide) return;
        if (entity instanceof ServerPlayer serverPlayer) {
            boolean isAiming = false;
            try {
                IGunOperator operator = IGunOperator.fromLivingEntity(entity);
                if (operator != null) {
                    ShooterDataHolder data = operator.getDataHolder();
                    isAiming = data != null && data.isAiming;
                }
            } catch (Throwable ignored) {}
            Networking.sendToPlayer(new SyncArmStaminaPacket(entity.getUUID(), stamina, isAiming), serverPlayer);
        }
    }

    @Override
    public void reset() {
        stamina = getMaxStamina();
        lastDrainTick = -1000;
        removeAllPossiblePenalties();
        activeLowPenalties.clear();
        activeCriticalPenalties.clear();
        wasLow = false;
        wasCritical = false;
        sync();
    }

    @Override
    public int getLastDrainTick() {
        return lastDrainTick;
    }

    @Override
    public Map<UUID, AttributeModifier> getActiveLowPenalties() {
        return activeLowPenalties;
    }

    @Override
    public Map<UUID, AttributeModifier> getActiveCriticalPenalties() {
        return activeCriticalPenalties;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Stamina", stamina);
        tag.putInt("LastDrainTick", lastDrainTick);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        try {
            stamina = tag.getFloat("Stamina");
            lastDrainTick = tag.getInt("LastDrainTick");
            initialized = true;
        } catch (Exception e) {
            ModernDamage.LOGGER.error("Failed to deserialize arm stamina for entity {}", entity.getName().getString(), e);
            stamina = getMaxStamina();
            lastDrainTick = -1000;
            initialized = true;
        }
    }
}