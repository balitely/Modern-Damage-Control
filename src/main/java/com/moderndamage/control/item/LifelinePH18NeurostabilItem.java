package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class LifelinePH18NeurostabilItem extends LifelineInjectorItem {
    public LifelinePH18NeurostabilItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        player.addEffect(new MobEffectInstance(ModEffects.PH18_NEUROSTABIL.get(), 3600, 0));
    }
}