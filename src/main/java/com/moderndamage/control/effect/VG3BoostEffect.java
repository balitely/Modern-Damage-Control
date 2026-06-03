package com.moderndamage.control.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class VG3BoostEffect extends MobEffect {
    public VG3BoostEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {}
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}