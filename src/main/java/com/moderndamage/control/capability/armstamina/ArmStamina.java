package com.moderndamage.control.capability.armstamina;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.attribute.ModAttributes;
import com.moderndamage.control.config.ModClothConfig;
import com.moderndamage.control.config.ModClothConfig.PenaltyModifier;
import com.moderndamage.control.network.Networking;
import com.moderndamage.control.network.SyncArmStaminaPacket;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class ArmStamina implements IArmStamina {
    private final LivingEntity entity;
    private float stamina;
    private int lastDrainTick = -1000;
    private boolean initialized = false;
    private boolean lastSyncedAiming = false;

    // 缓存已添加的惩罚修饰符，用于移除
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
        // 耐力变化时重新评估惩罚（在 tick 中也会做，但这里立即触发可提升响应）
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

        // 恢复耐力（根据配置的延迟 ticks）
        int delay = config.staminaRegenDelayTicks;
        if (entity.tickCount - lastDrainTick > delay) {
            float regenPerTick = (float) entity.getAttributeValue(ModAttributes.ARM_STAMINA_REGEN.get()) / 20f;
            addStamina(regenPerTick);
        }

        // 检测瞄准状态变化，并同步给客户端（用于镜头晃动）
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
            sync(); // 触发一次同步（同时发送耐力值和瞄准状态）
        }

        // 根据当前耐力比例应用或移除惩罚效果
        updatePenalties();
    }

    private void updatePenalties() {
        ModClothConfig config = ModClothConfig.get();
        float ratio = getStamina() / getMaxStamina();
        boolean isLow = ratio <= config.lowStaminaThreshold;
        boolean isCritical = ratio <= config.criticallyLowStaminaThreshold;

        // 低耐力惩罚（非极低时也应用低惩罚）
        if (isLow && !isCritical) {
            applyPenalty(config.lowStaminaPenalties.modifiers, activeLowPenalties, false);
            // 如果之前是极低，移除极低惩罚
            if (wasCritical) {
                removePenalty(activeCriticalPenalties);
                wasCritical = false;
            }
            wasLow = true;
        } else if (!isLow) {
            // 移除所有惩罚
            if (wasLow || wasCritical) {
                removePenalty(activeLowPenalties);
                removePenalty(activeCriticalPenalties);
                wasLow = false;
                wasCritical = false;
            }
        }

        // 极低耐力惩罚（独立于低耐力，会叠加）
        if (isCritical) {
            applyPenalty(config.criticallyLowStaminaPenalties.modifiers, activeCriticalPenalties, true);
            // 施加药水效果（周期性，避免每 tick 重复添加）
            if (entity instanceof Player player && config.criticallyLowStaminaPenalties.effect != null) {
                long now = entity.tickCount;
                // 每 100 tick (5秒) 重新施加一次，保证持续时间刷新
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
            wasLow = true; // 低标记也置为 true，以便退出极低时能清除低惩罚
        }
    }

    private void applyPenalty(List<PenaltyModifier> modifiers, Map<UUID, AttributeModifier> cache, boolean isCritical) {
        if (modifiers == null) return;
        for (PenaltyModifier mod : modifiers) {
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(mod.attribute));
            if (attribute == null) continue;
            AttributeModifier.Operation op;
            switch (mod.operation.toLowerCase()) {
                case "multiply_base": op = AttributeModifier.Operation.MULTIPLY_BASE; break;
                case "multiply_total": op = AttributeModifier.Operation.MULTIPLY_TOTAL; break;
                default: op = AttributeModifier.Operation.ADDITION;
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

    private void removePenalty(Map<UUID, AttributeModifier> cache) {
        for (Map.Entry<UUID, AttributeModifier> entry : cache.entrySet()) {
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(entry.getValue().getName()));
            if (attribute != null) {
                var instance = entity.getAttribute(attribute);
                if (instance != null) {
                    instance.removeModifier(entry.getKey());
                }
            }
        }
        cache.clear();
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
        removePenalty(activeLowPenalties);
        removePenalty(activeCriticalPenalties);
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
        stamina = tag.getFloat("Stamina");
        lastDrainTick = tag.getInt("LastDrainTick");
        initialized = true;
        // 重生后需要重新评估惩罚（在 tick 中会自动处理）
    }
}