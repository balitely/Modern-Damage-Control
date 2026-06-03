package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class LifelineAG3MorphineItem extends LifelineInjectorItem {
    public LifelineAG3MorphineItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        player.addEffect(new MobEffectInstance(ModEffects.PAIN_SUPPRESSION.get(), 9600, 0)); // 480秒
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 9)); // 恶心副作用保留
    }
}