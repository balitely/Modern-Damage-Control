package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class LifelineVG4EpinephrineItem extends LifelineInjectorItem {
    public LifelineVG4EpinephrineItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        player.addEffect(new MobEffectInstance(ModEffects.EPINEPHRINE_BOOST.get(), 1200, 0));
    }
}