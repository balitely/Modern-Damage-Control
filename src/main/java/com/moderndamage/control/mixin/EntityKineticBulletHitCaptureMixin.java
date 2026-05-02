package com.moderndamage.control.mixin;

import com.moderndamage.control.mixininterface.EntityKineticBulletExtension;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.util.TacHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityKineticBullet.class, remap = false)
public class EntityKineticBulletHitCaptureMixin implements EntityKineticBulletExtension {

    @Unique
    private Vec3 moderndamage$lastHitLocation = null;

    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void moderndamage$captureHitLocation(TacHitResult result, Vec3 startVec, Vec3 endVec, CallbackInfo ci) {
        moderndamage$lastHitLocation = result.getLocation();
    }

    @Override
    public Vec3 moderndamage$getLastHitLocation() {
        return moderndamage$lastHitLocation;
    }

    @Override
    public void moderndamage$setLastHitLocation(Vec3 location) {
        moderndamage$lastHitLocation = location;
    }
}