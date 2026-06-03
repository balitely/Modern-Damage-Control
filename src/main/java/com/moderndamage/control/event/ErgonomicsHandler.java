package com.moderndamage.control.event;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.attribute.ModAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ErgonomicsHandler {
    private static final UUID ADS_MODIFIER_UUID = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000001");
    private static final UUID DRAW_MODIFIER_UUID = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000002");
    private static final UUID RELOAD_MODIFIER_UUID = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000003");
    private static final String MODIFIER_NAME = "ergonomics_modifier";

    private final Map<Player, Float> lastErgonomics = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;

        AttributeInstance ergoAttr = player.getAttribute(ModAttributes.ERGONOMICS.get());
        if (ergoAttr == null) return;

        float currentErgo = (float) ergoAttr.getValue();
        Float last = lastErgonomics.get(player);
        if (last != null && Math.abs(currentErgo - last) < 0.01f) return;

        lastErgonomics.put(player, currentErgo);
        updateModifiers(player, currentErgo);
    }

    private void updateModifiers(Player player, float ergo) {
        float delta = 100.0f - ergo;
        boolean hasTAA = ModList.get().isLoaded("taa");
        boolean hasTACZAttr = ModList.get().isLoaded("tacz_attributes");

        // 1. 开镜速度 (taa:ads_time) - 乘法
        if (hasTAA) {
            Attribute adsTimeAttr = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("taa", "ads_time"));
            if (adsTimeAttr != null) {
                float multiplier = 1.0f + (delta / 100.0f);
                multiplier = Math.min(3.0f, Math.max(0.33f, multiplier));
                applyModifier(player, adsTimeAttr, ADS_MODIFIER_UUID, multiplier, AttributeModifier.Operation.MULTIPLY_TOTAL);
                ModernDamage.LOGGER.debug("Ergonomics: ads_time multiplier = {} (delta={})", multiplier, delta);
            }
        }

        // 2. 切枪速度 (tacz_attributes:draw_speed) - 加法
        if (hasTACZAttr) {
            Attribute drawSpeedAttr = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("tacz_attributes", "draw_speed"));
            if (drawSpeedAttr != null) {
                float add = delta * 0.005f;
                add = Math.min(1.0f, Math.max(-1.0f, add));
                applyModifier(player, drawSpeedAttr, DRAW_MODIFIER_UUID, add, AttributeModifier.Operation.ADDITION);
                ModernDamage.LOGGER.debug("Ergonomics: draw_speed add = {}", add);
            }
        }

        // 3. 换弹时间 (taa:reload_time) - 乘法，直接影响换弹所需时间（值越大换弹越慢）
        if (hasTAA) {
            Attribute reloadTimeAttr = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("taa", "reload_time"));
            if (reloadTimeAttr != null) {
                float add = delta * 0.005f;
                add = Math.min(2.0f, Math.max(-2.0f, add));
                applyModifier(player, reloadTimeAttr, RELOAD_MODIFIER_UUID, add, AttributeModifier.Operation.ADDITION);
                ModernDamage.LOGGER.debug("Ergonomics: reload_time add = {} seconds", add);
            }
        }
    }

    private void applyModifier(Player player, Attribute attr, UUID uuid, float amount, AttributeModifier.Operation op) {
        AttributeInstance instance = player.getAttribute(attr);
        if (instance == null) {
            ModernDamage.LOGGER.warn("Attribute instance is null for {}", ForgeRegistries.ATTRIBUTES.getKey(attr));
            return;
        }
        instance.removeModifier(uuid);
        if (Math.abs(amount) > 0.001f && amount != 1.0f) {
            AttributeModifier modifier = new AttributeModifier(uuid, MODIFIER_NAME, amount, op);
            instance.addTransientModifier(modifier);
            ModernDamage.LOGGER.debug("Applied modifier to {}: amount={}, op={}",
                    ForgeRegistries.ATTRIBUTES.getKey(attr), amount, op);
        } else {
            ModernDamage.LOGGER.debug("Removed modifier from {} (amount={})", ForgeRegistries.ATTRIBUTES.getKey(attr), amount);
        }
    }
}