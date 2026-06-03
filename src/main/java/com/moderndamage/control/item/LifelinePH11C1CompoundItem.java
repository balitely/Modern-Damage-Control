package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

public class LifelinePH11C1CompoundItem extends LifelineInjectorItem {
    private static final Random RANDOM = new Random();

    public LifelinePH11C1CompoundItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        player.addEffect(new MobEffectInstance(ModEffects.PH11_REGENERATION.get(), 100, 0));
        player.addEffect(new MobEffectInstance(ModEffects.C1_BUFF.get(), 2400, 0));
        int roll = RANDOM.nextInt(100);
        if (roll < 65) {
            player.addEffect(new MobEffectInstance(ModEffects.C1_SIDE_EFFECT.get(), 6000, 0));
        } else if (roll < 80) {
        } else {
            player.hurt(player.damageSources().magic(), 999);
            player.addEffect(new MobEffectInstance(ModEffects.C1_SIDE_EFFECT.get(), 6000, 0));
        }
    }
}