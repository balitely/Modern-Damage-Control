package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class LifelineVG8ModafinilXItem extends LifelineInjectorItem {
    public LifelineVG8ModafinilXItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        player.addEffect(new MobEffectInstance(ModEffects.MODAFINIL_FOCUS.get(), 7200, 0));
        player.addEffect(new MobEffectInstance(ModEffects.PAIN_SUPPRESSION.get(), 3600, 0));
    }
}