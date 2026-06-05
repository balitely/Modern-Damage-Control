package com.moderndamage.control.compat.hitbox;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BodypartHitbox {

    public enum BodypartSlot {
        HEAD(EnumPlayerPart.HEAD, EquipmentSlot.HEAD),
        BODY(EnumPlayerPart.BODY, EquipmentSlot.CHEST),
        LEFT_ARM(EnumPlayerPart.LEFT_ARM, EquipmentSlot.CHEST),
        RIGHT_ARM(EnumPlayerPart.RIGHT_ARM, EquipmentSlot.CHEST),
        LEFT_LEG(EnumPlayerPart.LEFT_LEG, EquipmentSlot.LEGS),
        RIGHT_LEG(EnumPlayerPart.RIGHT_LEG, EquipmentSlot.LEGS),
        LEFT_FOOT(EnumPlayerPart.LEFT_FOOT, EquipmentSlot.FEET),
        RIGHT_FOOT(EnumPlayerPart.RIGHT_FOOT, EquipmentSlot.FEET);

        private final EnumPlayerPart part;
        private final EquipmentSlot armorSlot;

        BodypartSlot(EnumPlayerPart part, EquipmentSlot armorSlot) {
            this.part = part;
            this.armorSlot = armorSlot;
        }

        public EnumPlayerPart getPart() {
            return part;
        }

        public EquipmentSlot getArmorSlot() {
            return armorSlot;
        }

        public static BodypartSlot fromPart(EnumPlayerPart part) {
            for (BodypartSlot slot : values()) {
                if (slot.part == part) {
                    return slot;
                }
            }
            return null;
        }
    }

    private static final AABB HEAD_BOX = new AABB(-0.2, 1.5, -0.2, 0.2, 1.8, 0.2);
    private static final AABB BODY_BOX = new AABB(-0.3, 0.7, -0.15, 0.3, 1.5, 0.15);
    private static final AABB LEFT_ARM_BOX = new AABB(0.3, 0.7, -0.15, 0.4, 1.5, 0.15);
    private static final AABB RIGHT_ARM_BOX = new AABB(-0.4, 0.7, -0.15, -0.3, 1.5, 0.15);
    private static final AABB LEFT_LEG_BOX = new AABB(0.0, 0.15, -0.15, 0.2, 0.7, 0.15);
    private static final AABB RIGHT_LEG_BOX = new AABB(-0.2, 0.15, -0.15, 0.0, 0.7, 0.15);
    private static final AABB LEFT_FOOT_BOX = new AABB(0.0, 0.0, -0.2, 0.2, 0.15, 0.2);
    private static final AABB RIGHT_FOOT_BOX = new AABB(-0.2, 0.0, -0.2, 0.0, 0.15, 0.2);

    private static final BodypartSlot[] CHECK_ORDER = {
            BodypartSlot.HEAD,
            BodypartSlot.LEFT_ARM,
            BodypartSlot.RIGHT_ARM,
            BodypartSlot.LEFT_LEG,
            BodypartSlot.RIGHT_LEG,
            BodypartSlot.LEFT_FOOT,
            BodypartSlot.RIGHT_FOOT,
            BodypartSlot.BODY
    };

    private static AABB getHitbox(BodypartSlot slot) {
        switch (slot) {
            case HEAD: return HEAD_BOX;
            case BODY: return BODY_BOX;
            case LEFT_ARM: return LEFT_ARM_BOX;
            case RIGHT_ARM: return RIGHT_ARM_BOX;
            case LEFT_LEG: return LEFT_LEG_BOX;
            case RIGHT_LEG: return RIGHT_LEG_BOX;
            case LEFT_FOOT: return LEFT_FOOT_BOX;
            case RIGHT_FOOT: return RIGHT_FOOT_BOX;
            default: return BODY_BOX;
        }
    }

    public static EnumPlayerPart getHitPart(Vec3 localPoint) {
        for (BodypartSlot slot : CHECK_ORDER) {
            AABB box = getHitbox(slot);
            if (box.contains(localPoint)) {
                EnumPlayerPart part = slot.getPart();
                if (part == EnumPlayerPart.LEFT_FOOT) return EnumPlayerPart.LEFT_LEG;
                if (part == EnumPlayerPart.RIGHT_FOOT) return EnumPlayerPart.RIGHT_LEG;
                return part;
            }
        }
        return null;
    }

    public static Vec3 getPartCenter(EnumPlayerPart part) {
        BodypartSlot slot = BodypartSlot.fromPart(part);
        if (slot == null) {
            return Vec3.ZERO;
        }
        AABB box = getHitbox(slot);
        return box.getCenter();
    }

    public static EnumPlayerPart getClosestPart(Vec3 localPoint) {
        EnumPlayerPart closest = EnumPlayerPart.BODY;
        double minDistance = Double.MAX_VALUE;

        for (BodypartSlot slot : BodypartSlot.values()) {
            EnumPlayerPart part = slot.getPart();
            if (part == EnumPlayerPart.LEFT_FOOT) part = EnumPlayerPart.LEFT_LEG;
            if (part == EnumPlayerPart.RIGHT_FOOT) part = EnumPlayerPart.RIGHT_LEG;
            Vec3 center = getPartCenter(part);
            double distance = center.distanceToSqr(localPoint);
            if (distance < minDistance) {
                minDistance = distance;
                closest = part;
            }
        }
        return closest;
    }
}