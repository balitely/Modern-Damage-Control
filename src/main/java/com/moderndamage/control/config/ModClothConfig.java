package com.moderndamage.control.config;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.api.ModDamagePart;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@Config(name = ModernDamage.MODID)
public class ModClothConfig implements ConfigData {

    public enum DamageModel {
        SOFT,
        HARDCORE
    }

    // ========== 通用调试 ==========
    @ConfigEntry.Gui.Tooltip
    public boolean debugMode = false;

    @ConfigEntry.Gui.Tooltip
    public DamageModel damageModel = DamageModel.SOFT;

    // ========== 出血系统配置 ==========
    @ConfigEntry.Gui.Tooltip
    public int minorBleedingIntervalTicks = 100;

    @ConfigEntry.Gui.Tooltip
    public float minorBleedingDamagePerLevel = 1f;

    @ConfigEntry.Gui.Tooltip
    public int majorBleedingIntervalTicks = 50;

    @ConfigEntry.Gui.Tooltip
    public float majorBleedingDamagePerLevel = 1f;

    // ========== 凝血增强配置 ==========
    @ConfigEntry.Gui.Tooltip
    public int coagulationClearIntervalTicks = 200;

    // ========== 严重创伤配置 ==========
    @ConfigEntry.Gui.Tooltip
    public int traumaDamageIntervalTicks = 80;

    @ConfigEntry.Gui.Tooltip
    public float traumaDamagePerTick = 1f;

    // ========== 骨折自然愈合时间（秒） ==========
    @ConfigEntry.Gui.Tooltip
    public int splintedHealTimeSeconds = 300;

    // ========== 输血效果治疗量缩放 ==========
    @ConfigEntry.Gui.Tooltip
    public float ivFluidHealMultiplier = 1.0f;

    // ========== 钝伤与穿透相关配置 ==========
    @ConfigEntry.Gui.Tooltip
    public float bluntDamageRatio = 0.2f;

    @ConfigEntry.Gui.Tooltip
    public float bluntDamageHalveDistance = 15.0f;

    @ConfigEntry.Gui.Tooltip
    public float bluntDamageMinRatio = 0.08f;

    // ========== 防护能力系统相关配置 ==========

    @ConfigEntry.Gui.Tooltip
    public float armorStackingFactor = 0.2f;

    @ConfigEntry.Gui.Tooltip
    public int armorCap = 0;

    // ========== 耐久损耗配置 ==========
    @ConfigEntry.Gui.Tooltip
    public int durabilityLossBase = 1;

    @ConfigEntry.Gui.Tooltip
    public float durabilityLossExtraPerPenetration = 0.2f;

    @ConfigEntry.Gui.Tooltip
    public int maxDurabilityLoss = 5;

    // ========== 玩家额外最大生命值==========
    @ConfigEntry.Gui.Tooltip
    public double playerMaxHealthBonus = 0;

    // ========== 其他实体的额外最大生命值 ==========
    @ConfigEntry.Gui.Tooltip
    public Map<String, Double> entityMaxHealthBonus = new HashMap<>();

    // ========== 部位药水效果配置（玩家） ==========
    @ConfigEntry.Gui.CollapsibleObject
    @ConfigEntry.Gui.Tooltip
    public Map<String, BodyPartConfig> bodyParts = new HashMap<>();

    // ========== HUD 位置配置 ==========
    @ConfigEntry.Gui.Tooltip
    public boolean enablePartHealthHUD = false;

    @ConfigEntry.Gui.Tooltip
    public int partHealthHUDX = 10;

    @ConfigEntry.Gui.Tooltip
    public int partHealthHUDY = 50;

    @ConfigEntry.Gui.Tooltip
    public float partHealthHUDPartScale = 3.0f;

    // ========== 硬核模式配置 ==========
    @ConfigEntry.Gui.Tooltip
    public boolean creaturePartHealthEnabled = true;

    @ConfigEntry.Gui.Tooltip
    public float creatureHeadRatio = 0.20f;

