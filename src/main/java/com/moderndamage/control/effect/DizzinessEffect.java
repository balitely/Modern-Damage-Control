package com.moderndamage.control.effect;

import com.moderndamage.control.ModernDamage;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class DizzinessEffect extends MobEffect {
    public DizzinessEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player player)) return;

        if (player.level().isClientSide) {
            if (ModernDamage.enhancedVisuals != null) {
                boolean hasDizzy = player.hasEffect(ModEffects.DIZZINESS.get());
                boolean hasResistance = player.hasEffect(ModEffects.DIZZINESS_RESISTANCE.get());
                ModernDamage.enhancedVisuals.updateDizzyVisual(player, hasDizzy, hasResistance);
            }
            return;
        }

        if (player.hasEffect(ModEffects.DIZZINESS_RESISTANCE.get())) {
            return;
        }
        if (player.isSprinting()) {
            player.setSprinting(false);
        }
        if (player.tickCount % 60 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, false, true, true));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}