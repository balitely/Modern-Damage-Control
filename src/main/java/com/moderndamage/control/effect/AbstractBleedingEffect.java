package com.moderndamage.control.effect;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public abstract class AbstractBleedingEffect extends MobEffect {

    protected final boolean isMajor;

    public AbstractBleedingEffect(MobEffectCategory category, int color, boolean isMajor) {
        super(category, color);
        this.isMajor = isMajor;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;

        ModClothConfig config = ModClothConfig.get();
        int interval;
        float damagePerLevel;

        if (isMajor) {
            interval = config.majorBleedingIntervalTicks;
            damagePerLevel = config.majorBleedingDamagePerLevel;
        } else {
            interval = config.minorBleedingIntervalTicks;
            damagePerLevel = config.minorBleedingDamagePerLevel;
        }

        int level = amplifier + 1;
        if (entity.tickCount % interval == 0) {
            float totalDamage = level * damagePerLevel;
            entity.hurt(entity.damageSources().magic(), totalDamage);
            if (config.debugMode) {
                ModernDamage.LOGGER.debug("{} bleeding level {} dealt {} damage to {}",
                        isMajor ? "Major" : "Minor", level, totalDamage, entity.getName().getString());
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}