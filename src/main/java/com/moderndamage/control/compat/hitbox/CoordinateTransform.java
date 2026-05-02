package com.moderndamage.control.compat.hitbox;

import com.tacz.guns.api.entity.IGunOperator;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class CoordinateTransform {

    public static Vec3 worldToLocal(Vec3 worldHit, Player player) {
        Vec3 relative = worldHit.subtract(player.position());
        float yaw = -player.getYRot() * Mth.DEG_TO_RAD;
        float cos = Mth.cos(yaw);
        float sin = Mth.sin(yaw);
        double localX = relative.x * cos - relative.z * sin;
        double localZ = relative.x * sin + relative.z * cos;
        if (isCrawling(player)) {
            return rotateCrawl(new Vec3(localX, relative.y, localZ));
        } else {
            return new Vec3(localX, relative.y, localZ);
        }
    }

    private static boolean isCrawling(Player player) {
        IGunOperator operator = IGunOperator.fromLivingEntity(player);
        if (operator == null || operator.getDataHolder() == null) {
            return false;
        }
        return operator.getDataHolder().isCrawling;
    }

    private static Vec3 rotateCrawl(Vec3 local) {
        return new Vec3(local.x, local.z, -local.y);
    }

    public static Vec3 getPartWorldPosition(Player player, EnumPlayerPart part) {
        Vec3 localCenter = BodypartHitbox.getPartCenter(part);
        return localToWorld(localCenter, player);
    }

    public static Vec3 localToWorld(Vec3 local, Player player) {
        Vec3 adjusted;
        if (isCrawling(player)) {
            adjusted = new Vec3(local.x, -local.z, local.y);
        } else {
            adjusted = local;
        }
        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        float cos = Mth.cos(yaw);
        float sin = Mth.sin(yaw);
        double worldX = adjusted.x * cos - adjusted.z * sin;
        double worldZ = adjusted.x * sin + adjusted.z * cos;
        return player.position().add(worldX, adjusted.y, worldZ);
    }
}