package com.moderndamage.control.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class C1BuffEffect extends MobEffect {
    public C1BuffEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        entity.removeEffect(ModEffects.FATIGUE.get());
        entity.removeEffect(ModEffects.PAIN.get());
        entity.removeEffect(ModEffects.DIZZINESS.get());
        entity.removeEffect(MobEffects.CONFUSION);   // 恶心
        entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        entity.removeEffect(MobEffects.WEAKNESS);
        entity.removeEffect(MobEffects.DIG_SLOWDOWN);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}