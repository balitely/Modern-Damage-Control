package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class LifelineAG2TranexamicAcidItem extends LifelineInjectorItem {
    public LifelineAG2TranexamicAcidItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        player.removeEffect(ModEffects.MINOR_BLEEDING.get());
        player.removeEffect(ModEffects.MAJOR_BLEEDING.get());
        player.addEffect(new MobEffectInstance(ModEffects.COAGULATION_BOOST.get(), 1200, 0));
    }
}