package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class LifelineVG2FamexinItem extends LifelineInjectorItem {
    public LifelineVG2FamexinItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        player.addEffect(new MobEffectInstance(ModEffects.FAMEXIN_RESISTANCE.get(), 6000, 0));
    }
}