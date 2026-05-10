package com.moderndamage.control.client;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModernDamage.MODID, value = Dist.CLIENT)
public class ClientEventBus {

    @SubscribeEvent
    public static void onInputUpdate(MovementInputUpdateEvent event) {
        Player player = event.getEntity();

        boolean shouldPrevent = false;
        if (player.hasEffect(ModEffects.PAIN.get()) && !player.hasEffect(ModEffects.PAIN_SUPPRESSION.get())) {
            shouldPrevent = true;
        }
        if (player.hasEffect(ModEffects.DIZZINESS.get()) && !player.hasEffect(ModEffects.DIZZINESS_RESISTANCE.get())) {
            shouldPrevent = true;
        }

        if (player.hasEffect(ModEffects.LEFT_LEG_FRACTURE.get()) ||
                player.hasEffect(ModEffects.RIGHT_LEG_FRACTURE.get()) ||
                player.hasEffect(ModEffects.LEFT_LEG_TRAUMA.get()) ||
                player.hasEffect(ModEffects.RIGHT_LEG_TRAUMA.get())) {
            shouldPrevent = true;
        }

        if (shouldPrevent) {
            event.getInput().jumping = false;

            if (event.getInput().forwardImpulse > 0.7f) {
                event.getInput().forwardImpulse = 0.7f;
            }

            if (player.isSprinting()) {
                player.setSprinting(false);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().player != null) {
            Player player = Minecraft.getInstance().player;

            ClientPartHealthCache.get(player.getUUID())
                    .ifPresent(ClientPartHealthCache::tick);

            if (ModernDamage.enhancedVisuals != null) {
                boolean hasPain = player.hasEffect(ModEffects.PAIN.get());
                boolean hasPainSuppression = player.hasEffect(ModEffects.PAIN_SUPPRESSION.get());
                ModernDamage.enhancedVisuals.updatePainVisual(player, hasPain, hasPainSuppression);

                boolean hasDizzy = player.hasEffect(ModEffects.DIZZINESS.get());
                boolean hasDizzyResist = player.hasEffect(ModEffects.DIZZINESS_RESISTANCE.get());
                ModernDamage.enhancedVisuals.updateDizzyVisual(player, hasDizzy, hasDizzyResist);
            }
        }
    }
}