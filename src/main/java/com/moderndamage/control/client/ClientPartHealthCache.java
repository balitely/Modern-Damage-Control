package com.moderndamage.control.client;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.capability.parthealth.IPartHealth;
import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class ClientPartHealthCache implements IPartHealth {
    private static final Map<UUID, ClientPartHealthCache> CACHE = new ConcurrentHashMap<>();

    private final Map<ModDamagePart, Float> health = new EnumMap<>(ModDamagePart.class);
    private final Map<ModDamagePart, Float> maxHealth = new EnumMap<>(ModDamagePart.class);
    private int flashRemainingTicks = 0;
    private int lastFlashTick = -100;

    public static void initDefault(UUID playerId) {
        ClientPartHealthCache cache = CACHE.computeIfAbsent(playerId, id -> new ClientPartHealthCache());
        for (ModDamagePart part : ModDamagePart.values()) {
            float ratio = ModClothConfig.getPlayerPartRatio(part);
            float defaultMax = 20.0f * ratio;
            cache.maxHealth.put(part, defaultMax);
            cache.health.put(part, defaultMax);
        }
    }

    public static Optional<ClientPartHealthCache> get(UUID playerId) {
        return Optional.ofNullable(CACHE.get(playerId));
    }

    public static void update(UUID playerId, Map<ModDamagePart, Float> newHealth, Map<ModDamagePart, Float> newMaxHealth) {
        ClientPartHealthCache cache = CACHE.computeIfAbsent(playerId, id -> new ClientPartHealthCache());

        boolean changed = false;
        for (Map.Entry<ModDamagePart, Float> entry : newHealth.entrySet()) {
            Float old = cache.health.get(entry.getKey());
            if (old == null || Math.abs(entry.getValue() - old) > 0.001f) {
                changed = true;
                break;
            }
        }

        cache.health.clear();
        cache.health.putAll(newHealth);
        cache.maxHealth.clear();
        cache.maxHealth.putAll(newMaxHealth);

        if (changed) {
            cache.flashRemainingTicks = 2;
            if (Minecraft.getInstance().player != null) {
                cache.lastFlashTick = Minecraft.getInstance().player.tickCount;
            }
        }
    }

    public void tick() {
        if (flashRemainingTicks > 0) {
            flashRemainingTicks--;
        }
    }

    public boolean isFlashing() {
        return flashRemainingTicks > 0;
    }

    @Override
    public float getHealth(ModDamagePart part) {
        return health.getOrDefault(part, 0f);
    }

    @Override
    public float getMaxHealth(ModDamagePart part) {
        return maxHealth.getOrDefault(part, 20f);
    }

    @Override
    public int getLastDamageTick() {
        return isFlashing() ? lastFlashTick : -1000;
    }

    @Override
    public float getTotalHealthPercent() {
        float total = 0, maxTotal = 0;
        for (ModDamagePart part : ModDamagePart.values()) {
            total += getHealth(part);
            maxTotal += getMaxHealth(part);
        }
        return maxTotal == 0 ? 0 : total / maxTotal;
    }

    @Override
    public boolean isPartDestroyed(ModDamagePart part) {
        return getHealth(part) <= 0;
    }

    @Override
    public boolean damagePart(ModDamagePart part, float amount) {
        throw new UnsupportedOperationException("Client-side cache does not support damagePart");
    }

    @Override
    public boolean damageAll(float amount) {
        throw new UnsupportedOperationException("Client-side cache does not support damageAll");
    }

    @Override
    public void healAll(float amount) {
        throw new UnsupportedOperationException("Client-side cache does not support healAll");
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Client-side cache does not support reset");
    }
}