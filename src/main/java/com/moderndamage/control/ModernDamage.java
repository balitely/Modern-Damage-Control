package com.moderndamage.control;

import com.moderndamage.control.network.Networking;
import com.moderndamage.control.util.MaxHealthModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import com.moderndamage.control.capability.parthealth.*;
import com.moderndamage.control.armor.ArmorDataLoader;
import com.moderndamage.control.attribute.ModAttributes;
import com.moderndamage.control.command.ModerndamageCommand;
import com.moderndamage.control.compat.EnhancedVisualsCompat;
import com.moderndamage.control.config.EntityHitboxConfigLoader;
import com.moderndamage.control.config.ModClothConfig;
import com.moderndamage.control.effect.ModEffects;
import com.moderndamage.control.event.*;
import com.moderndamage.control.item.ModCreativeTab;
import com.moderndamage.control.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

@Mod(ModernDamage.MODID)
public class ModernDamage {
    public static final String MODID = "moderndamage";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public ModernDamage() {
        ModItems.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModAttributes.ATTRIBUTES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModEffects.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModClothConfig.register();

        ModCreativeTab.register(FMLJavaModLoadingContext.get().getModEventBus());

        MinecraftForge.EVENT_BUS.register(new InjuryEventHandler());
        MinecraftForge.EVENT_BUS.register(new ArmorTooltipHandler());
        MinecraftForge.EVENT_BUS.register(new StimulantEffectHandler());
        MinecraftForge.EVENT_BUS.register(new ResistanceEventHandler());

        MinecraftForge.EVENT_BUS.register(new CapabilityAttachHandler());
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerTick);

        MinecraftForge.EVENT_BUS.addListener(ModerndamageCommand::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onEntityJoinLevel);
        MinecraftForge.EVENT_BUS.register(new HealEventHandler());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onEntityAttributeModification);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    public static EnhancedVisualsCompat enhancedVisuals = null;

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Path configDir = FMLPaths.CONFIGDIR.get();
            ArmorDataLoader.init(configDir);
            EntityHitboxConfigLoader.init(configDir);
            MaxHealthModifier.init();
            LOGGER.info("ModernDamageControl 已加载");
            Networking.register();
            if (ModList.get().isLoaded("enhancedvisuals")) {
                enhancedVisuals = new EnhancedVisualsCompat();
                MinecraftForge.EVENT_BUS.register(enhancedVisuals);
                LOGGER.info("EnhancedVisuals 集成已启用");
            }
        });
    }

    private void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            event.add(type, ModAttributes.HEAD_NATURAL_ARMOR.get());
            event.add(type, ModAttributes.CHEST_NATURAL_ARMOR.get());
            event.add(type, ModAttributes.STOMACH_NATURAL_ARMOR.get());
            event.add(type, ModAttributes.ARM_NATURAL_ARMOR.get());
            event.add(type, ModAttributes.LEG_NATURAL_ARMOR.get());
        }
        LOGGER.info("添加天然护甲属性到所有实体");
    }

    private void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        var cfg = EntityHitboxConfigLoader.getConfig(living.getType());
        if (cfg == null) return;

        setAttribute(living, ModAttributes.HEAD_NATURAL_ARMOR.get(), cfg.naturalArmor.get("head"));
        setAttribute(living, ModAttributes.CHEST_NATURAL_ARMOR.get(), cfg.naturalArmor.get("chest"));
        setAttribute(living, ModAttributes.STOMACH_NATURAL_ARMOR.get(), cfg.naturalArmor.get("stomach"));
        setAttribute(living, ModAttributes.ARM_NATURAL_ARMOR.get(), cfg.naturalArmor.get("arm"));
        setAttribute(living, ModAttributes.LEG_NATURAL_ARMOR.get(), cfg.naturalArmor.get("leg"));
    }

    private void setAttribute(LivingEntity entity, Attribute attribute, Integer value) {
        if (value != null && value != 0) {
            var instance = entity.getAttribute(attribute);
            if (instance != null) {
                instance.setBaseValue(value);
            }
        }
    }

    public static class CapabilityAttachHandler {
        @SubscribeEvent
        public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            ModClothConfig config = ModClothConfig.get();
            if (config.damageModel != ModClothConfig.DamageModel.HARDCORE) return;

            if (event.getObject().level().isClientSide) return;

            if (event.getObject() instanceof Player player) {
                event.addCapability(new ResourceLocation(ModernDamage.MODID, "part_health"), new PlayerPartHealthProvider(player));
                ModernDamage.LOGGER.debug("附加玩家部位血量能力（服务端）");
            } else if (config.creaturePartHealthEnabled && event.getObject() instanceof LivingEntity living && !(living instanceof Player)) {
                event.addCapability(new ResourceLocation(ModernDamage.MODID, "creature_part_health"), new CreaturePartHealthProvider(living));
                ModernDamage.LOGGER.debug("附加生物部位血量能力（服务端）");
            }
        }
    }

    private void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;
        player.getCapability(PartHealthCapability.PART_HEALTH_CAP).ifPresent(IPartHealth::tick);
    }
}