package com.moderndamage.control.api;

import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;

/**
 * 精细化子部位枚举（仅用于硬核模式精确命中判定和护甲配置）
 * 每个子部位对应一个父部位（ModDamagePart）
 */
public enum ModDamageSubPart {
    // 头部 (父部位 HEAD)
    HEAD_TOP(ModDamagePart.HEAD, "head_top"),
    HEAD_FACE(ModDamagePart.HEAD, "head_face"),
    HEAD_NECK(ModDamagePart.HEAD, "head_neck"),

    // 胸部 (父部位 CHEST)
    CHEST_FRONT(ModDamagePart.CHEST, "chest_front"),
    CHEST_BACK(ModDamagePart.CHEST, "chest_back"),

    // 胃部 (父部位 STOMACH)
    STOMACH_FRONT(ModDamagePart.STOMACH, "stomach_front"),
    STOMACH_BACK(ModDamagePart.STOMACH, "stomach_back"),

    // 左臂 (父部位 LEFT_ARM)
    LEFT_SHOULDER(ModDamagePart.LEFT_ARM, "left_shoulder"),
    LEFT_FOREARM(ModDamagePart.LEFT_ARM, "left_forearm"),

    // 右臂 (父部位 RIGHT_ARM)
    RIGHT_SHOULDER(ModDamagePart.RIGHT_ARM, "right_shoulder"),
    RIGHT_FOREARM(ModDamagePart.RIGHT_ARM, "right_forearm"),

    // 左腿 (父部位 LEFT_LEG)
    LEFT_THIGH(ModDamagePart.LEFT_LEG, "left_thigh"),
    LEFT_CALF(ModDamagePart.LEFT_LEG, "left_calf"),
    LEFT_FOOT(ModDamagePart.LEFT_LEG, "left_foot"),

    // 右腿 (父部位 RIGHT_LEG)
    RIGHT_THIGH(ModDamagePart.RIGHT_LEG, "right_thigh"),
    RIGHT_CALF(ModDamagePart.RIGHT_LEG, "right_calf"),
    RIGHT_FOOT(ModDamagePart.RIGHT_LEG, "right_foot");

    private final ModDamagePart parent;
    private final String subKey;   // 用于 JSON 配置的键名

    private static final Map<String, ModDamageSubPart> BY_SUBKEY = new HashMap<>();
    static {
        for (ModDamageSubPart sub : values()) {
            BY_SUBKEY.put(sub.subKey, sub);
        }
    }

    ModDamageSubPart(ModDamagePart parent, String subKey) {
        this.parent = parent;
        this.subKey = subKey;
    }

    public ModDamagePart getParent() {
        return parent;
    }

    public String getSubKey() {
        return subKey;
    }

    public static ModDamageSubPart bySubKey(String key) {
        return BY_SUBKEY.get(key);
    }
}