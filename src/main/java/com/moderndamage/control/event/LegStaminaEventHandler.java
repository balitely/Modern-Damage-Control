package com.moderndamage.control.event;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.capability.legstamina.LegStaminaCapability;
import com.moderndamage.control.config.ModClothConfig;
import com.tacz.guns.api.entity.IGunOperator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModernDamage.MODID)
public class LegStaminaEventHandler {
    private static boolean lastCrouching = false;
    private static boolean lastCrawling = false;

    // 跳跃消耗
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        ModClothConfig config = ModClothConfig.get();
        if (!config.enableLegStamina) return;
        // 创造模式不消耗耐力
        if (player.isCreative()) return;

        player.getCapability(LegStaminaCapability.LEG_STAMINA).ifPresent(stamina -> {
            stamina.consumeStamina(config.legJumpCost, false);
        });
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        ModClothConfig config = ModClothConfig.get();
        if (!config.enableLegStamina) return;
        // 创造模式不消耗耐力
        if (player.isCreative()) return;

        player.getCapability(LegStaminaCapability.LEG_STAMINA).ifPresent(stamina -> {
            // 疾跑消耗
            if (player.isSprinting()) {
                stamina.consumeStamina(config.legSprintingCostPerTick, false);
            }
            // 游泳消耗
            if (player.isSwimming()) {
                stamina.consumeStamina(config.legSwimmingCostPerTick, false);
            }

            // 原版潜行状态变化检测
            boolean crouching = player.isCrouching();
            if (crouching != lastCrouching) {
                lastCrouching = crouching;
                if (crouching) {
                    stamina.consumeStamina(config.legCrouchEnterCost, false);
                } else {
                    stamina.consumeStamina(config.legCrouchExitCost, false);
                }
            }

            // TACZ 趴下状态变化检测
            try {
                IGunOperator operator = IGunOperator.fromLivingEntity(player);
                if (operator != null && operator.getDataHolder() != null) {
                    boolean crawling = operator.getDataHolder().isCrawling;
                    if (crawling != lastCrawling) {
                        lastCrawling = crawling;
                        if (crawling) {
                            stamina.consumeStamina(config.legCrawlEnterCost, false);
                        } else {
                            stamina.consumeStamina(config.legCrawlExitCost, false);
                        }
                    }
                }
            } catch (Throwable ignored) {}
        });
    }
}