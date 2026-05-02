package com.moderndamage.control.config;

import com.moderndamage.control.api.ModDamagePart;

import java.util.HashMap;
import java.util.Map;

public class EntityEffectConfig {
    public boolean enabled = true;
    public Map<String, EntityBodyPartEffectConfig> bodyParts = new HashMap<>();

    public EntityEffectConfig() {}

    public EntityBodyPartEffectConfig getPartConfig(ModDamagePart part) {
        return bodyParts.getOrDefault(part.getConfigKey(), new EntityBodyPartEffectConfig());
    }
}