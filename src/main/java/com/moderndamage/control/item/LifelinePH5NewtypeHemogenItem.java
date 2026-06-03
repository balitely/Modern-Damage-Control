package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class LifelinePH5NewtypeHemogenItem extends LifelineInjectorItem {
    public LifelinePH5NewtypeHemogenItem() {
        super(new Properties(), 20, 16);
    }

    @Override
    protected void applyEffect(Player player) {
        player.addEffect(new MobEffectInstance(ModEffects.PH5_REGENERATION.get(), 3600, 0));
        player.removeEffect(ModEffects.MINOR_BLEEDING.get());
        player.removeEffect(ModEffects.MAJOR_BLEEDING.get());
        player.addEffect(new MobEffectInstance(ModEffects.COAGULATION_BOOST.get(), 3600, 0));
        player.addEffect(new MobEffectInstance(ModEffects.POISON_RESISTANCE.get(), 3600, 0));
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 2)); // 饥饿 III 60秒
    }
}