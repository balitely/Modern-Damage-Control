package com.moderndamage.control.capability.parthealth;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.config.EffectEntry;
import com.moderndamage.control.config.ModClothConfig;
import com.moderndamage.control.network.Networking;
import com.moderndamage.control.network.SyncPartHealthPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class PlayerPartHealth implements IPartHealth {
    private final Player player;
    private final Map<ModDamagePart, Float> health = new EnumMap<>(ModDamagePart.class);
    private final Map<ModDamagePart, Boolean> destroyed = new EnumMap<>(ModDamagePart.class);
    private boolean dead = false;
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
        for (ModDamagePart part : ModDamagePart.values()) {
            float max = getMaxHealthRaw(part);
            health.put(part, max);
            destroyed.put(part, false);
        }
        initialized = true;
        lastDamageTick = 0;
        updateVanillaHealth();
        syncToClient();
    }

    private void updateVanillaHealth() {
        float newTotal = getTotalHealthPercent() * player.getMaxHealth();
        player.setHealth(Math.max(0, Math.min(player.getMaxHealth(), newTotal)));
    }

    private void syncToClient() {
        if (player.level().isClientSide) return;
        if (player.tickCount - lastSyncTick < 2) return;
        lastSyncTick = player.tickCount;
        if (player instanceof ServerPlayer serverPlayer) {
            Networking.sendToPlayer(new SyncPartHealthPacket(player.getUUID(), health, getMaxHealthMap()), serverPlayer);
        }
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

    // ========== 单部位伤害 ==========
    @Override
    public boolean damagePart(ModDamagePart part, float amount) {
        if (!tryInit()) return false;
        if (dead || amount <= 0) return false;

        lastDamageTick = player.tickCount;
        float current = getHealth(part);
        float newHealth = current - amount;
        if (newHealth <= 0) {
            // 如果之前部位还活着，现在被摧毁，触发摧毁效果
            if (health.get(part) > 0) {
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
            player.setHealth(0);
            syncToClient();
            return true;
        }

        updateVanillaHealth();
        syncToClient();
        return false;
    }

    @Override
    public boolean damageAll(float amount) {
        if (!tryInit()) return false;
        if (dead || amount <= 0) return false;

        lastDamageTick = player.tickCount;

        // 复制当前血量
        Map<ModDamagePart, Float> newHealth = new EnumMap<>(ModDamagePart.class);
        for (ModDamagePart part : ModDamagePart.values()) {
            newHealth.put(part, getHealth(part));
        }

        float remaining = amount;
        Set<ModDamagePart> destroyedParts = new HashSet<>();

        while (remaining > 0.001f) {
            // 获取当前未摧毁且血量大于0的部位
            List<ModDamagePart> aliveParts = new ArrayList<>();
            float totalMax = 0;
            for (ModDamagePart part : ModDamagePart.values()) {
                if (!destroyedParts.contains(part) && newHealth.get(part) > 0) {
                    aliveParts.add(part);
                    totalMax += getMaxHealth(part);
                }
            }
            if (aliveParts.isEmpty()) break;

            // 计算每个部位应承担的伤害（基于最大血量比例）
            Map<ModDamagePart, Float> partDamage = new EnumMap<>(ModDamagePart.class);
            float totalDamageAssigned = 0;
            for (ModDamagePart part : aliveParts) {
                float damage = remaining * (getMaxHealth(part) / totalMax);
                partDamage.put(part, damage);
                totalDamageAssigned += damage;
            }
            // 浮点误差修正
            if (totalDamageAssigned > remaining + 0.001f) {
                float scale = remaining / totalDamageAssigned;
                for (ModDamagePart part : aliveParts) {
                    partDamage.put(part, partDamage.get(part) * scale);
                }
            }

            // 应用伤害，记录溢出和摧毁
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
                remaining = totalOverflow; // 溢出伤害继续分配
            } else {
                remaining = 0;
            }
        }

        // 应用摧毁状态和新血量的最终值
        for (ModDamagePart part : destroyedParts) {
            destroyed.put(part, true);
        }
        for (ModDamagePart part : ModDamagePart.values()) {
            health.put(part, newHealth.get(part));
            if (health.get(part) <= 0) destroyed.put(part, true);
        }

        // 死亡判定
        if (getHealth(ModDamagePart.HEAD) <= 0 || getHealth(ModDamagePart.CHEST) <= 0 || getTotalHealthPercent() <= 0) {
            dead = true;
            player.setHealth(0);
            syncToClient();
            return true;
        }

        updateVanillaHealth();
        syncToClient();
        return false;
    }

    // 全身比例治疗
    @Override
    public void healAll(float amount) {
        if (!tryInit()) return;
        if (dead || amount <= 0) return;

        lastDamageTick = player.tickCount;

        Map<ModDamagePart, Float> newHealth = new EnumMap<>(ModDamagePart.class);
        for (ModDamagePart part : ModDamagePart.values()) {
            newHealth.put(part, getHealth(part));
        }

        float remaining = amount;
        Set<ModDamagePart> fullParts = new HashSet<>();

        while (remaining > 0.001f) {
            // 获取所有未满血且未被标记为已满的部位
            List<ModDamagePart> needyParts = new ArrayList<>();
            float totalMax = 0;
            for (ModDamagePart part : ModDamagePart.values()) {
                if (!fullParts.contains(part) && newHealth.get(part) < getMaxHealth(part)) {
                    needyParts.add(part);
                    totalMax += getMaxHealth(part);
                }
            }
            if (needyParts.isEmpty()) break;

            // 按最大血量比例计算每个部位应获得的治疗
            Map<ModDamagePart, Float> partHeal = new EnumMap<>(ModDamagePart.class);
            float totalHealAssigned = 0;
            for (ModDamagePart part : needyParts) {
                float heal = remaining * (getMaxHealth(part) / totalMax);
                partHeal.put(part, heal);
                totalHealAssigned += heal;
            }
            if (totalHealAssigned > remaining + 0.001f) {
                float scale = remaining / totalHealAssigned;
                for (ModDamagePart part : needyParts) {
                    partHeal.put(part, partHeal.get(part) * scale);
                }
            }

            boolean anyFull = false;
            float totalOverflow = 0;
            for (ModDamagePart part : needyParts) {
                float max = getMaxHealth(part);
                float cur = newHealth.get(part);
                float newVal = cur + partHeal.get(part);
                if (newVal >= max) {
                    totalOverflow += newVal - max;
                    newHealth.put(part, max);
                    fullParts.add(part);
                    anyFull = true;
                } else {
                    newHealth.put(part, newVal);
                }
            }

            if (anyFull) {
                remaining = totalOverflow;
            } else {
                remaining = 0;
            }
        }

        // 应用治疗后的新血量
        for (ModDamagePart part : ModDamagePart.values()) {
            float val = newHealth.get(part);
            health.put(part, val);
            if (val <= 0) destroyed.put(part, true);
            else if (val > 0 && destroyed.getOrDefault(part, false)) destroyed.put(part, false);
        }

        updateVanillaHealth();
        syncToClient();
    }

    // ========== 其他接口方法 ==========
    @Override
    public int getLastDamageTick() {
        return lastDamageTick;
    }

    @Override
    public void tick() {
        // 不再强制同步，避免平均化
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
        updateVanillaHealth();
    }
}