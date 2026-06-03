package com.moderndamage.control.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class ClientLegStaminaCache {
    private static final Map<UUID, Float> STAMINA_MAP = new ConcurrentHashMap<>();

    public static void update(UUID playerId, float stamina) {
        STAMINA_MAP.put(playerId, stamina);
    }

    public static float getStamina(UUID playerId) {
        return STAMINA_MAP.getOrDefault(playerId, 0f);
    }

    public static void remove(UUID playerId) {
        STAMINA_MAP.remove(playerId);
    }
}