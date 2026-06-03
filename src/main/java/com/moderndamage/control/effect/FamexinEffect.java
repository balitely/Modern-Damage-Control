package com.moderndamage.control.effect;

import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class FamexinEffect extends MobEffect {
    private int foodTimer = 0;

    public FamexinEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        // 清除恶心和饥饿
        if (entity.hasEffect(MobEffects.CONFUSION)) entity.removeEffect(MobEffects.CONFUSION);
        if (entity.hasEffect(MobEffects.HUNGER)) entity.removeEffect(MobEffects.HUNGER);

        // 饱食度恢复
        if (entity instanceof Player player) {
            ModClothConfig config = ModClothConfig.get();
            int interval = config.famexinFoodRestoreInterval;
            int amount = config.famexinFoodRestoreAmount;
            if (interval > 0 && amount > 0) {
                foodTimer++;
                if (foodTimer >= interval) {
                    foodTimer = 0;
                    player.getFoodData().eat(amount, 0.6f);
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}