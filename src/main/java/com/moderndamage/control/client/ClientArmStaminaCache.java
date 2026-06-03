package com.moderndamage.control.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class ClientArmStaminaCache {
    private static final Map<UUID, Float> STAMINA_MAP = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> AIMING_MAP = new ConcurrentHashMap<>();

    public static void update(UUID playerId, float stamina, boolean isAiming) {
        STAMINA_MAP.put(playerId, stamina);
        AIMING_MAP.put(playerId, isAiming);
    }

    public static float getStamina(UUID playerId) {
        return STAMINA_MAP.getOrDefault(playerId, 0f);
    }

    public static boolean isAiming(UUID playerId) {
        return AIMING_MAP.getOrDefault(playerId, false);
    }

    public static void remove(UUID playerId) {
        STAMINA_MAP.remove(playerId);
        AIMING_MAP.remove(playerId);
    }
}