package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class LifelineAG1ThrombinItem extends LifelineInjectorItem {
    public LifelineAG1ThrombinItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        player.addEffect(new MobEffectInstance(ModEffects.COAGULATION_BOOST.get(), 7200, 0));
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0)); // 饥饿30秒
    }
}