    @ConfigEntry.Gui.Tooltip
    public float creatureChestRatio = 0.38f;

    @ConfigEntry.Gui.Tooltip
    public float creatureStomachRatio = 0.20f;

    @ConfigEntry.Gui.Tooltip
    public float creatureLeftArmRatio = 0.01f;

    @ConfigEntry.Gui.Tooltip
    public float creatureRightArmRatio = 0.01f;

    @ConfigEntry.Gui.Tooltip
    public float creatureLeftLegRatio = 0.10f;

    @ConfigEntry.Gui.Tooltip
    public float creatureRightLegRatio = 0.10f;

    @ConfigEntry.Gui.Tooltip
    public float armOverflowToChest = 1.0f;

    @ConfigEntry.Gui.Tooltip
    public float legOverflowToChest = 1.0f;

    // ========== 玩家部位血量比例 ==========
    @ConfigEntry.Gui.CollapsibleObject
    @ConfigEntry.Gui.Tooltip
    public Map<String, Float> playerPartHealthRatios = new HashMap<>();

    // ========== 非玩家实体生物效果配置 ==========
    @ConfigEntry.Gui.CollapsibleObject
    @ConfigEntry.Gui.Tooltip
    public Map<String, EntityEffectConfig> entityEffects = new HashMap<>();

    @ConfigEntry.Gui.CollapsibleObject
    @ConfigEntry.Gui.Tooltip
    public Map<String, CreaturePartRatios> entityPartRatios = new HashMap<>();

    public static class BodyPartConfig {
        public List<EffectEntry> generic = new ArrayList<>();
        public List<EffectEntry> destroy = new ArrayList<>();
    }

    public static class CreaturePartRatios {
        @ConfigEntry.Gui.Tooltip
        public float head = 0.20f;
        @ConfigEntry.Gui.Tooltip
        public float chest = 0.38f;
        @ConfigEntry.Gui.Tooltip
        public float stomach = 0.20f;
        @ConfigEntry.Gui.Tooltip
        public float leftArm = 0.01f;
        @ConfigEntry.Gui.Tooltip
        public float rightArm = 0.01f;
        @ConfigEntry.Gui.Tooltip
        public float leftLeg = 0.10f;
        @ConfigEntry.Gui.Tooltip
        public float rightLeg = 0.10f;
    }

    public static void register() {
        AutoConfig.register(ModClothConfig.class, JanksonConfigSerializer::new);
        ModClothConfig config = get();
        if (config.bodyParts.isEmpty() || config.playerPartHealthRatios.isEmpty() || config.entityPartRatios.isEmpty()) {
            config.setDefaultConfig();
            AutoConfig.getConfigHolder(ModClothConfig.class).save();
        }
    }

    public static ModClothConfig get() {
        return AutoConfig.getConfigHolder(ModClothConfig.class).getConfig();
    }

    public static List<EffectEntry> getEffects(ModDamagePart part) {
        ModClothConfig config = get();
        String key = part.getConfigKey();
        BodyPartConfig partConfig = config.bodyParts.get(key);
        if (partConfig == null) return Collections.emptyList();
        return partConfig.generic;
    }

    public static float getPartInitialHealth(ModDamagePart part) {
        ModClothConfig config = get();
        String key = part.getConfigKey();
        return config.playerPartHealthRatios.getOrDefault(key, 50f);
    }

    public static EntityEffectConfig getEntityEffectConfig(EntityType<?> entityType) {
        ModClothConfig config = get();
        String key = ForgeRegistries.ENTITY_TYPES.getKey(entityType).toString();
        return config.entityEffects.getOrDefault(key, new EntityEffectConfig());
    }

