package com.moderndamage.control.mixininterface;

import net.minecraft.world.phys.Vec3;

public interface EntityKineticBulletExtension {
    Vec3 moderndamage$getLastHitLocation();
    void moderndamage$setLastHitLocation(Vec3 location);
}