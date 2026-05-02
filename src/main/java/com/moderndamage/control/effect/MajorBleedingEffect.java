package com.moderndamage.control.effect;

import net.minecraft.world.effect.MobEffectCategory;

public class MajorBleedingEffect extends AbstractBleedingEffect {

    public MajorBleedingEffect(MobEffectCategory category, int color) {
        super(category, color, true); // true = 大出血
    }
}