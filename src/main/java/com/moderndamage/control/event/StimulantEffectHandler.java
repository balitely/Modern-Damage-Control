package com.moderndamage.control.event;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

public class StimulantEffectHandler {

    private static final Map<RegistryObject<? extends MobEffect>, Integer> SIDE_EFFECT_FATIGUE_MAP = Map.of(
            ModEffects.EPINEPHRINE_BOOST, 120
    );

    @SubscribeEvent
    public void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null) return;
        MobEffect effect = instance.getEffect();

        for (Map.Entry<RegistryObject<? extends MobEffect>, Integer> entry : SIDE_EFFECT_FATIGUE_MAP.entrySet()) {
            RegistryObject<? extends MobEffect> ro = entry.getKey();
            if (ro.isPresent() && ro.get() == effect) {
                LivingEntity entity = event.getEntity();
                if (!entity.level().isClientSide) {
                    int fatigueSeconds = entry.getValue();
                    if (fatigueSeconds > 0) {
                        entity.addEffect(new MobEffectInstance(ModEffects.FATIGUE.get(),
                                fatigueSeconds * 20, 0, false, true, true));
                    }
                }
                break;
            }
        }
    }
}