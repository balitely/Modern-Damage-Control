package com.moderndamage.control.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public abstract class AbstractLegFractureEffect extends AbstractFractureEffect {
    public AbstractLegFractureEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (entity.level().isClientSide) return;
        if (entity instanceof Player player && player.isSprinting()) {
            player.setSprinting(false);
        }
    }
}