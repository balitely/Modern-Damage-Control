package com.moderndamage.control.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

public class StomachTraumaEffect extends AbstractTraumaEffect {
    private static final Random RANDOM = new Random();
    private long lastPainTriggerTime = 0;

    public StomachTraumaEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (entity.level().isClientSide) return;
        if (!(entity instanceof Player player)) return;

        long currentTick = player.tickCount;
        if (currentTick % 40 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, false, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0, false, true, true));
        }

        if (currentTick - lastPainTriggerTime < 200) return;
        if (RANDOM.nextDouble() < 0.005) {
            lastPainTriggerTime = currentTick;
            int duration = 20 + RANDOM.nextInt(41);
            player.addEffect(new MobEffectInstance(ModEffects.PAIN.get(), duration, 0));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}