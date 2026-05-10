package com.moderndamage.control.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class EpinephrineBoostEffect extends AbstractStimulantEffect {
    public EpinephrineBoostEffect(MobEffectCategory category, int color) {
        super(category, color, 120);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        if (!(entity instanceof Player player)) return;

        if (player.hasEffect(ModEffects.PAIN.get())) {
            player.removeEffect(ModEffects.PAIN.get());
        }
        if (player.hasEffect(ModEffects.DIZZINESS.get())) {
            player.removeEffect(ModEffects.DIZZINESS.get());
        }
        if (player.hasEffect(ModEffects.FATIGUE.get())) {
            player.removeEffect(ModEffects.FATIGUE.get());
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}