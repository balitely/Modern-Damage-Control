package com.moderndamage.control.compat.hitbox;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.api.ModDamageSubPart;
import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class SubBodypartHitbox {
    private static final List<Entry> SUB_PART_BOXES = new ArrayList<>();

    static {
        // ========== 头部 (Y: 1.35 ~ 1.8) ==========
        // 颈部 (下段)
        add(ModDamageSubPart.HEAD_NECK,
                -0.4, 1.35, -0.4,
                0.4, 1.5, 0.4);
        // 面部 (前部，Z>0，Y范围从 1.5 开始)
        add(ModDamageSubPart.HEAD_FACE,
                -0.20, 1.5, 0.05,
                0.20, 1.8, 0.30);
        // 头顶 (整个头部剩余部分，Y范围从 1.5 开始，与面部重叠)
        add(ModDamageSubPart.HEAD_TOP,
                -0.4, 1.5, -0.4,
                0.4, 2.0, 0.4);

        // ========== 胸部 (Y: 1.00625 ~ 1.55625) ==========
        add(ModDamageSubPart.CHEST_FRONT,
                -0.28, 1.0, 0.0,
                0.28, 1.5, 0.4);
        add(ModDamageSubPart.CHEST_BACK,
                -0.28, 1.0, -0.4,
                0.28, 1.5, 0.0);

        // ========== 胃部 (Y: 0.50625 ~ 1.00625) ==========
        add(ModDamageSubPart.STOMACH_FRONT,
                -0.28, 0.7, 0.0,
                0.28, 1.0, 0.4);
        add(ModDamageSubPart.STOMACH_BACK,
                -0.28, 0.7, -0.4,
                0.28, 1.0, 0.0);

        // ========== 手臂 (Y: 0.45 ~ 1.35) ==========
        // 左肩 (上部分)
        add(ModDamageSubPart.LEFT_SHOULDER,
                0.28, 0.9, -0.4,
                0.4, 1.35, 0.4);
        // 左前臂 (下部分)
        add(ModDamageSubPart.LEFT_FOREARM,
                0.28, 0.45, -0.4,
                0.4, 0.9, 0.4);
        // 右肩
        add(ModDamageSubPart.RIGHT_SHOULDER,
                -0.4, 0.9, -0.4,
                -0.28, 1.35, 0.4);
        // 右前臂
        add(ModDamageSubPart.RIGHT_FOREARM,
                -0.4, 0.45, -0.4,
                -0.28, 0.9, 0.4);

        // ========== 腿部 (Y: 0 ~ 0.50625) ==========
        // 左大腿
        add(ModDamageSubPart.LEFT_THIGH,
                0.0, 0.3, -0.4,
                0.4, 0.7, 0.4);
        // 左小腿
        add(ModDamageSubPart.LEFT_CALF,
                0.0, 0.1, -0.4,
                0.4, 0.3, 0.4);
        // 左脚
        add(ModDamageSubPart.LEFT_FOOT,
                0.0, 0, -0.4,
                0.4, 0.1, 0.4);
        // 右大腿
        add(ModDamageSubPart.RIGHT_THIGH,
                -0.4, 0.3, -0.4,
                -0.0, 0.7, 0.4);
        // 右小腿
        add(ModDamageSubPart.RIGHT_CALF,
                -0.4, 0.1, -0.4,
                -0.0, 0.3, 0.4);
        // 右脚
        add(ModDamageSubPart.RIGHT_FOOT,
                -0.4, 0, -0.4,
                -0.0, 0.1, 0.4);
    }

    private static void add(ModDamageSubPart sub, double x1, double y1, double z1, double x2, double y2, double z2) {
        SUB_PART_BOXES.add(new Entry(sub, new AABB(x1, y1, z1, x2, y2, z2)));
    }

    public static ModDamageSubPart getSubPart(Vec3 localPoint, boolean isCrawling) {
        boolean debug = ModClothConfig.get().debugMode;
        if (debug) {
            ModernDamage.LOGGER.info("[SubHitbox] Checking point: {} (crawling={})", localPoint, isCrawling);
        }
        for (Entry entry : SUB_PART_BOXES) {
            if (entry.box.contains(localPoint)) {
                if (debug) {
                    ModernDamage.LOGGER.info("[SubHitbox] Matched: {} with box {}", entry.subPart, entry.box);
                }
                return entry.subPart;
            }
        }
        if (debug) {
            ModernDamage.LOGGER.warn("[SubHitbox] No match for point: {}. Available boxes:", localPoint);
            for (Entry entry : SUB_PART_BOXES) {
                ModernDamage.LOGGER.warn("  {}: {}", entry.subPart, entry.box);
            }
        }
        return ModDamageSubPart.CHEST_FRONT;
    }

    private static class Entry {
        final ModDamageSubPart subPart;
        final AABB box;
        Entry(ModDamageSubPart subPart, AABB box) {
            this.subPart = subPart;
            this.box = box;
        }
    }
}