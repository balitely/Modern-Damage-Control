package com.moderndamage.control.compat;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.effect.ModEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.creativecore.common.config.premade.curve.DecimalCurve;
import team.creative.enhancedvisuals.api.Visual;
import team.creative.enhancedvisuals.api.VisualHandler;
import team.creative.enhancedvisuals.api.type.VisualType;
import team.creative.enhancedvisuals.client.VisualManager;
import team.creative.enhancedvisuals.common.handler.VisualHandlers;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EnhancedVisuals 模组联动（可选依赖）
 * 只有当 EnhancedVisuals 被加载时，此类才会被实例化和注册。
 */
public class EnhancedVisualsCompat {

    private final Map<UUID, Visual> painVisuals = new ConcurrentHashMap<>();
    private final Map<UUID, Visual> dizzyVisuals = new ConcurrentHashMap<>();

    private final VisualHandler dummyHandler = new VisualHandler() {
        @Override
        public void tick(@Nullable Player player) {}
        { enabled = true; opacity = 1.0f; }
    };

    private VisualType lowHealthType = null;
    private VisualType blurType = null;
    private boolean initialized = false;

    public EnhancedVisualsCompat() {
        try {
            lowHealthType = VisualHandlers.HEARTBEAT.lowhealth;
            blurType = VisualHandlers.HEARTBEAT.blur;
            if (lowHealthType != null && blurType != null) {
                ModernDamage.LOGGER.info("EnhancedVisuals integration active");
                initialized = true;
            } else {
                ModernDamage.LOGGER.warn("EnhancedVisuals visuals not found, integration disabled");
                initialized = false;
            }
        } catch (Exception e) {
            ModernDamage.LOGGER.warn("Failed to integrate EnhancedVisuals", e);
            initialized = false;
        }
    }

    private void playSound(Player player, String soundName, float volume) {
        if (!initialized) return;
        try {
            ResourceLocation soundLoc = new ResourceLocation("enhancedvisuals", soundName);
            VisualHandlers.HEARTBEAT.playSound(soundLoc, volume);
        } catch (Exception e) {
            ModernDamage.LOGGER.warn("Failed to play sound", e);
        }
    }

    private void addPainEffect(Player player) {
        if (!initialized) return;
        UUID id = player.getUUID();
        if (painVisuals.containsKey(id)) return;
        DecimalCurve curve = new DecimalCurve(0.6f, 0.6f, Integer.MAX_VALUE, 0);
        Visual visual = new Visual(lowHealthType, dummyHandler, curve, 0);
        visual.setOpacityInternal(1.0f);
        VisualManager.add(visual);
        painVisuals.put(id, visual);
        playSound(player, "heartbeatin", 0.8f);
        ModernDamage.LOGGER.debug("Added pain visual and sound for {}", player.getName().getString());
    }

    private void removePainEffect(Player player) {
        if (!initialized) return;
        UUID id = player.getUUID();
        Visual visual = painVisuals.remove(id);
        if (visual != null) {
            VisualManager.remove(visual);
            ModernDamage.LOGGER.debug("Removed pain visual for {}", player.getName().getString());
        }
    }

    private void addDizzyEffect(Player player) {
        if (!initialized) return;
        UUID id = player.getUUID();
        if (dizzyVisuals.containsKey(id)) return;
        DecimalCurve curve = new DecimalCurve(30f, 30f, Integer.MAX_VALUE, 0);
        Visual visual = new Visual(blurType, dummyHandler, curve, 0);
        visual.setOpacityInternal(1.0f);
        VisualManager.add(visual);
        dizzyVisuals.put(id, visual);
        playSound(player, "ringing", 1.0f);
        ModernDamage.LOGGER.debug("Added dizzy visual and sound for {}", player.getName().getString());
    }

    private void removeDizzyEffect(Player player) {
        if (!initialized) return;
        UUID id = player.getUUID();
        Visual visual = dizzyVisuals.remove(id);
        if (visual != null) {
            VisualManager.remove(visual);
            ModernDamage.LOGGER.debug("Removed dizzy visual for {}", player.getName().getString());
        }
    }

    public void updatePainVisual(Player player, boolean hasPainEffect, boolean hasSuppression) {
        if (!initialized) return;
        boolean shouldHave = hasPainEffect && !hasSuppression;
        boolean currentlyHas = painVisuals.containsKey(player.getUUID());
        if (shouldHave && !currentlyHas) {
            addPainEffect(player);
        } else if (!shouldHave && currentlyHas) {
            removePainEffect(player);
        }
    }

    public void updateDizzyVisual(Player player, boolean hasDizzyEffect, boolean hasResistance) {
        if (!initialized) return;
        boolean shouldHave = hasDizzyEffect && !hasResistance;
        boolean currentlyHas = dizzyVisuals.containsKey(player.getUUID());
        if (shouldHave && !currentlyHas) {
            addDizzyEffect(player);
        } else if (!shouldHave && currentlyHas) {
            removeDizzyEffect(player);
        }
    }

    // 事件监听方法（非静态）
    @SubscribeEvent
    public void onPotionAdded(MobEffectEvent.Added event) {
        if (!initialized) return;
        if (!(event.getEntity() instanceof Player player)) return;
        // 可选：处理效果刚添加时的视觉更新
    }

    @SubscribeEvent
    public void onPotionRemoved(MobEffectEvent.Remove event) {
        if (!initialized) return;
        if (!(event.getEntity() instanceof Player player)) return;
        MobEffect effect = event.getEffect();
        if (effect == ModEffects.PAIN.get()) {
            removePainEffect(player);
        } else if (effect == ModEffects.DIZZINESS.get()) {
            removeDizzyEffect(player);
        }
    }

    @SubscribeEvent
    public void onPotionExpired(MobEffectEvent.Expired event) {
        if (!initialized) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getEffectInstance() == null) return;
        MobEffect effect = event.getEffectInstance().getEffect();
        if (effect == ModEffects.PAIN.get()) {
            removePainEffect(player);
        } else if (effect == ModEffects.DIZZINESS.get()) {
            removeDizzyEffect(player);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!initialized) return;
        removePainEffect(event.getEntity());
        removeDizzyEffect(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!initialized) return;
        removePainEffect(event.getEntity());
        removeDizzyEffect(event.getEntity());
    }
}