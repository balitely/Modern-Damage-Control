package com.moderndamage.control.effect;

import com.moderndamage.control.ModernDamage;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class PainEffect extends MobEffect {
    public PainEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player player)) return;

        if (player.level().isClientSide) {
            if (ModernDamage.enhancedVisuals != null) {
                boolean hasPain = player.hasEffect(ModEffects.PAIN.get());
                boolean hasSuppression = player.hasEffect(ModEffects.PAIN_SUPPRESSION.get());
                ModernDamage.enhancedVisuals.updatePainVisual(player, hasPain, hasSuppression);
            }
            return;
        }

        if (player.hasEffect(ModEffects.PAIN_SUPPRESSION.get())) {
            if (player.hasEffect(MobEffects.DARKNESS)) player.removeEffect(MobEffects.DARKNESS);
            return;
        }
        if (player.tickCount % 20 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 1, false, true, true));
        }
        if (player.isSprinting()) {
            player.setSprinting(false);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}