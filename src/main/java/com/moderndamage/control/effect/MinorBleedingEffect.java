package com.moderndamage.control.effect;

import net.minecraft.world.effect.MobEffectCategory;

public class MinorBleedingEffect extends AbstractBleedingEffect {

    public MinorBleedingEffect(MobEffectCategory category, int color) {
        super(category, color, false); // false = 轻微
    }
}