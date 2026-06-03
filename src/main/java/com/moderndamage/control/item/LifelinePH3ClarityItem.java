package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class LifelinePH3ClarityItem extends LifelineInjectorItem {
    public LifelinePH3ClarityItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        player.removeEffect(ModEffects.DIZZINESS.get());
        player.removeEffect(MobEffects.CONFUSION);
        player.addEffect(new MobEffectInstance(ModEffects.DIZZINESS_RESISTANCE.get(), 5600, 0));
        player.addEffect(new MobEffectInstance(ModEffects.PAIN_SUPPRESSION.get(), 5600, 0));
    }
}