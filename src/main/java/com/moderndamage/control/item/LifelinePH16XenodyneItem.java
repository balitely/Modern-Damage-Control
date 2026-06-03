package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class LifelinePH16XenodyneItem extends LifelineInjectorItem {
    public LifelinePH16XenodyneItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        player.addEffect(new MobEffectInstance(ModEffects.PH16_REGENERATION.get(), 6000, 0));
        player.addEffect(new MobEffectInstance(ModEffects.COAGULATION_BOOST.get(), 6000, 0));
        player.addEffect(new MobEffectInstance(ModEffects.PH16_HEAL_EFFECT.get(), 6000, 0));
    }
}