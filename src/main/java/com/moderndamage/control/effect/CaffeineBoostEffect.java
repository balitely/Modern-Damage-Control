package com.moderndamage.control.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class CaffeineBoostEffect extends AbstractStimulantEffect {
    public CaffeineBoostEffect(MobEffectCategory category, int color) {
        super(category, color, 0);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}