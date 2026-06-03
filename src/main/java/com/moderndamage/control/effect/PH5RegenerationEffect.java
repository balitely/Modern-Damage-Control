package com.moderndamage.control.effect;

import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class PH5RegenerationEffect extends MobEffect {
    private int tickCounter = 0;

    public PH5RegenerationEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        tickCounter++;
        if (tickCounter >= 20) {
            tickCounter = 0;
            float heal = (float) ModClothConfig.get().ph5RegenPerSecond;
            if (heal > 0) entity.heal(heal);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}