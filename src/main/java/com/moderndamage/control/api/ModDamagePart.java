package com.moderndamage.control.api;

public enum ModDamagePart {
    HEAD(true),      // 关键部位
    CHEST(true),     // 关键部位
    STOMACH(false),
    LEFT_ARM(false),
    RIGHT_ARM(false),
    LEFT_LEG(false),
    RIGHT_LEG(false);

    private final boolean vital;

    ModDamagePart(boolean vital) {
        this.vital = vital;
    }

    public boolean isVital() {
        return vital;
    }

    public static ModDamagePart fromFirstAidPart(com.moderndamage.control.compat.hitbox.EnumPlayerPart oldPart) {
        if (oldPart == null) return CHEST;
        switch (oldPart) {
            case HEAD: return HEAD;
            case BODY: return CHEST; // 后续通过坐标细分
            case LEFT_ARM: return LEFT_ARM;
            case RIGHT_ARM: return RIGHT_ARM;
            case LEFT_LEG: case LEFT_FOOT: return LEFT_LEG;
            case RIGHT_LEG: case RIGHT_FOOT: return RIGHT_LEG;
            default: return CHEST;
        }
    }

    // 转换为字符串键（用于配置）
    public String getConfigKey() {
        return name().toLowerCase();
    }
}