    private void setDefaultConfig() {

        // ========== 头部 ==========
        BodyPartConfig head = new BodyPartConfig();
        head.generic.add(new EffectEntry("moderndamage:dizziness", 20, 0, 10.0f, 1.0));
        bodyParts.put("head", head);

        // ========== 胸部 ==========
        BodyPartConfig chest = new BodyPartConfig();
        chest.generic.add(new EffectEntry("moderndamage:minor_bleeding", -1, 0, 8.0f, 0.3));
        bodyParts.put("chest", chest);

        // ========== 胃部 ==========
        BodyPartConfig stomach = new BodyPartConfig();
        stomach.generic.add(new EffectEntry("minecraft:nausea", 100, 0, 10.0f, 1.0));
        stomach.generic.add(new EffectEntry("moderndamage:major_bleeding", -1, 0, 12.0f, 0.2));
        stomach.generic.add(new EffectEntry("moderndamage:minor_bleeding", -1, 0, 8.0f, 0.3));
        stomach.destroy.add(new EffectEntry("moderndamage:stomach_trauma", -1, 0, 0, 1.0));
        bodyParts.put("stomach", stomach);

        // ========== 左臂 ==========
        BodyPartConfig leftArm = new BodyPartConfig();
        leftArm.generic.add(new EffectEntry("moderndamage:left_arm_fracture", -1, 0, 8.0f, 1.0));
        leftArm.generic.add(new EffectEntry("moderndamage:major_bleeding", -1, 0, 12.0f, 0.2));
        leftArm.generic.add(new EffectEntry("moderndamage:minor_bleeding", -1, 0, 8.0f, 0.3));
        leftArm.destroy.add(new EffectEntry("moderndamage:left_arm_trauma", -1, 0, 0, 1.0));
        bodyParts.put("left_arm", leftArm);

        // ========== 右臂 ==========
        BodyPartConfig rightArm = new BodyPartConfig();
        rightArm.generic.add(new EffectEntry("moderndamage:right_arm_fracture", -1, 0, 8.0f, 1.0));
        rightArm.generic.add(new EffectEntry("moderndamage:major_bleeding", -1, 0, 12.0f, 0.2));
        rightArm.generic.add(new EffectEntry("moderndamage:minor_bleeding", -1, 0, 8.0f, 0.3));
        rightArm.destroy.add(new EffectEntry("moderndamage:right_arm_trauma", -1, 0, 0, 1.0));
        bodyParts.put("right_arm", rightArm);

        // ========== 左腿 ==========
        BodyPartConfig leftLeg = new BodyPartConfig();
        leftLeg.generic.add(new EffectEntry("moderndamage:left_leg_fracture", -1, 0, 8.0f, 1.0));
        leftLeg.generic.add(new EffectEntry("moderndamage:major_bleeding", -1, 0, 12.0f, 0.2));
        leftLeg.generic.add(new EffectEntry("moderndamage:minor_bleeding", -1, 0, 8.0f, 0.3));
        leftLeg.destroy.add(new EffectEntry("moderndamage:left_leg_trauma", -1, 0, 0, 1.0));
        bodyParts.put("left_leg", leftLeg);

        // ========== 右腿 ==========
        BodyPartConfig rightLeg = new BodyPartConfig();
        rightLeg.generic.add(new EffectEntry("moderndamage:right_leg_fracture", -1, 0, 8.0f, 1.0));
        rightLeg.generic.add(new EffectEntry("moderndamage:major_bleeding", -1, 0, 12.0f, 0.2));
        rightLeg.generic.add(new EffectEntry("moderndamage:minor_bleeding", -1, 0, 8.0f, 0.3));
        rightLeg.destroy.add(new EffectEntry("moderndamage:right_leg_trauma", -1, 0, 0, 1.0));
        bodyParts.put("right_leg", rightLeg);

        EntityBodyPartEffectConfig legDestroy = new EntityBodyPartEffectConfig();
        legDestroy.destroy.add(new EffectEntry("minecraft:slowness", 200, 2, 0, 1.0));
        legDestroy.enabled = true;

        EntityBodyPartEffectConfig legHurt = new EntityBodyPartEffectConfig();
        legHurt.thresholdPercent = 0.2f;
        legHurt.effects.add(new EffectEntry("minecraft:slowness", 100, 0, 0, 0.8));
        legHurt.enabled = true;

        EntityBodyPartEffectConfig headHurt = new EntityBodyPartEffectConfig();
        headHurt.thresholdPercent = 0.15f;
        headHurt.effects.add(new EffectEntry("minecraft:blindness", 60, 0, 0, 0.5));
        headHurt.effects.add(new EffectEntry("minecraft:nausea", 80, 0, 0, 0.6));
        headHurt.enabled = true;

        EntityEffectConfig zombieEffects = new EntityEffectConfig();
        zombieEffects.bodyParts.put("left_leg", legHurt);
        zombieEffects.bodyParts.put("right_leg", legHurt);

        EntityBodyPartEffectConfig zombieLegDestroy = new EntityBodyPartEffectConfig();
        zombieLegDestroy.destroy.add(new EffectEntry("minecraft:slowness", 200, 2, 0, 1.0));
        zombieEffects.bodyParts.put("left_leg", zombieLegDestroy);
        zombieEffects.bodyParts.put("right_leg", zombieLegDestroy);

        zombieEffects.bodyParts.put("head", headHurt);
        entityEffects.put("minecraft:zombie", zombieEffects);

        EntityEffectConfig skeletonEffects = new EntityEffectConfig();
        skeletonEffects.bodyParts.put("left_leg", legHurt);
        skeletonEffects.bodyParts.put("right_leg", legHurt);
        skeletonEffects.bodyParts.put("left_leg", zombieLegDestroy); // 摧毁
        skeletonEffects.bodyParts.put("right_leg", zombieLegDestroy);
        skeletonEffects.bodyParts.put("head", headHurt);
        entityEffects.put("minecraft:skeleton", skeletonEffects);

        EntityEffectConfig spiderEffects = new EntityEffectConfig();
        EntityBodyPartEffectConfig spiderLegHurt = new EntityBodyPartEffectConfig();
        spiderLegHurt.thresholdPercent = 0.1f;
        spiderLegHurt.effects.add(new EffectEntry("minecraft:slowness", 120, 1, 0, 0.9));
        spiderEffects.bodyParts.put("left_leg", spiderLegHurt);
        spiderEffects.bodyParts.put("right_leg", spiderLegHurt);

        spiderEffects.bodyParts.put("left_leg", zombieLegDestroy);
        spiderEffects.bodyParts.put("right_leg", zombieLegDestroy);
        entityEffects.put("minecraft:spider", spiderEffects);

        playerPartHealthRatios.put("head", 0.15f);
        playerPartHealthRatios.put("chest", 0.24f);
        playerPartHealthRatios.put("stomach", 0.17f);
        playerPartHealthRatios.put("left_arm", 0.10f);
        playerPartHealthRatios.put("right_arm", 0.10f);
        playerPartHealthRatios.put("left_leg", 0.12f);
        playerPartHealthRatios.put("right_leg", 0.12f);

        playerMaxHealthBonus = 0;
        entityMaxHealthBonus.put("minecraft:zombie", 10.0);
        entityMaxHealthBonus.put("minecraft:skeleton", 5.0);

        CreaturePartRatios zombieRatios = new CreaturePartRatios();
        zombieRatios.head = 0.25f;
        zombieRatios.chest = 0.35f;
        zombieRatios.stomach = 0.15f;
        zombieRatios.leftArm = 0.05f;
        zombieRatios.rightArm = 0.05f;
        zombieRatios.leftLeg = 0.10f;
        zombieRatios.rightLeg = 0.05f;
        entityPartRatios.put("minecraft:zombie", zombieRatios);
    }

    public static float getPlayerPartRatio(ModDamagePart part) {
        ModClothConfig config = get();
        String key = part.getConfigKey();
        return config.playerPartHealthRatios.getOrDefault(key, 0.12f);
    }
}