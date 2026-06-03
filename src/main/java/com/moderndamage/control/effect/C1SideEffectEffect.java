package com.moderndamage.control.effect;

import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class C1SideEffectEffect extends MobEffect {
    private int damageTimer = 0;

    public C1SideEffectEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        ModClothConfig config = ModClothConfig.get();
        int interval = config.ph11SideEffectInterval; // 默认100 tick = 5秒
        int damage = config.ph11SideEffectDamage;
        if (interval <= 0 || damage <= 0) return;
        damageTimer++;
        if (damageTimer >= interval) {
            damageTimer = 0;
            entity.hurt(entity.damageSources().magic(), damage);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}