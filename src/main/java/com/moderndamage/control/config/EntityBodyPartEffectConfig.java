package com.moderndamage.control.config;

import java.util.ArrayList;
import java.util.List;

public class EntityBodyPartEffectConfig {
    public boolean enabled = true;
    public float thresholdPercent = 0.1f;
    public List<EffectEntry> effects = new ArrayList<>();
    public List<EffectEntry> destroy = new ArrayList<>();

    public EntityBodyPartEffectConfig() {}
}