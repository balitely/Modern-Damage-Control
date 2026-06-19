package com.moderndamage.control.capability.parthealth;

import com.moderndamage.control.api.ModDamagePart;
import net.minecraft.world.damagesource.DamageSource;

public interface IPartHealth {
    boolean damagePart(ModDamagePart part, float amount, DamageSource source);
    boolean damagePart(ModDamagePart part, float amount);
    boolean damageAll(float amount);
    void healAll(float amount);
    float getHealth(ModDamagePart part);
    float getMaxHealth(ModDamagePart part);
    float getTotalHealthPercent();
    boolean isPartDestroyed(ModDamagePart part);
    void reset();
    int getLastDamageTick();
    void tick();

    void resetDeathState();
}