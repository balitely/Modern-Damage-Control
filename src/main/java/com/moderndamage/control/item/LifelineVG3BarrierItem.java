package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class LifelineVG3BarrierItem extends LifelineInjectorItem {
    public LifelineVG3BarrierItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        player.addEffect(new MobEffectInstance(ModEffects.VG3_BOOST.get(), 4800, 0));
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0)); // 恶心5秒
    }
}