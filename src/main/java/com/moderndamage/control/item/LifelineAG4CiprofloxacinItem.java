package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class LifelineAG4CiprofloxacinItem extends LifelineInjectorItem {
    public LifelineAG4CiprofloxacinItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        player.addEffect(new MobEffectInstance(ModEffects.INFECTION_RESISTANCE.get(), 24000, 0)); // 1200秒
    }
}