package com.moderndamage.control.client;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.attribute.ModAttributes;
import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = ModernDamage.MODID, value = Dist.CLIENT)
public class StaminaSwayModifier {
    private static Float originalStrength = null;
    private static int currentMultiplier = 1;
    private static Object clientConfig = null;
    private static ForgeConfigSpec.DoubleValue strengthConfig = null;

    // 获取 TaczAdditions 的客户端配置实例和 scopeSwayStrength 字段
    private static void initTaczConfig() {
        if (clientConfig != null) return;
        try {
            Class<?> configClass = Class.forName("com.raiiiden.taczadditions.config.TacZAdditionsConfig");
            Field clientField = configClass.getDeclaredField("CLIENT");
            clientField.setAccessible(true);
            clientConfig = clientField.get(null);
            if (clientConfig != null) {
                Field strengthField = clientConfig.getClass().getDeclaredField("scopeSwayStrength");
                strengthField.setAccessible(true);
                strengthConfig = (ForgeConfigSpec.DoubleValue) strengthField.get(clientConfig);
                if (strengthConfig != null) {
                    originalStrength = strengthConfig.get().floatValue();
                    ModernDamage.LOGGER.info("TaczAdditions scopeSwayStrength detected: {}", originalStrength);
                }
            }
        } catch (Exception e) {
            ModernDamage.LOGGER.warn("TaczAdditions not found or configuration inaccessible. Stamina sway will be disabled.");
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ModClothConfig config = ModClothConfig.get();
        if (!config.enableArmStamina || !config.enableStaminaSway) return;
        initTaczConfig();
        if (strengthConfig == null || originalStrength == null) return;

        // 获取玩家耐力比例
        float stamina = ClientArmStaminaCache.getStamina(mc.player.getUUID());
        float maxStamina = (float) mc.player.getAttributeValue(ModAttributes.MAX_ARM_STAMINA.get());
        if (maxStamina <= 0) return;
        float ratio = stamina / maxStamina;

        int newMultiplier = 1;
        if (ratio < config.criticallyLowStaminaThreshold) {
            newMultiplier = 3;  // 极低耐力时晃动强度为 3 倍
        } else if (ratio < config.lowStaminaThreshold) {
            newMultiplier = 2;  // 低耐力时晃动强度为 2 倍
        }

        if (newMultiplier != currentMultiplier) {
            currentMultiplier = newMultiplier;
            float newStrength = originalStrength * currentMultiplier;
            try {
                // 通过反射修改 ForgeConfigSpec.DoubleValue 的内部 value 字段
                Field valueField = ForgeConfigSpec.DoubleValue.class.getDeclaredField("value");
                valueField.setAccessible(true);
                valueField.set(strengthConfig, newStrength);
                ModernDamage.LOGGER.debug("Set scopeSwayStrength to {} (multiplier {})", newStrength, currentMultiplier);
            } catch (Exception e) {
                ModernDamage.LOGGER.error("Failed to modify scopeSwayStrength", e);
            }
        }
    }
}