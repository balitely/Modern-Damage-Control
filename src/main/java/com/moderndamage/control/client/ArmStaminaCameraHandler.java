package com.moderndamage.control.client;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.attribute.ModAttributes;
import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModernDamage.MODID, value = Dist.CLIENT)
public class ArmStaminaCameraHandler {
    private static float angle = 0f;
    private static long lastTimestamp = System.currentTimeMillis();

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ModClothConfig config = ModClothConfig.get();
        if (!config.enableArmStamina || !config.enableStaminaSway) return;

        // 从缓存中获取瞄准状态（由服务端同步）
        boolean isAiming = ClientArmStaminaCache.isAiming(player.getUUID());
        if (!isAiming) return;

        float stamina = ClientArmStaminaCache.getStamina(player.getUUID());
        float maxStamina = (float) player.getAttributeValue(ModAttributes.MAX_ARM_STAMINA.get());
        if (maxStamina <= 0) return;
        float ratio = stamina / maxStamina;

        float amplitude = 0f;
        if (ratio < config.criticallyLowStaminaThreshold) {
            amplitude = (float) config.criticallyLowStaminaSwayAmplitude;
        } else if (ratio < config.lowStaminaThreshold) {
            amplitude = (float) config.lowStaminaSwayAmplitude;
        } else {
            return;
        }

        amplitude *= config.staminaSwayMultiplier;

        long now = System.currentTimeMillis();
        float delta = (now - lastTimestamp) / 1000f;
        lastTimestamp = now;
        angle += delta * config.staminaSwaySpeed;
        if (angle > (float) (Math.PI * 2)) angle -= (float) (Math.PI * 2);

        float swayYaw = (float) Math.sin(angle) * amplitude;
        float swayPitch = (float) Math.cos(angle * 1.3f) * amplitude * 0.6f;

        player.setXRot(player.getXRot() + swayPitch);
        player.setYRot(player.getYRot() + swayYaw);
    }
}