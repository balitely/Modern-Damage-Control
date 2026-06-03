package com.moderndamage.control.client;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModernDamage.MODID, value = Dist.CLIENT)
public class ArmStaminaOverlay {
    private static final ResourceLocation ARM_STAMINA_TEXTURE =
            new ResourceLocation(ModernDamage.MODID, "textures/gui/armstamina.png");
    private static final ResourceLocation LEG_STAMINA_TEXTURE =
            new ResourceLocation(ModernDamage.MODID, "textures/gui/legstamina.png");

    private static final int TEXTURE_WIDTH = 91;
    private static final int TEXTURE_HEIGHT = 10;
    private static final int BAR_HEIGHT = 5;      // 背景/前景高度均为 5

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ModClothConfig config = ModClothConfig.get();
        if (!config.enableStaminaHUD) return;

        boolean drawArm = config.enableArmStamina;
        boolean drawLeg = config.enableLegStamina;
        if (!drawArm && !drawLeg) return;

        float scale = (float) config.staminaHUDScale;
        int scaledWidth = (int)(TEXTURE_WIDTH * scale);
        int scaledHeight = (int)(BAR_HEIGHT * scale);
        int totalWidth = (drawArm ? scaledWidth : 0) + (drawLeg ? scaledWidth : 0);

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int defaultX = (screenWidth - totalWidth) / 2;
        int defaultY = screenHeight - 40;

        int x = defaultX + config.staminaHUDXOffset;
        int y = defaultY + config.staminaHUDYOffset;

        if (drawLeg) {
            float legStamina = ClientLegStaminaCache.getStamina(mc.player.getUUID());
            float maxLegStamina = (float) mc.player.getAttributeValue(
                    com.moderndamage.control.attribute.ModAttributes.MAX_LEG_STAMINA.get());
            if (maxLegStamina > 0) {
                float ratio = legStamina / maxLegStamina;
                renderStaminaBar(event.getGuiGraphics(), LEG_STAMINA_TEXTURE,
                        x, y, scaledWidth, scaledHeight, ratio);
            }
            if (drawArm) x += scaledWidth;
        }
        if (drawArm) {
            float armStamina = ClientArmStaminaCache.getStamina(mc.player.getUUID());
            float maxArmStamina = (float) mc.player.getAttributeValue(
                    com.moderndamage.control.attribute.ModAttributes.MAX_ARM_STAMINA.get());
            if (maxArmStamina > 0) {
                float ratio = armStamina / maxArmStamina;
                renderStaminaBar(event.getGuiGraphics(), ARM_STAMINA_TEXTURE,
                        x, y, scaledWidth, scaledHeight, ratio);
            }
        }
    }

    private static void renderStaminaBar(GuiGraphics graphics, ResourceLocation texture,
                                         int x, int y, int width, int height, float ratio) {
        // 绘制背景：纹理上半部分 (0,0) 到 (TEXTURE_WIDTH, BAR_HEIGHT)
        graphics.blit(texture, x, y, width, height,
                0, 0, TEXTURE_WIDTH, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // 绘制前景：纹理下半部分 (0, BAR_HEIGHT) 开始，只绘制前 ratio 宽度的部分
        if (ratio > 0.001f) {
            int fillWidth = (int)(width * ratio);
            if (fillWidth > 0) {
                graphics.blit(texture, x, y, fillWidth, height,
                        0, BAR_HEIGHT, fillWidth, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            }
        }

        // 低耐力闪烁效果
        if (ratio < 0.2f && (System.currentTimeMillis() / 200 % 2 == 0)) {
            graphics.fill(x, y, x + width, y + height, 0x80FF0000);
        }
    }
}