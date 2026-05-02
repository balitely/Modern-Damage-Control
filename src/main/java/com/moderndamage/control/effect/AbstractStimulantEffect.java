package com.moderndamage.control.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public abstract class AbstractStimulantEffect extends MobEffect {
    protected final int fatigueDurationSeconds;

    public AbstractStimulantEffect(MobEffectCategory category, int color, int fatigueDurationSeconds) {
        super(category, color);
        this.fatigueDurationSeconds = fatigueDurationSeconds;
    }

    protected void applySideEffect(LivingEntity entity, int amplifier) {
        if (fatigueDurationSeconds > 0) {
            entity.addEffect(new MobEffectInstance(ModEffects.FATIGUE.get(),
                    fatigueDurationSeconds * 20, 0, false, true, true));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}