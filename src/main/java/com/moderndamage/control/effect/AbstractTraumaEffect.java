package com.moderndamage.control.effect;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public abstract class AbstractTraumaEffect extends MobEffect {

    public AbstractTraumaEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;

        ModClothConfig config = ModClothConfig.get();
        if (entity.tickCount % config.traumaDamageIntervalTicks == 0) {
            float damage = config.traumaDamagePerTick;
            entity.hurt(entity.damageSources().magic(), damage);
            if (config.debugMode) {
                ModernDamage.LOGGER.debug("Trauma dealt {} damage to {}", damage, entity.getName().getString());
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}