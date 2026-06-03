package com.moderndamage.control;

import com.moderndamage.control.capability.armstamina.*;
import com.moderndamage.control.capability.legstamina.ILegStamina;
import com.moderndamage.control.capability.legstamina.LegStamina;
import com.moderndamage.control.capability.legstamina.LegStaminaCapability;
import com.moderndamage.control.capability.legstamina.LegStaminaProvider;
import com.moderndamage.control.event.ArmStaminaEventHandler;
import com.moderndamage.control.armor.ArmorDataLoader;
import com.moderndamage.control.attribute.ModAttributes;
import com.moderndamage.control.capability.armstamina.ArmStaminaCapability;
import com.moderndamage.control.capability.armstamina.ArmStaminaProvider;
import com.moderndamage.control.capability.parthealth.CreaturePartHealthProvider;
import com.moderndamage.control.capability.parthealth.IPartHealth;
import com.moderndamage.control.capability.parthealth.PartHealthCapability;
import com.moderndamage.control.capability.parthealth.PlayerPartHealthProvider;
import com.moderndamage.control.command.ModerndamageCommand;
import com.moderndamage.control.compat.EnhancedVisualsCompat;
import com.moderndamage.control.config.EntityHitboxConfigLoader;
import com.moderndamage.control.config.ModClothConfig;
import com.moderndamage.control.effect.ModEffects;
import com.moderndamage.control.event.*;
import com.moderndamage.control.item.ModCreativeTab;
import com.moderndamage.control.item.ModItems;
import com.moderndamage.control.network.Networking;
import com.moderndamage.control.util.MaxHealthModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.UUID;

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
        MinecraftForge.EVENT_BUS.register(new ArmStaminaEventHandler());
        MinecraftForge.EVENT_BUS.register(new LegStaminaEventHandler());
        MinecraftForge.EVENT_BUS.register(new ErgonomicsHandler());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onEntityAttributeModification);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static EnhancedVisualsCompat enhancedVisuals = null;

    // ========== 集中注册所有药水效果的属性修饰符 ==========
    private void registerEffectModifiers() {
        registerBasicModifiers();
        registerConditionalModifiers();
        registerStaminaModifiers();
        registerInjectorModifiers();
    }

    private void registerBasicModifiers() {
        // 左臂创伤 (LeftArmTraumaEffect)
        ModEffects.LEFT_ARM_TRAUMA.get()
                .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                        String.valueOf(UUID.fromString("a1b2c3d4-1111-2222-3333-aaaaaaaaaaaa")),
                        -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(Attributes.ATTACK_SPEED,
                        String.valueOf(UUID.fromString("a1b2c3d4-8888-9999-aaaa-bbbbbbbbbccc")),
                        -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // 右臂创伤 (RightArmTraumaEffect)
        ModEffects.RIGHT_ARM_TRAUMA.get()
                .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                        String.valueOf(UUID.fromString("b1b2c3d4-1111-2222-3333-aaaaaaaaaaaa")),
                        -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(Attributes.ATTACK_SPEED,
                        String.valueOf(UUID.fromString("b1b2c3d4-8888-9999-aaaa-bbbbbbbbbccc")),
                        -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // 胃部创伤 (StomachTraumaEffect)
        ModEffects.STOMACH_TRAUMA.get()
                .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                        String.valueOf(UUID.fromString("a3a3a3a4-1111-2222-3333-aaaaaaaaaaaa")),
                        -0.075, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE,
                        String.valueOf(UUID.fromString("a3a3a3a4-2222-3333-4444-bbbbbbbbbbbb")),
                        -0.5, AttributeModifier.Operation.ADDITION);

        // 左腿创伤 (LeftLegTraumaEffect)
        ModEffects.LEFT_LEG_TRAUMA.get()
                .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                        String.valueOf(UUID.fromString("a1a2a3a4-1111-2222-3333-aaaaaaaaaaaa")),
                        -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // 右腿创伤 (RightLegTraumaEffect)
        ModEffects.RIGHT_LEG_TRAUMA.get()
                .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                        String.valueOf(UUID.fromString("a2a2a3a4-4444-5555-6666-bbbbbbbbbbbb")),
                        -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // ----- 骨折类效果 -----
        // 左臂骨折 (LeftArmFractureEffect)
        ModEffects.LEFT_ARM_FRACTURE.get()
                .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                        String.valueOf(UUID.fromString("a1b2c3d4-8888-9999-aaaa-bbbbbbbbbbbb")),
                        -0.15, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // 右臂骨折 (RightArmFractureEffect)
        ModEffects.RIGHT_ARM_FRACTURE.get()
                .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                        String.valueOf(UUID.fromString("c1c2c3d4-8888-9999-aaaa-bbbbbbbbbbbb")),
                        -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // 左腿骨折 (LeftLegFractureEffect)
        ModEffects.LEFT_LEG_FRACTURE.get()
                .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                        String.valueOf(UUID.fromString("f1f2f3f4-1111-2222-3333-aaaaaaaaaaaa")),
                        -0.15, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // 右腿骨折 (RightLegFractureEffect)
        ModEffects.RIGHT_LEG_FRACTURE.get()
                .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                        String.valueOf(UUID.fromString("f2f2f3f4-4444-5555-6666-bbbbbbbbbbbb")),
                        -0.15, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // ----- 固定骨折类效果（夹板）-----
        // 左臂固定 (LeftSplintedArmEffect)
        ModEffects.LEFT_SPLINTED_ARM.get()
                .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                        String.valueOf(UUID.fromString("b1b2c3d4-8888-9999-aaaa-bbbbbbbbbbbb")),
                        -0.075, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // 右臂固定 (RightSplintedArmEffect)
        ModEffects.RIGHT_SPLINTED_ARM.get()
                .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                        String.valueOf(UUID.fromString("d1d2d3d4-8888-9999-aaaa-bbbbbbbbbbbb")),
                        -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // 左腿固定 (LeftSplintedLegEffect)
        ModEffects.LEFT_SPLINTED_LEG.get()
                .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                        String.valueOf(UUID.fromString("f3f2f3f4-7777-8888-9999-aaaaaaaaaaaa")),
                        -0.075, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // 右腿固定 (RightSplintedLegEffect)
        ModEffects.RIGHT_SPLINTED_LEG.get()
                .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                        String.valueOf(UUID.fromString("f4f2f3f4-aaaa-bbbb-cccc-dddddddddddd")),
                        -0.075, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // ----- 负面影响效果 -----
        // 疼痛 (PainEffect)
        ModEffects.PAIN.get()
                .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                        String.valueOf(UUID.fromString("e1e2e3e4-1111-2222-3333-aaaaaaaaaaaa")),
                        -0.4, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(Attributes.ATTACK_SPEED,
                        String.valueOf(UUID.fromString("e1e2e3e4-aaaa-bbbb-cccc-dddddddddddd")),
                        -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // 眩晕 (DizzinessEffect)
        ModEffects.DIZZINESS.get()
                .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                        String.valueOf(UUID.fromString("f1f2f3f4-4444-5555-6666-aaaaaaaaaaaa")),
                        -0.6, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(Attributes.ATTACK_SPEED,
                        String.valueOf(UUID.fromString("f1f2f3f4-5555-6666-7777-bbbbbbbbbbbb")),
                        -0.9, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // 疲劳 (FatigueEffect)
        ModEffects.FATIGUE.get()
                .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                        String.valueOf(UUID.fromString("a4a4a4a4-1111-2222-3333-aaaaaaaaaaaa")),
                        -0.05, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // ----- 兴奋剂效果（基础属性）-----
        // 肾上腺素 (EpinephrineBoostEffect)
        ModEffects.EPINEPHRINE_BOOST.get()
                .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                        String.valueOf(UUID.fromString("e1e2e3e4-1111-2222-3333-aaaaaaaaaaaa")),
                        0.2, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                        String.valueOf(UUID.fromString("e1e2e3e4-3333-4444-5555-cccccccccccc")),
                        0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // 肌酸 (CreatineBoostEffect)
        ModEffects.CREATINE_BOOST.get()
                .addAttributeModifier(Attributes.ATTACK_SPEED,
                        String.valueOf(UUID.fromString("c1e2f3a4-1111-2222-3333-aaaaaaaaaaaa")),
                        0.15, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                        String.valueOf(UUID.fromString("c1e2f3a4-2222-3333-4444-bbbbbbbbbbbb")),
                        0.15, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                        String.valueOf(UUID.fromString("c1e2f3a4-3333-4444-5555-cccccccccccc")),
                        0.15, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    private void registerConditionalModifiers() {
        boolean hasTAA = ModList.get().isLoaded("taa");
        boolean hasTACZAttr = ModList.get().isLoaded("tacz_attributes");

        // ---- TAA 属性 ----
        if (hasTAA) {
            // 通用属性获取辅助
            Attribute reloadSpeed = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("taa", "reload_speed"));
            Attribute adsTime = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("taa", "ads_time"));
            Attribute moveSpeedTaa = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("taa", "move_speed"));
            Attribute rpm = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("taa", "rounds_per_minute"));
            String[] inaccuracyNames = {"inaccuracy_aim", "inaccuracy_lie", "inaccuracy_move", "inaccuracy_sneak", "inaccuracy_stand"};
            Attribute[] inaccuracyAttrs = new Attribute[5];
            for (int i = 0; i < inaccuracyNames.length; i++) {
                inaccuracyAttrs[i] = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("taa", inaccuracyNames[i]));
            }

            // 左臂创伤
            if (reloadSpeed != null)
                ModEffects.LEFT_ARM_TRAUMA.get().addAttributeModifier(reloadSpeed,
                        String.valueOf(UUID.fromString("a1b2c3d4-2222-3333-4444-bbbbbbbbbbbb")), 0.25, AttributeModifier.Operation.MULTIPLY_TOTAL);
            for (int i = 0; i < inaccuracyAttrs.length; i++) {
                if (inaccuracyAttrs[i] != null)
                    ModEffects.LEFT_ARM_TRAUMA.get().addAttributeModifier(inaccuracyAttrs[i],
                            String.valueOf(UUID.fromString(new String[]{"a1b2c3d4-3333-4444-5555-cccccccccccc",
                                    "a1b2c3d4-4444-5555-6666-dddddddddddd",
                                    "a1b2c3d4-5555-6666-7777-eeeeeeeeeeee",
                                    "a1b2c3d4-6666-7777-8888-ffffffffffff",
                                    "a1b2c3d4-7777-8888-9999-aaaaaaaaabbb"}[i])),
                            0.25, AttributeModifier.Operation.ADDITION);
            }

            // 右臂创伤
            if (adsTime != null)
                ModEffects.RIGHT_ARM_TRAUMA.get().addAttributeModifier(adsTime,
                        String.valueOf(UUID.fromString("b1b2c3d4-2222-3333-4444-bbbbbbbbbbbb")), 0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);
            for (int i = 0; i < inaccuracyAttrs.length; i++) {
                if (inaccuracyAttrs[i] != null)
                    ModEffects.RIGHT_ARM_TRAUMA.get().addAttributeModifier(inaccuracyAttrs[i],
                            String.valueOf(UUID.fromString(new String[]{"b1b2c3d4-6666-7777-8888-ffffffffffff",
                                    "b1b2c3d4-7777-8888-9999-aaaaaaaaabbb",
                                    "b1b2c3d4-8888-9999-aaaa-bbbbbbbbbccc",
                                    "b1b2c3d4-9999-aaaa-bbbb-cccccccccddd",
                                    "b1b2c3d4-aaaa-bbbb-cccc-dddddddddddd"}[i])),
                            0.15, AttributeModifier.Operation.ADDITION);
            }

            // 左腿骨折
            if (moveSpeedTaa != null)
                ModEffects.LEFT_LEG_FRACTURE.get().addAttributeModifier(moveSpeedTaa,
                        String.valueOf(UUID.fromString("f1f2f3f4-1234-5678-90ab-cdef12345678")), -0.03, AttributeModifier.Operation.MULTIPLY_TOTAL);
            // 右腿骨折
            if (moveSpeedTaa != null)
                ModEffects.RIGHT_LEG_FRACTURE.get().addAttributeModifier(moveSpeedTaa,
                        String.valueOf(UUID.fromString("f2f2f3f4-5678-90ab-cdef-123456789abc")), -0.03, AttributeModifier.Operation.MULTIPLY_TOTAL);

            // 疼痛 (Pain)
            if (moveSpeedTaa != null)
                ModEffects.PAIN.get().addAttributeModifier(moveSpeedTaa,
                        String.valueOf(UUID.fromString("e1e2e3e4-1234-5678-90ab-cdef12345678")), -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);
            if (adsTime != null)
                ModEffects.PAIN.get().addAttributeModifier(adsTime,
                        String.valueOf(UUID.fromString("e1e2e3e4-2222-3333-4444-bbbbbbbbbbbb")), 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);
            if (reloadSpeed != null)
                ModEffects.PAIN.get().addAttributeModifier(reloadSpeed,
                        String.valueOf(UUID.fromString("e1e2e3e4-6666-7777-8888-cccccccccccc")), 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
            if (rpm != null)
                ModEffects.PAIN.get().addAttributeModifier(rpm,
                        String.valueOf(UUID.fromString("e1e2e3e4-cccc-dddd-eeee-ffffffffffff")), -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);
            for (int i = 0; i < inaccuracyAttrs.length; i++) {
                if (inaccuracyAttrs[i] != null)
                    ModEffects.PAIN.get().addAttributeModifier(inaccuracyAttrs[i],
                            String.valueOf(UUID.fromString(new String[]{"e1e2e3e4-8888-9999-aaaa-bbbbbbbbbbbb",
                                    "e1e2e3e4-9999-aaaa-bbbb-cccccccccccc",
                                    "e1e2e3e4-aaaa-bbbb-cccc-dddddddddddd",
                                    "e1e2e3e4-bbbb-cccc-dddd-eeeeeeeeeeee",
                                    "e1e2e3e4-cccc-dddd-eeee-ffffffffffff"}[i])),
                            0.5, AttributeModifier.Operation.ADDITION);
            }

            // 眩晕 (Dizziness)
            if (moveSpeedTaa != null)
                ModEffects.DIZZINESS.get().addAttributeModifier(moveSpeedTaa,
                        String.valueOf(UUID.fromString("f1f2f3f4-aaaa-bbbb-cccc-aaaaaaaaabbb")), -0.12, AttributeModifier.Operation.MULTIPLY_TOTAL);
            if (reloadSpeed != null)
                ModEffects.DIZZINESS.get().addAttributeModifier(reloadSpeed,
                        String.valueOf(UUID.fromString("f1f2f3f4-6666-7777-8888-cccccccccccc")), 2.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
            if (rpm != null)
                ModEffects.DIZZINESS.get().addAttributeModifier(rpm,
                        String.valueOf(UUID.fromString("f1f2f3f4-9999-aaaa-bbbb-ffffffffffff")), -0.9, AttributeModifier.Operation.MULTIPLY_TOTAL);
            for (int i = 0; i < inaccuracyAttrs.length; i++) {
                if (inaccuracyAttrs[i] != null)
                    ModEffects.DIZZINESS.get().addAttributeModifier(inaccuracyAttrs[i],
                            String.valueOf(UUID.fromString(new String[]{"f1f2f3f4-bbbb-cccc-dddd-cccccccccddd",
                                    "f1f2f3f4-cccc-dddd-eeee-dddddddddddd",
                                    "f1f2f3f4-dddd-eeee-ffff-eeeeeeeeeeee",
                                    "f1f2f3f4-eeee-ffff-aaaa-ffffffffffff",
                                    "f1f2f3f4-ffff-aaaa-bbbb-aaaaaaaaaccc"}[i])),
                            1.0, AttributeModifier.Operation.ADDITION);
            }

            // 疲劳 (Fatigue)
            if (moveSpeedTaa != null)
                ModEffects.FATIGUE.get().addAttributeModifier(moveSpeedTaa,
                        String.valueOf(UUID.fromString("a4a4a4a4-1234-5678-90ab-cdef12345678")), -0.01, AttributeModifier.Operation.MULTIPLY_TOTAL);

            // 莫达非尼 (ModafinilFocusEffect)
            if (adsTime != null)
                ModEffects.MODAFINIL_FOCUS.get().addAttributeModifier(adsTime,
                        String.valueOf(UUID.fromString("c1c2c3d4-1111-2222-3333-aaaaaaaaaaaa")), -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
            for (int i = 0; i < inaccuracyAttrs.length; i++) {
                if (inaccuracyAttrs[i] != null)
                    ModEffects.MODAFINIL_FOCUS.get().addAttributeModifier(inaccuracyAttrs[i],
                            String.valueOf(UUID.fromString(new String[]{"c1c2c3d4-4444-5555-6666-dddddddddddd",
                                    "c1c2c3d4-5555-6666-7777-eeeeeeeeeeee",
                                    "c1c2c3d4-6666-7777-8888-ffffffffffff",
                                    "c1c2c3d4-7777-8888-9999-aaaaaaaaabbb",
                                    "c1c2c3d4-8888-9999-aaaa-bbbbbbbbbccc"}[i])),
                            -0.15, AttributeModifier.Operation.ADDITION);
            }

            // 肾上腺素 (EpinephrineBoostEffect)
            if (moveSpeedTaa != null)
                ModEffects.EPINEPHRINE_BOOST.get().addAttributeModifier(moveSpeedTaa,
                        String.valueOf(UUID.fromString("e1e2e3e4-2222-3333-4444-bbbbbbbbbbbb")), 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
            if (adsTime != null)
                ModEffects.EPINEPHRINE_BOOST.get().addAttributeModifier(adsTime,
                        String.valueOf(UUID.fromString("e1e2e3e4-4444-5555-6666-dddddddddddd")), -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
            for (int i = 0; i < inaccuracyAttrs.length; i++) {
                if (inaccuracyAttrs[i] != null)
                    ModEffects.EPINEPHRINE_BOOST.get().addAttributeModifier(inaccuracyAttrs[i],
                            String.valueOf(UUID.fromString(new String[]{"e1e2e3e4-7777-8888-9999-aaaaaaaaabbb",
                                    "e1e2e3e4-8888-9999-aaaa-bbbbbbbbbccc",
                                    "e1e2e3e4-9999-aaaa-bbbb-cccccccccddd",
                                    "e1e2e3e4-aaaa-bbbb-cccc-dddddddddddd",
                                    "e1e2e3e4-bbbb-cccc-dddd-eeeeeeeeeeee"}[i])),
                            -0.2, AttributeModifier.Operation.ADDITION);
            }
        }

        // ---- TACZ Attributes 模组 ----
        if (hasTACZAttr) {
            Attribute verticalRecoil = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("tacz_attributes", "ads_vertical_recoil"));
            Attribute horizontalRecoil = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("tacz_attributes", "ads_horizontal_recoil"));

            // 左臂创伤
            if (verticalRecoil != null)
                ModEffects.LEFT_ARM_TRAUMA.get().addAttributeModifier(verticalRecoil,
                        String.valueOf(UUID.fromString("a1b2c3d4-4444-5555-6666-dddddddddddd")), 0.2, AttributeModifier.Operation.ADDITION);
            if (horizontalRecoil != null)
                ModEffects.LEFT_ARM_TRAUMA.get().addAttributeModifier(horizontalRecoil,
                        String.valueOf(UUID.fromString("a1b2c3d4-5555-6666-7777-eeeeeeeeeeee")), 0.2, AttributeModifier.Operation.ADDITION);

            // 右臂创伤
            if (verticalRecoil != null)
                ModEffects.RIGHT_ARM_TRAUMA.get().addAttributeModifier(verticalRecoil,
                        String.valueOf(UUID.fromString("b1b2c3d4-4444-5555-6666-dddddddddddd")), 0.2, AttributeModifier.Operation.ADDITION);
            if (horizontalRecoil != null)
                ModEffects.RIGHT_ARM_TRAUMA.get().addAttributeModifier(horizontalRecoil,
                        String.valueOf(UUID.fromString("b1b2c3d4-5555-6666-7777-eeeeeeeeeeee")), 0.2, AttributeModifier.Operation.ADDITION);

            // 左臂骨折
            if (verticalRecoil != null)
                ModEffects.LEFT_ARM_FRACTURE.get().addAttributeModifier(verticalRecoil,
                        String.valueOf(UUID.fromString("a1b2c3d4-9999-aaaa-bbbb-cccccccccccc")), 0.2, AttributeModifier.Operation.ADDITION);
            if (horizontalRecoil != null)
                ModEffects.LEFT_ARM_FRACTURE.get().addAttributeModifier(horizontalRecoil,
                        String.valueOf(UUID.fromString("a1b2c3d4-aaaa-bbbb-cccc-dddddddddddd")), 0.2, AttributeModifier.Operation.ADDITION);

            // 右臂骨折
            if (verticalRecoil != null)
                ModEffects.RIGHT_ARM_FRACTURE.get().addAttributeModifier(verticalRecoil,
                        String.valueOf(UUID.fromString("c1c2c3d4-9999-aaaa-bbbb-cccccccccccc")), 0.2, AttributeModifier.Operation.ADDITION);
            if (horizontalRecoil != null)
                ModEffects.RIGHT_ARM_FRACTURE.get().addAttributeModifier(horizontalRecoil,
                        String.valueOf(UUID.fromString("c1c2c3d4-aaaa-bbbb-cccc-dddddddddddd")), 0.2, AttributeModifier.Operation.ADDITION);

            // 左臂固定
            if (verticalRecoil != null)
                ModEffects.LEFT_SPLINTED_ARM.get().addAttributeModifier(verticalRecoil,
                        String.valueOf(UUID.fromString("b1b2c3d4-9999-aaaa-bbbb-cccccccccccc")), 0.1, AttributeModifier.Operation.ADDITION);
            if (horizontalRecoil != null)
                ModEffects.LEFT_SPLINTED_ARM.get().addAttributeModifier(horizontalRecoil,
                        String.valueOf(UUID.fromString("b1b2c3d4-aaaa-bbbb-cccc-dddddddddddd")), 0.1, AttributeModifier.Operation.ADDITION);

            // 右臂固定
            if (verticalRecoil != null)
                ModEffects.RIGHT_SPLINTED_ARM.get().addAttributeModifier(verticalRecoil,
                        String.valueOf(UUID.fromString("d1d2d3d4-9999-aaaa-bbbb-cccccccccccc")), 0.1, AttributeModifier.Operation.ADDITION);
            if (horizontalRecoil != null)
                ModEffects.RIGHT_SPLINTED_ARM.get().addAttributeModifier(horizontalRecoil,
                        String.valueOf(UUID.fromString("d1d2d3d4-aaaa-bbbb-cccc-dddddddddddd")), 0.1, AttributeModifier.Operation.ADDITION);

            // 疼痛
            if (verticalRecoil != null)
                ModEffects.PAIN.get().addAttributeModifier(verticalRecoil,
                        String.valueOf(UUID.fromString("e1e2e3e4-4444-5555-6666-777777777777")), 1.0, AttributeModifier.Operation.ADDITION);
            if (horizontalRecoil != null)
                ModEffects.PAIN.get().addAttributeModifier(horizontalRecoil,
                        String.valueOf(UUID.fromString("e1e2e3e4-5555-6666-7777-888888888888")), 1.0, AttributeModifier.Operation.ADDITION);

            // 眩晕
            if (verticalRecoil != null)
                ModEffects.DIZZINESS.get().addAttributeModifier(verticalRecoil,
                        String.valueOf(UUID.fromString("f1f2f3f4-7777-8888-9999-dddddddddddd")), 1.0, AttributeModifier.Operation.ADDITION);
            if (horizontalRecoil != null)
                ModEffects.DIZZINESS.get().addAttributeModifier(horizontalRecoil,
                        String.valueOf(UUID.fromString("f1f2f3f4-8888-9999-aaaa-eeeeeeeeeeee")), 1.0, AttributeModifier.Operation.ADDITION);

            // 肾上腺素
            if (verticalRecoil != null)
                ModEffects.EPINEPHRINE_BOOST.get().addAttributeModifier(verticalRecoil,
                        String.valueOf(UUID.fromString("e1e2e3e4-5555-6666-7777-eeeeeeeeeeee")), -0.3, AttributeModifier.Operation.ADDITION);
            if (horizontalRecoil != null)
                ModEffects.EPINEPHRINE_BOOST.get().addAttributeModifier(horizontalRecoil,
                        String.valueOf(UUID.fromString("e1e2e3e4-6666-7777-8888-ffffffffffff")), -0.3, AttributeModifier.Operation.ADDITION);

            // 肌酸
            if (verticalRecoil != null)
                ModEffects.CREATINE_BOOST.get().addAttributeModifier(verticalRecoil,
                        String.valueOf(UUID.fromString("c1e2f3a4-4444-5555-6666-dddddddddddd")), -0.1, AttributeModifier.Operation.ADDITION);
            if (horizontalRecoil != null)
                ModEffects.CREATINE_BOOST.get().addAttributeModifier(horizontalRecoil,
                        String.valueOf(UUID.fromString("c1e2f3a4-5555-6666-7777-eeeeeeeeeeee")), -0.1, AttributeModifier.Operation.ADDITION);

            // 莫达非尼
            if (verticalRecoil != null)
                ModEffects.MODAFINIL_FOCUS.get().addAttributeModifier(verticalRecoil,
                        String.valueOf(UUID.fromString("c1c2c3d4-2222-3333-4444-bbbbbbbbbbbb")), -0.2, AttributeModifier.Operation.ADDITION);
            if (horizontalRecoil != null)
                ModEffects.MODAFINIL_FOCUS.get().addAttributeModifier(horizontalRecoil,
                        String.valueOf(UUID.fromString("c1c2c3d4-3333-4444-5555-cccccccccccc")), -0.2, AttributeModifier.Operation.ADDITION);
        }
    }

    /**
     * 新增：为内置耐力属性添加药水效果修饰符，替换 staminafortweakers 联动。
     * 所有 UUID 均为临时生成，确保唯一性。
     */
    private void registerStaminaModifiers() {
        // ----- 疲劳 (Fatigue) -----
        ModEffects.FATIGUE.get()
                .addAttributeModifier(ModAttributes.MAX_ARM_STAMINA.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("fatigue_max_arm".getBytes())),
                        -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.MAX_LEG_STAMINA.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("fatigue_max_leg".getBytes())),
                        -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.ARM_STAMINA_REGEN.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("fatigue_regen_arm".getBytes())),
                        -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.LEG_STAMINA_REGEN.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("fatigue_regen_leg".getBytes())),
                        -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // ----- 左腿骨折 (LeftLegFracture) -----
        ModEffects.LEFT_LEG_FRACTURE.get()
                .addAttributeModifier(ModAttributes.MAX_LEG_STAMINA.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("left_leg_frac_max".getBytes())),
                        -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.LEG_STAMINA_REGEN.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("left_leg_frac_regen".getBytes())),
                        -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // ----- 右腿骨折 (RightLegFracture) -----
        ModEffects.RIGHT_LEG_FRACTURE.get()
                .addAttributeModifier(ModAttributes.MAX_LEG_STAMINA.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("right_leg_frac_max".getBytes())),
                        -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.LEG_STAMINA_REGEN.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("right_leg_frac_regen".getBytes())),
                        -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // ----- 左腿创伤 (LeftLegTrauma) -----
        ModEffects.LEFT_LEG_TRAUMA.get()
                .addAttributeModifier(ModAttributes.MAX_LEG_STAMINA.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("left_leg_trauma_max".getBytes())),
                        -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.LEG_STAMINA_REGEN.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("left_leg_trauma_regen".getBytes())),
                        -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // ----- 右腿创伤 (RightLegTrauma) -----
        ModEffects.RIGHT_LEG_TRAUMA.get()
                .addAttributeModifier(ModAttributes.MAX_LEG_STAMINA.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("right_leg_trauma_max".getBytes())),
                        -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.LEG_STAMINA_REGEN.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("right_leg_trauma_regen".getBytes())),
                        -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // ----- 咖啡因 (CaffeineBoost) -----
        ModEffects.CAFFEINE_BOOST.get()
                .addAttributeModifier(ModAttributes.MAX_ARM_STAMINA.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("caffeine_max_arm".getBytes())),
                        0.1, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.MAX_LEG_STAMINA.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("caffeine_max_leg".getBytes())),
                        0.1, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.ARM_STAMINA_REGEN.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("caffeine_regen_arm".getBytes())),
                        0.2, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.LEG_STAMINA_REGEN.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("caffeine_regen_leg".getBytes())),
                        0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // ----- 肾上腺素 (EpinephrineBoost) -----
        ModEffects.EPINEPHRINE_BOOST.get()
                .addAttributeModifier(ModAttributes.ARM_STAMINA_REGEN.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("epi_regen_arm".getBytes())),
                        0.5, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.LEG_STAMINA_REGEN.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("epi_regen_leg".getBytes())),
                        0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // ----- 肌酸 (CreatineBoost) 侧重上肢 -----
        ModEffects.CREATINE_BOOST.get()
                .addAttributeModifier(ModAttributes.MAX_ARM_STAMINA.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("creatine_max_arm".getBytes())),
                        0.15, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.ARM_STAMINA_REGEN.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("creatine_regen_arm".getBytes())),
                        0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // ----- 莫达非尼 (ModafinilFocus) -----
        ModEffects.MODAFINIL_FOCUS.get()
                .addAttributeModifier(ModAttributes.ARM_STAMINA_REGEN.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("modafinil_regen_arm".getBytes())),
                        0.1, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.LEG_STAMINA_REGEN.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("modafinil_regen_leg".getBytes())),
                        0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    // ========== 针剂药水效果属性修饰符 ==========
    private void registerInjectorModifiers() {
        // VG-1 稳定剂：耐力恢复 +20%
        ModEffects.VG1_BOOST.get()
                .addAttributeModifier(ModAttributes.ARM_STAMINA_REGEN.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("vg1_arm_regen".getBytes())),
                        0.2, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.LEG_STAMINA_REGEN.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("vg1_leg_regen".getBytes())),
                        0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // VG-3 屏障：全身各部位 +5 天然护甲
        ModEffects.VG3_BOOST.get()
                .addAttributeModifier(ModAttributes.HEAD_NATURAL_ARMOR.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("vg3_head_armor".getBytes())),
                        5.0, AttributeModifier.Operation.ADDITION)
                .addAttributeModifier(ModAttributes.CHEST_NATURAL_ARMOR.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("vg3_chest_armor".getBytes())),
                        5.0, AttributeModifier.Operation.ADDITION)
                .addAttributeModifier(ModAttributes.STOMACH_NATURAL_ARMOR.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("vg3_stomach_armor".getBytes())),
                        5.0, AttributeModifier.Operation.ADDITION)
                .addAttributeModifier(ModAttributes.ARM_NATURAL_ARMOR.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("vg3_arm_armor".getBytes())),
                        5.0, AttributeModifier.Operation.ADDITION)
                .addAttributeModifier(ModAttributes.LEG_NATURAL_ARMOR.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("vg3_leg_armor".getBytes())),
                        5.0, AttributeModifier.Operation.ADDITION);

        // VG-6 哌甲酯：攻击力 +15%，攻击速度 +15%，最大手臂/腿部耐力 +20%
        ModEffects.VG6_METHYLPHENIDATE.get()
                .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                        String.valueOf(UUID.nameUUIDFromBytes("vg6_attack_damage".getBytes())),
                        0.15, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(Attributes.ATTACK_SPEED,
                        String.valueOf(UUID.nameUUIDFromBytes("vg6_attack_speed".getBytes())),
                        0.15, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.MAX_ARM_STAMINA.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("vg6_max_arm".getBytes())),
                        0.20, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.MAX_LEG_STAMINA.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("vg6_max_leg".getBytes())),
                        0.20, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // C-1 化合物正面增益 (C1_BUFF)：攻击力 +20%，攻击速度 +20%，移动速度 +20%，全身 +10 天然护甲
        ModEffects.C1_BUFF.get()
                .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                        String.valueOf(UUID.nameUUIDFromBytes("c1_attack_damage".getBytes())),
                        0.20, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(Attributes.ATTACK_SPEED,
                        String.valueOf(UUID.nameUUIDFromBytes("c1_attack_speed".getBytes())),
                        0.20, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                        String.valueOf(UUID.nameUUIDFromBytes("c1_movement_speed".getBytes())),
                        0.20, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.HEAD_NATURAL_ARMOR.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("c1_head_armor".getBytes())),
                        10.0, AttributeModifier.Operation.ADDITION)
                .addAttributeModifier(ModAttributes.CHEST_NATURAL_ARMOR.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("c1_chest_armor".getBytes())),
                        10.0, AttributeModifier.Operation.ADDITION)
                .addAttributeModifier(ModAttributes.STOMACH_NATURAL_ARMOR.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("c1_stomach_armor".getBytes())),
                        10.0, AttributeModifier.Operation.ADDITION)
                .addAttributeModifier(ModAttributes.ARM_NATURAL_ARMOR.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("c1_arm_armor".getBytes())),
                        10.0, AttributeModifier.Operation.ADDITION)
                .addAttributeModifier(ModAttributes.LEG_NATURAL_ARMOR.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("c1_leg_armor".getBytes())),
                        10.0, AttributeModifier.Operation.ADDITION);

        // 如果有 TAA 属性，为 C1_BUFF 添加后坐力控制、开镜速度、换弹速度等
        if (ModList.get().isLoaded("taa")) {
            Attribute adsTime = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("taa", "ads_time"));
            Attribute reloadSpeed = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("taa", "reload_speed"));
            Attribute inaccuracyAim = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("taa", "inaccuracy_aim"));
            if (adsTime != null)
                ModEffects.C1_BUFF.get().addAttributeModifier(adsTime,
                        String.valueOf(UUID.nameUUIDFromBytes("c1_ads_time".getBytes())),
                        -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
            if (reloadSpeed != null)
                ModEffects.C1_BUFF.get().addAttributeModifier(reloadSpeed,
                        String.valueOf(UUID.nameUUIDFromBytes("c1_reload_speed".getBytes())),
                        -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
            if (inaccuracyAim != null)
                ModEffects.C1_BUFF.get().addAttributeModifier(inaccuracyAim,
                        String.valueOf(UUID.nameUUIDFromBytes("c1_inaccuracy".getBytes())),
                        -0.2, AttributeModifier.Operation.ADDITION);
        }
        if (ModList.get().isLoaded("tacz_attributes")) {
            Attribute verticalRecoil = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("tacz_attributes", "ads_vertical_recoil"));
            Attribute horizontalRecoil = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("tacz_attributes", "ads_horizontal_recoil"));
            if (verticalRecoil != null)
                ModEffects.C1_BUFF.get().addAttributeModifier(verticalRecoil,
                        String.valueOf(UUID.nameUUIDFromBytes("c1_vertical_recoil".getBytes())),
                        -0.2, AttributeModifier.Operation.ADDITION);
            if (horizontalRecoil != null)
                ModEffects.C1_BUFF.get().addAttributeModifier(horizontalRecoil,
                        String.valueOf(UUID.nameUUIDFromBytes("c1_horizontal_recoil".getBytes())),
                        -0.2, AttributeModifier.Operation.ADDITION);
        }

        // C-1 副作用：耐力消耗 +25%
        ModEffects.C1_SIDE_EFFECT.get()
                .addAttributeModifier(ModAttributes.ARM_STAMINA_DRAIN_MULTIPLIER.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("c1_arm_drain".getBytes())),
                        0.25, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.LEG_STAMINA_DRAIN_MULTIPLIER.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("c1_leg_drain".getBytes())),
                        0.25, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // PH-18 神经稳定剂：后坐力控制 +30%，非瞄准散步 -30%，耐力消耗 -20%
        if (ModList.get().isLoaded("tacz_attributes")) {
            Attribute verticalRecoil = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("tacz_attributes", "ads_vertical_recoil"));
            Attribute horizontalRecoil = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("tacz_attributes", "ads_horizontal_recoil"));
            if (verticalRecoil != null)
                ModEffects.PH18_NEUROSTABIL.get().addAttributeModifier(verticalRecoil,
                        String.valueOf(UUID.nameUUIDFromBytes("ph18_vertical_recoil".getBytes())),
                        -0.3, AttributeModifier.Operation.ADDITION);
            if (horizontalRecoil != null)
                ModEffects.PH18_NEUROSTABIL.get().addAttributeModifier(horizontalRecoil,
                        String.valueOf(UUID.nameUUIDFromBytes("ph18_horizontal_recoil".getBytes())),
                        -0.3, AttributeModifier.Operation.ADDITION);
        }
        if (ModList.get().isLoaded("taa")) {
            Attribute inaccuracyAim = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("taa", "inaccuracy_aim"));
            Attribute inaccuracyMove = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("taa", "inaccuracy_move"));
            Attribute inaccuracyStand = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("taa", "inaccuracy_stand"));
            if (inaccuracyAim != null)
                ModEffects.PH18_NEUROSTABIL.get().addAttributeModifier(inaccuracyAim,
                        String.valueOf(UUID.nameUUIDFromBytes("ph18_inaccuracy_aim".getBytes())),
                        -0.3, AttributeModifier.Operation.ADDITION);
            if (inaccuracyMove != null)
                ModEffects.PH18_NEUROSTABIL.get().addAttributeModifier(inaccuracyMove,
                        String.valueOf(UUID.nameUUIDFromBytes("ph18_inaccuracy_move".getBytes())),
                        -0.3, AttributeModifier.Operation.ADDITION);
            if (inaccuracyStand != null)
                ModEffects.PH18_NEUROSTABIL.get().addAttributeModifier(inaccuracyStand,
                        String.valueOf(UUID.nameUUIDFromBytes("ph18_inaccuracy_stand".getBytes())),
                        -0.3, AttributeModifier.Operation.ADDITION);
        }
        // 耐力消耗 -20%
        ModEffects.PH18_NEUROSTABIL.get()
                .addAttributeModifier(ModAttributes.ARM_STAMINA_DRAIN_MULTIPLIER.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("ph18_arm_drain".getBytes())),
                        -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(ModAttributes.LEG_STAMINA_DRAIN_MULTIPLIER.get(),
                        String.valueOf(UUID.nameUUIDFromBytes("ph18_leg_drain".getBytes())),
                        -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

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

            registerEffectModifiers();
        });
    }

    private void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            event.add(type, ModAttributes.HEAD_NATURAL_ARMOR.get());
            event.add(type, ModAttributes.CHEST_NATURAL_ARMOR.get());
            event.add(type, ModAttributes.STOMACH_NATURAL_ARMOR.get());
            event.add(type, ModAttributes.ARM_NATURAL_ARMOR.get());
            event.add(type, ModAttributes.LEG_NATURAL_ARMOR.get());
            event.add(type, ModAttributes.PENETRATION.get());
            event.add(type, ModAttributes.HEAD_NATURAL_TOUGHNESS.get());
            event.add(type, ModAttributes.CHEST_NATURAL_TOUGHNESS.get());
            event.add(type, ModAttributes.STOMACH_NATURAL_TOUGHNESS.get());
            event.add(type, ModAttributes.ARM_NATURAL_TOUGHNESS.get());
            event.add(type, ModAttributes.LEG_NATURAL_TOUGHNESS.get());
            event.add(type, ModAttributes.MAX_ARM_STAMINA.get());
            event.add(type, ModAttributes.ARM_STAMINA_REGEN.get());
            event.add(type, ModAttributes.ARM_STAMINA_DRAIN_MULTIPLIER.get());
            event.add(type, ModAttributes.MAX_LEG_STAMINA.get());
            event.add(type, ModAttributes.LEG_STAMINA_REGEN.get());
            event.add(type, ModAttributes.LEG_STAMINA_DRAIN_MULTIPLIER.get());
            event.add(type, ModAttributes.ERGONOMICS.get());
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

        setAttribute(living, ModAttributes.HEAD_NATURAL_TOUGHNESS.get(), cfg.naturalToughness.get("head"));
        setAttribute(living, ModAttributes.CHEST_NATURAL_TOUGHNESS.get(), cfg.naturalToughness.get("chest"));
        setAttribute(living, ModAttributes.STOMACH_NATURAL_TOUGHNESS.get(), cfg.naturalToughness.get("stomach"));
        setAttribute(living, ModAttributes.ARM_NATURAL_TOUGHNESS.get(), cfg.naturalToughness.get("arm"));
        setAttribute(living, ModAttributes.LEG_NATURAL_TOUGHNESS.get(), cfg.naturalToughness.get("leg"));
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

            if (event.getObject().level().isClientSide) return;

            if (event.getObject() instanceof Player player) {
                // 硬核模式部位血量（仅在硬核模式下启用）
                if (config.damageModel == ModClothConfig.DamageModel.HARDCORE) {
                    event.addCapability(new ResourceLocation(ModernDamage.MODID, "part_health"), new PlayerPartHealthProvider(player));
                    ModernDamage.LOGGER.debug("附加玩家部位血量能力（服务端）");
                }

                // 手臂耐力（独立开关，不依赖硬核模式）
                if (config.enableArmStamina) {
                    event.addCapability(new ResourceLocation(ModernDamage.MODID, "arm_stamina"), new ArmStaminaProvider(player));
                    ModernDamage.LOGGER.debug("附加玩家手臂耐力能力（服务端）");
                }

                // 腿部耐力（独立开关，不依赖硬核模式）
                if (config.enableLegStamina) {
                    event.addCapability(new ResourceLocation(ModernDamage.MODID, "leg_stamina"), new LegStaminaProvider(player));
                    ModernDamage.LOGGER.debug("附加玩家腿部耐力能力（服务端）");
                }

            } else if (config.creaturePartHealthEnabled && config.damageModel == ModClothConfig.DamageModel.HARDCORE && event.getObject() instanceof LivingEntity living && !(living instanceof Player)) {
                event.addCapability(new ResourceLocation(ModernDamage.MODID, "creature_part_health"), new CreaturePartHealthProvider(living));
                ModernDamage.LOGGER.debug("附加生物部位血量能力（服务端）");
            }
        }
    }

    private void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;
        player.getCapability(PartHealthCapability.PART_HEALTH_CAP).ifPresent(IPartHealth::tick);
        player.getCapability(ArmStaminaCapability.ARM_STAMINA).ifPresent(IArmStamina::tick);
        player.getCapability(LegStaminaCapability.LEG_STAMINA).ifPresent(ILegStamina::tick);
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        player.getCapability(ArmStaminaCapability.ARM_STAMINA).ifPresent(IArmStamina::reset);
        player.getCapability(LegStaminaCapability.LEG_STAMINA).ifPresent(ILegStamina::reset);
        player.getCapability(PartHealthCapability.PART_HEALTH_CAP).ifPresent(cap -> {
            cap.reset();
            ModernDamage.LOGGER.debug("Player {} respawned, part health reset and synced.", player.getName().getString());
        });
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        // 同步手臂耐力
        player.getCapability(ArmStaminaCapability.ARM_STAMINA).ifPresent(stamina -> {
            if (stamina instanceof ArmStamina armStamina) {
                armStamina.sync();
            }
        });

        // 同步腿部耐力
        player.getCapability(LegStaminaCapability.LEG_STAMINA).ifPresent(stamina -> {
            if (stamina instanceof LegStamina legStamina) {
                legStamina.sync();
            }
        });
    }
}