package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class LifelinePH12C1ExtenderItem extends LifelineInjectorItem {
    public LifelinePH12C1ExtenderItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        MobEffectInstance c1Buff = player.getEffect(ModEffects.C1_BUFF.get());
        if (c1Buff != null) {
            int newDuration = c1Buff.getDuration() + 1800;
            player.removeEffect(ModEffects.C1_BUFF.get());
            player.addEffect(new MobEffectInstance(ModEffects.C1_BUFF.get(), newDuration, 0));
        }
        player.removeEffect(ModEffects.C1_SIDE_EFFECT.get());
    }
}