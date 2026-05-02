package com.moderndamage.control.client;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.capability.parthealth.IPartHealth;
import com.moderndamage.control.capability.parthealth.PartHealthCapability;
import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.EnumMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ModernDamage.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PartHealthOverlay {
    private static final ResourceLocation FLASH_TEXTURE = new ResourceLocation(ModernDamage.MODID, "textures/gui/part_health_0.png");
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[7];
    static {
        for (int i = 1; i <= 6; i++) {
            TEXTURES[i] = new ResourceLocation(ModernDamage.MODID, "textures/gui/part_health_" + i + ".png");
        }
    }

    private static final Map<ModDamagePart, int[]> PART_UV = new EnumMap<>(ModDamagePart.class);
    static {
        PART_UV.put(ModDamagePart.HEAD, new int[]{4, 0, 8, 8});
        PART_UV.put(ModDamagePart.CHEST, new int[]{4, 8, 8, 7});
        PART_UV.put(ModDamagePart.STOMACH, new int[]{4, 15, 8, 5});
        PART_UV.put(ModDamagePart.RIGHT_ARM, new int[]{0, 8, 4, 12});
        PART_UV.put(ModDamagePart.LEFT_ARM, new int[]{12, 8, 4, 12});
        PART_UV.put(ModDamagePart.RIGHT_LEG, new int[]{4, 20, 4, 12});
        PART_UV.put(ModDamagePart.LEFT_LEG, new int[]{8, 20, 4, 12});
    }

    private static int getTextureLevel(float percent) {
        if (percent >= 0.81f) return 1;
        if (percent >= 0.61f) return 2;
        if (percent >= 0.41f) return 3;
        if (percent >= 0.21f) return 4;
        if (percent >= 0.06f) return 5;
        return 6;
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        ModClothConfig config = ModClothConfig.get();
        if (!config.enablePartHealthHUD) return;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        if (ClientPartHealthCache.get(player.getUUID()).isEmpty()) {
            ClientPartHealthCache.initDefault(player.getUUID());
        }

        ClientPartHealthCache.get(player.getUUID()).ifPresent(cache -> {
            renderPartHealth(event.getGuiGraphics(), player, cache, config);
        });
    }

    private static void renderPartHealth(GuiGraphics graphics, Player player, IPartHealth cap, ModClothConfig config) {
        int baseX = config.partHealthHUDX;
        int baseY = config.partHealthHUDY;
        float scale = config.partHealthHUDPartScale;

        boolean flash = (player.tickCount - cap.getLastDamageTick()) < 2;

        for (ModDamagePart part : ModDamagePart.values()) {
            float percent = cap.getHealth(part) / cap.getMaxHealth(part);
            int level = getTextureLevel(percent);
            ResourceLocation texture = flash ? FLASH_TEXTURE : TEXTURES[level];
            if (texture == null) continue;

            int[] uv = PART_UV.get(part);
            int x = baseX + (int)(uv[0] * scale);
            int y = baseY + (int)(uv[1] * scale);
            int w = (int)(uv[2] * scale);
            int h = (int)(uv[3] * scale);
            graphics.blit(texture, x, y, w, h, uv[0], uv[1], uv[2], uv[3], 16, 32);
        }
    }
}