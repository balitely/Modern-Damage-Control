package com.moderndamage.control.config;

import java.util.HashMap;
import java.util.Map;

public class EntityHitboxConfig {
    public boolean useArmor = true;

    public float[] headHeight = {0.75f, 1.0f};
    public float[] bodyHeight = {0.3f, 0.75f};
    public float chestRatio = 0.5f;
    public float[] armXRange = {0, 0};
    public float[] legXRange = {0.2f, 0.8f};
    public boolean vitalPartsLethal = true;

    public Map<String, Integer> naturalArmor = new HashMap<>();
    public Map<String, Integer> naturalToughness = new HashMap<>();  // 新增

    public EntityHitboxConfig() {}
}