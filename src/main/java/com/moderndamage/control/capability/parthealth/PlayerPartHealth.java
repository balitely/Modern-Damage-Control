package com.moderndamage.control.capability.parthealth;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.config.EffectEntry;
import com.moderndamage.control.config.ModClothConfig;
import com.moderndamage.control.effect.ModEffects;
import com.moderndamage.control.network.Networking;
import com.moderndamage.control.network.SyncPartHealthPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class PlayerPartHealth implements IPartHealth {
    private final Player player;
    private final Map<ModDamagePart, Float> health = new EnumMap<>(ModDamagePart.class);
    private final Map<ModDamagePart, Boolean> destroyed = new EnumMap<>(ModDamagePart.class);
    private boolean dead = false;
    private boolean hasDied = false;
    private boolean initialized = false;
    private int lastDamageTick = 0;
    private int lastSyncTick = 0;

    public PlayerPartHealth(Player player) {
        this.player = player;
    }

    private boolean tryInit() {
        if (initialized) return true;
        if (player.getAttributes() == null) return false;
        for (ModDamagePart part : ModDamagePart.values()) {
            float max = getMaxHealthRaw(part);
            health.put(part, max);
            destroyed.put(part, false);
        }
        initialized = true;
        return true;
    }

    private float getMaxHealthRaw(ModDamagePart part) {
        float ratio = ModClothConfig.getPlayerPartRatio(part);
        return player.getMaxHealth() * ratio;
    }

    @Override
    public float getMaxHealth(ModDamagePart part) {
        if (!tryInit()) return 0;
        return getMaxHealthRaw(part);
    }

    @Override
    public float getHealth(ModDamagePart part) {
        if (!tryInit()) return 0;
        return health.getOrDefault(part, getMaxHealthRaw(part));
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
        if (player.getAttributes() == null) return;
        dead = false;
        hasDied = false;
        for (ModDamagePart part : ModDamagePart.values()) {
            float max = getMaxHealthRaw(part);
            health.put(part, max);
            destroyed.put(part, false);
        }
        initialized = true;
        lastDamageTick = 0;
        updateVanillaHealth();
        syncToClient(true);
        ModernDamage.LOGGER.debug("PlayerPartHealth reset for {}", player.getName().getString());
    }

    private void updateVanillaHealth() {
        float newTotal = getTotalHealthPercent() * player.getMaxHealth();
        player.setHealth(Math.max(0, Math.min(player.getMaxHealth(), newTotal)));
    }

    private void syncToClient() {
        syncToClient(false);
    }

    private void syncToClient(boolean force) {
        if (player.level().isClientSide) return;
        if (!force && player.tickCount - lastSyncTick < 2) return;
        lastSyncTick = player.tickCount;
        if (player instanceof ServerPlayer serverPlayer) {
            Networking.sendToPlayer(new SyncPartHealthPacket(player.getUUID(), getHealthMap(), getMaxHealthMap()), serverPlayer);
        }
    }

    public Map<ModDamagePart, Float> getHealthMap() {
        return new EnumMap<>(health);
    }

    private Map<ModDamagePart, Float> getMaxHealthMap() {
        Map<ModDamagePart, Float> map = new EnumMap<>(ModDamagePart.class);
        for (ModDamagePart part : ModDamagePart.values()) {
            map.put(part, getMaxHealth(part));
        }
        return map;
    }

    private void applyDestroyEffects(ModDamagePart part) {
        ModClothConfig config = ModClothConfig.get();
        String key = part.getConfigKey();
        ModClothConfig.BodyPartConfig partConfig = config.bodyParts.get(key);
        if (partConfig != null && partConfig.destroy != null) {
            for (EffectEntry entry : partConfig.destroy) {
                if (entry.isValid() && player.getRandom().nextDouble() <= entry.getChance()) {
                    player.addEffect(new MobEffectInstance(entry.getEffect(), entry.getDuration(), entry.getAmplifier()));
                }
            }
        }
    }

    @Override
    public boolean damagePart(ModDamagePart part, float amount) {
        return damagePart(part, amount, player.damageSources().magic());
    }

    @Override
    public boolean damagePart(ModDamagePart part, float amount, DamageSource source) {
        if (!tryInit()) return false;
        if (dead || hasDied || player.isDeadOrDying()) {
            ModernDamage.LOGGER.warn("damagePart ignored, already dead/hasDied for {} part={}", player.getName().getString(), part);
            return false;
        }
        if (amount <= 0) return false;

        ModernDamage.LOGGER.info("damagePart called for {} part={} amount={} source={}", player.getName().getString(), part, amount, source.getMsgId());

        lastDamageTick = player.tickCount;
        float current = getHealth(part);
        float newHealth = current - amount;
        if (newHealth <= 0) {
            if (health.get(part) > 0) {
                applyDestroyEffects(part);
            }
            health.put(part, 0f);
            destroyed.put(part, true);
            float overflow = -newHealth;
            if (overflow > 0) {
                distributeOverflowDamage(overflow, source);
            }
        } else {
            health.put(part, newHealth);
        }

        if (getHealth(ModDamagePart.HEAD) <= 0 || getHealth(ModDamagePart.CHEST) <= 0 || getTotalHealthPercent() <= 0) {
            if (!dead && !hasDied && !player.isDeadOrDying()) {
                dead = true;
                hasDied = true;
                player.setHealth(0);
                ModernDamage.LOGGER.info("Calling player.die for {} with source {}", player.getName().getString(), source.getMsgId());
                return true;
            } else {
                ModernDamage.LOGGER.warn("Death already processed for {}, skipping duplicate die call", player.getName().getString());
                return true;
            }
        }

        updateVanillaHealth();
        syncToClient();
        return false;
    }

    private void distributeOverflowDamage(float overflow, DamageSource source) {
        if (dead || hasDied || overflow <= 0) return;

        ModernDamage.LOGGER.debug("distributeOverflowDamage for {} overflow={}", player.getName().getString(), overflow);

        Map<ModDamagePart, Float> currentHealth = new EnumMap<>(ModDamagePart.class);
        Set<ModDamagePart> destroyedParts = new HashSet<>();
        for (ModDamagePart p : ModDamagePart.values()) {
            currentHealth.put(p, getHealth(p));
            if (destroyed.get(p)) destroyedParts.add(p);
        }

        float remaining = overflow;
        while (remaining > 0.001f && !dead && !hasDied) {
            List<ModDamagePart> aliveParts = new ArrayList<>();
            float totalMaxHealth = 0f;
            boolean onlyHeadAlive = true;
            for (ModDamagePart p : ModDamagePart.values()) {
                if (destroyedParts.contains(p)) continue;
                if (currentHealth.get(p) > 0) {
                    aliveParts.add(p);
                    totalMaxHealth += getMaxHealth(p);
                    if (p != ModDamagePart.HEAD) onlyHeadAlive = false;
                }
            }
            if (aliveParts.isEmpty()) break;

            if (onlyHeadAlive && currentHealth.get(ModDamagePart.HEAD) <= 1.0f) {
                break;
            }

            Map<ModDamagePart, Float> partDamage = new EnumMap<>(ModDamagePart.class);
            float totalDamageAssigned = 0f;
            for (ModDamagePart p : aliveParts) {
                float damage = remaining * (getMaxHealth(p) / totalMaxHealth);
                partDamage.put(p, damage);
                totalDamageAssigned += damage;
            }

            if (totalDamageAssigned > remaining + 0.001f) {
                float scale = remaining / totalDamageAssigned;
                for (ModDamagePart p : aliveParts) {
                    partDamage.put(p, partDamage.get(p) * scale);
                }
            }

            boolean hasDestroyed = false;
            float totalOverflow = 0f;
            for (ModDamagePart p : aliveParts) {
                float cur = currentHealth.get(p);
                float damage = partDamage.get(p);
                float newVal = cur - damage;

                if (p == ModDamagePart.HEAD) {
                    if (cur <= 1.0f) continue;
                    if (newVal < 1.0f) {
                        float actualDamage = cur - 1.0f;
                        if (actualDamage < 0) actualDamage = cur;
                        float overflowFromHead = damage - actualDamage;
                        if (overflowFromHead > 0) {
                            totalOverflow += overflowFromHead;
                        }
                        newVal = 1.0f;
                    }
                    currentHealth.put(p, newVal);
                } else {
                    if (newVal <= 0) {
                        totalOverflow += -newVal;
                        currentHealth.put(p, 0f);
                        destroyedParts.add(p);
                        hasDestroyed = true;
                    } else {
                        currentHealth.put(p, newVal);
                    }
                }
            }

            if (hasDestroyed || totalOverflow > 0) {
                remaining = totalOverflow;
            } else {
                remaining = 0;
            }
        }

        for (Map.Entry<ModDamagePart, Float> entry : currentHealth.entrySet()) {
            ModDamagePart p = entry.getKey();
            float newVal = entry.getValue();
            float oldVal = health.get(p);
            if (Math.abs(newVal - oldVal) > 0.001f) {
                health.put(p, newVal);
                if (newVal <= 0) {
                    destroyed.put(p, true);
                } else if (newVal > 0 && destroyed.get(p)) {
                    destroyed.put(p, false);
                }
            }
        }
    }

    @Override
    public boolean damageAll(float amount) {
        if (!tryInit()) return false;
        if (dead || hasDied || amount <= 0) return false;

        lastDamageTick = player.tickCount;

        Map<ModDamagePart, Float> newHealth = new EnumMap<>(ModDamagePart.class);
        for (ModDamagePart part : ModDamagePart.values()) {
            newHealth.put(part, getHealth(part));
        }

        float remaining = amount;
        Set<ModDamagePart> destroyedParts = new HashSet<>();

        while (remaining > 0.001f) {
            List<ModDamagePart> aliveParts = new ArrayList<>();
            float totalMax = 0;
            for (ModDamagePart part : ModDamagePart.values()) {
                if (!destroyedParts.contains(part) && newHealth.get(part) > 0) {
                    aliveParts.add(part);
                    totalMax += getMaxHealth(part);
                }
            }
            if (aliveParts.isEmpty()) break;

            Map<ModDamagePart, Float> partDamage = new EnumMap<>(ModDamagePart.class);
            float totalDamageAssigned = 0;
            for (ModDamagePart part : aliveParts) {
                float damage = remaining * (getMaxHealth(part) / totalMax);
                partDamage.put(part, damage);
                totalDamageAssigned += damage;
            }
            if (totalDamageAssigned > remaining + 0.001f) {
                float scale = remaining / totalDamageAssigned;
                for (ModDamagePart part : aliveParts) {
                    partDamage.put(part, partDamage.get(part) * scale);
                }
            }

            boolean hasDestroyed = false;
            float totalOverflow = 0;
            for (ModDamagePart part : aliveParts) {
                float cur = newHealth.get(part);
                float newVal = cur - partDamage.get(part);
                if (newVal <= 0) {
                    totalOverflow += -newVal;
                    newHealth.put(part, 0f);
                    destroyedParts.add(part);
                    hasDestroyed = true;
                } else {
                    newHealth.put(part, newVal);
                }
            }

            if (hasDestroyed) {
                remaining = totalOverflow;
            } else {
                remaining = 0;
            }
        }

        for (ModDamagePart part : destroyedParts) {
            destroyed.put(part, true);
        }
        for (ModDamagePart part : ModDamagePart.values()) {
            health.put(part, newHealth.get(part));
            if (health.get(part) <= 0) destroyed.put(part, true);
        }

        if (getHealth(ModDamagePart.HEAD) <= 0 || getHealth(ModDamagePart.CHEST) <= 0 || getTotalHealthPercent() <= 0) {
            if (!dead && !hasDied && !player.isDeadOrDying()) {
                dead = true;
                hasDied = true;
                player.setHealth(0);
                return true;
            }
        }

        updateVanillaHealth();
        syncToClient();
        return false;
    }

    @Override
    public void healAll(float amount) {
        if (!tryInit()) return;
        if (dead || amount <= 0) return;

        lastDamageTick = player.tickCount;

        boolean preventLeftArm = player.hasEffect(ModEffects.LEFT_ARM_TRAUMA.get());
        boolean preventRightArm = player.hasEffect(ModEffects.RIGHT_ARM_TRAUMA.get());
        boolean preventLeftLeg = player.hasEffect(ModEffects.LEFT_LEG_TRAUMA.get());
        boolean preventRightLeg = player.hasEffect(ModEffects.RIGHT_LEG_TRAUMA.get());
        boolean preventStomach = player.hasEffect(ModEffects.STOMACH_TRAUMA.get());

        List<ModDamagePart> healableParts = new ArrayList<>();
        float totalHealableMaxHealth = 0f;
        for (ModDamagePart part : ModDamagePart.values()) {
            if (part == ModDamagePart.HEAD || part == ModDamagePart.CHEST) {
                if (getHealth(part) < getMaxHealth(part)) {
                    healableParts.add(part);
                    totalHealableMaxHealth += getMaxHealth(part);
                }
                continue;
            }
            boolean blocked = false;
            switch (part) {
                case LEFT_ARM:
                    blocked = preventLeftArm;
                    break;
                case RIGHT_ARM:
                    blocked = preventRightArm;
                    break;
                case LEFT_LEG:
                    blocked = preventLeftLeg;
                    break;
                case RIGHT_LEG:
                    blocked = preventRightLeg;
                    break;
                case STOMACH:
                    blocked = preventStomach;
                    break;
                default:
                    break;
            }
            if (!blocked && getHealth(part) < getMaxHealth(part)) {
                healableParts.add(part);
                totalHealableMaxHealth += getMaxHealth(part);
            }
        }

        if (healableParts.isEmpty()) {
            return;
        }

        Map<ModDamagePart, Float> partHeal = new EnumMap<>(ModDamagePart.class);
        for (ModDamagePart part : healableParts) {
            float heal = amount * (getMaxHealth(part) / totalHealableMaxHealth);
            partHeal.put(part, heal);
        }

        float remaining = amount;
        boolean changed = true;
        while (changed && remaining > 0.001f) {
            changed = false;
            float totalNeeded = 0f;
            List<ModDamagePart> needyParts = new ArrayList<>();
            for (ModDamagePart part : healableParts) {
                float current = getHealth(part);
                float max = getMaxHealth(part);
                if (current < max) {
                    needyParts.add(part);
                    totalNeeded += (max - current);
                }
            }
            if (needyParts.isEmpty()) break;
            if (remaining >= totalNeeded) {
                for (ModDamagePart part : needyParts) {
                    float max = getMaxHealth(part);
                    health.put(part, max);
                    destroyed.put(part, false);
                }
                remaining -= totalNeeded;
                break;
            } else {
                float totalHealAssigned = 0f;
                for (ModDamagePart part : needyParts) {
                    float needed = getMaxHealth(part) - getHealth(part);
                    float heal = remaining * (needed / totalNeeded);
                    partHeal.put(part, heal);
                    totalHealAssigned += heal;
                }
                if (totalHealAssigned > remaining + 0.001f) {
                    float scale = remaining / totalHealAssigned;
                    for (ModDamagePart part : needyParts) {
                        partHeal.put(part, partHeal.get(part) * scale);
                    }
                }
                for (ModDamagePart part : needyParts) {
                    float newVal = getHealth(part) + partHeal.get(part);
                    if (newVal >= getMaxHealth(part)) {
                        newVal = getMaxHealth(part);
                        changed = false;
                    } else {
                        changed = true;
                    }
                    health.put(part, newVal);
                    if (newVal <= 0) destroyed.put(part, true);
                    else destroyed.put(part, false);
                }
                remaining = 0;
            }
        }

        updateVanillaHealth();
        syncToClient();
    }

    @Override
    public int getLastDamageTick() {
        return lastDamageTick;
    }

    @Override
    public void tick() {
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (!initialized) return tag;
        for (ModDamagePart part : ModDamagePart.values()) {
            tag.putFloat(part.name(), getHealth(part));
        }
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (player.getAttributes() == null) return;
        for (ModDamagePart part : ModDamagePart.values()) {
            if (tag.contains(part.name())) {
                float val = tag.getFloat(part.name());
                health.put(part, val);
                destroyed.put(part, val <= 0);
            } else {
                float max = getMaxHealthRaw(part);
                health.put(part, max);
                destroyed.put(part, false);
            }
        }
        initialized = true;
        dead = false;
        hasDied = false;
        updateVanillaHealth();
    }
}