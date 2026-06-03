package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class LifelinePH6VitalisItem extends LifelineInjectorItem {
    public LifelinePH6VitalisItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        player.addEffect(new MobEffectInstance(ModEffects.PH6_REGENERATION.get(), 6000, 0));
        player.addEffect(new MobEffectInstance(ModEffects.PAIN_SUPPRESSION.get(), 6000, 0));
    }
}