package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class LifelinePH1AtropineItem extends LifelineInjectorItem {
    public LifelinePH1AtropineItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        player.removeEffect(ModEffects.POISON_RESISTANCE.get());
        player.addEffect(new MobEffectInstance(ModEffects.POISON_RESISTANCE.get(), 6000, 0));
        player.addEffect(new MobEffectInstance(ModEffects.INFECTION_RESISTANCE.get(), 6000, 0));
    }
}