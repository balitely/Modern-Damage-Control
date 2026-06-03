package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class LifelineVG1StabilizerItem extends LifelineInjectorItem {
    public LifelineVG1StabilizerItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        player.removeEffect(ModEffects.FATIGUE.get());
        player.removeEffect(ModEffects.PAIN.get());
        player.addEffect(new MobEffectInstance(ModEffects.VG1_BOOST.get(), 2400, 0));
    }
}