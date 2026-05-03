package com.moderndamage.control.effect;

import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class IvFluidEffect extends MobEffect {
    public IvFluidEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        int baseHeal = amplifier + 1;
        float multiplier = ModClothConfig.get().ivFluidHealMultiplier;
        int healAmount = Math.max(1, (int)(baseHeal * multiplier));
        entity.heal(healAmount);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}