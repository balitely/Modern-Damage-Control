package com.moderndamage.control.event;

import com.moderndamage.control.capability.parthealth.IPartHealth;
import com.moderndamage.control.capability.parthealth.PartHealthCapability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HealEventHandler {

    @SubscribeEvent
    public void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        float amount = event.getAmount();
        if (amount <= 0) return;

        if (entity instanceof Player player) {
            player.getCapability(PartHealthCapability.PART_HEALTH_CAP).ifPresent(cap -> {
                cap.healAll(amount);
                event.setCanceled(true);
            });
        } else {
        }
    }
}