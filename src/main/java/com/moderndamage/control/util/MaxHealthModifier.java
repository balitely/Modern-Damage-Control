package com.moderndamage.control.util;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ModernDamage.MODID)
public class MaxHealthModifier {
    private static final UUID PLAYER_MODIFIER_UUID = UUID.fromString("a1a2a3a4-0000-1111-2222-bbbbccccdddd");
    private static final String PLAYER_MODIFIER_NAME = "moderndamage_player_max_health_bonus";
    private static final String ENTITY_MODIFIER_NAME = "moderndamage_entity_max_health_bonus";
    private static final Map<EntityType<?>, Double> CACHED_ENTITY_BONUS = new HashMap<>();

    public static void init() {
        ModClothConfig config = ModClothConfig.get();
        CACHED_ENTITY_BONUS.clear();
        for (Map.Entry<String, Double> entry : config.entityMaxHealthBonus.entrySet()) {
            ResourceLocation key = new ResourceLocation(entry.getKey());
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(key);
            if (type != null) {
                CACHED_ENTITY_BONUS.put(type, entry.getValue());
            } else {
                ModernDamage.LOGGER.warn("Unknown entity type in entityMaxHealthBonus: {}", entry.getKey());
            }
        }
    }

    private static void addModifier(LivingEntity entity, double bonus, UUID uuid, String name) {
        if (bonus == 0) return;
        AttributeInstance attr = entity.getAttribute(Attributes.MAX_HEALTH);
        if (attr == null) return;
        attr.removeModifier(uuid);
        AttributeModifier modifier = new AttributeModifier(uuid, name, bonus, AttributeModifier.Operation.ADDITION);
        attr.addPermanentModifier(modifier);
        ModernDamage.LOGGER.debug("Added max health bonus of {} to {}", bonus, entity.getName().getString());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        applyPlayerBonus(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        applyPlayerBonus(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        applyPlayerBonus(event.getEntity());
    }

    private static void applyPlayerBonus(Player player) {
        ModClothConfig config = ModClothConfig.get();
        addModifier(player, config.playerMaxHealthBonus, PLAYER_MODIFIER_UUID, PLAYER_MODIFIER_NAME);
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living && !(living instanceof Player)) {
            Double bonus = CACHED_ENTITY_BONUS.get(living.getType());
            if (bonus != null && bonus != 0) {
                UUID entityUuid = UUID.nameUUIDFromBytes(("moderndamage_entity_" + ForgeRegistries.ENTITY_TYPES.getKey(living.getType()).toString()).getBytes());
                addModifier(living, bonus, entityUuid, ENTITY_MODIFIER_NAME);
            }
        }
    }
}