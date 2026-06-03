package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class LifelineVG6MethylphenidateItem extends LifelineInjectorItem {
    public LifelineVG6MethylphenidateItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        player.addEffect(new MobEffectInstance(ModEffects.VG6_METHYLPHENIDATE.get(), 2400, 0));
        player.addEffect(new MobEffectInstance(ModEffects.PAIN_SUPPRESSION.get(), 2400, 0));
    }
}