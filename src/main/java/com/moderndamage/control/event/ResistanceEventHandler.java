package com.moderndamage.control.event;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.effect.ModEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class ResistanceEventHandler {

    // 感染效果的注册名（仅当 Spore 模组加载时存在）
    private static final ResourceLocation MYCELIUM_EFFECT = new ResourceLocation("spore", "mycelium_ef");
    private static boolean isSporeLoaded = false;

    public ResistanceEventHandler() {
        isSporeLoaded = ModList.get().isLoaded("spore");
    }

    @SubscribeEvent
    public void onEffectApplicable(MobEffectEvent.Applicable event) {
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance == null) return;

        MobEffect effect = effectInstance.getEffect();
        LivingEntity entity = event.getEntity();

        if (entity.hasEffect(ModEffects.EPINEPHRINE_BOOST.get())) {
            if (effect == ModEffects.PAIN.get() || effect == ModEffects.DIZZINESS.get() || effect == ModEffects.FATIGUE.get()) {
                event.setResult(Result.DENY);
                return;
            }
        }

        if (effect == MobEffects.POISON && entity.hasEffect(ModEffects.POISON_RESISTANCE.get())) {
            event.setResult(Result.DENY);
            if (ModernDamage.LOGGER.isDebugEnabled()) {
                ModernDamage.LOGGER.debug("Poison resistance prevented poison effect on {}", entity.getName().getString());
            }
            return;
        }

        if (isSporeLoaded && effect != null) {
            ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            if (MYCELIUM_EFFECT.equals(effectId) && entity.hasEffect(ModEffects.INFECTION_RESISTANCE.get())) {
                event.setResult(Result.DENY);
                if (ModernDamage.LOGGER.isDebugEnabled()) {
                    ModernDamage.LOGGER.debug("Infection resistance prevented mycelium effect on {}", entity.getName().getString());
                }
            }
        }

        if (effect == ModEffects.FATIGUE.get() && entity.hasEffect(ModEffects.CREATINE_BOOST.get())) {
            event.setResult(Result.DENY);
            if (ModernDamage.LOGGER.isDebugEnabled()) {
                ModernDamage.LOGGER.debug("Creatine boost prevented fatigue effect on {}", entity.getName().getString());
            }
        }
    }
}