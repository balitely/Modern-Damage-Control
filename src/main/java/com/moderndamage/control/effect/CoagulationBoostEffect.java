package com.moderndamage.control.effect;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class CoagulationBoostEffect extends MobEffect {

    public CoagulationBoostEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        if (!(entity instanceof Player player)) return;

        ModClothConfig config = ModClothConfig.get();
        int interval = config.coagulationClearIntervalTicks;
        if (interval <= 0) interval = 200; // 默认10秒

        if (player.tickCount % interval == 0) {
            if (player.hasEffect(ModEffects.MINOR_BLEEDING.get())) {
                player.removeEffect(ModEffects.MINOR_BLEEDING.get());
                if (config.debugMode) {
                    ModernDamage.LOGGER.debug("凝血增强清除了所有轻微出血效果");
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}