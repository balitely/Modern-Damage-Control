package com.moderndamage.control.effect;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ModernDamage.MODID);

    // 出血
    public static final RegistryObject<MobEffect> MINOR_BLEEDING = EFFECTS.register("minor_bleeding",
            () -> new MinorBleedingEffect(MobEffectCategory.HARMFUL, 0xB22222));
    public static final RegistryObject<MobEffect> MAJOR_BLEEDING = EFFECTS.register("major_bleeding",
            () -> new MajorBleedingEffect(MobEffectCategory.HARMFUL, 0x8B0000));

    // 骨折
    public static final RegistryObject<MobEffect> LEFT_ARM_FRACTURE = EFFECTS.register("left_arm_fracture",
            () -> new LeftArmFractureEffect(MobEffectCategory.HARMFUL, 0xCD853F));
    public static final RegistryObject<MobEffect> RIGHT_ARM_FRACTURE = EFFECTS.register("right_arm_fracture",
            () -> new RightArmFractureEffect(MobEffectCategory.HARMFUL, 0xCD853F));
    public static final RegistryObject<MobEffect> LEFT_LEG_FRACTURE = EFFECTS.register("left_leg_fracture",
            () -> new LeftLegFractureEffect(MobEffectCategory.HARMFUL, 0xCD853F));
    public static final RegistryObject<MobEffect> RIGHT_LEG_FRACTURE = EFFECTS.register("right_leg_fracture",
            () -> new RightLegFractureEffect(MobEffectCategory.HARMFUL, 0xCD853F));

    // 固定骨折
    public static final RegistryObject<MobEffect> LEFT_SPLINTED_ARM = EFFECTS.register("left_splinted_arm",
            () -> new LeftSplintedArmEffect(MobEffectCategory.HARMFUL, 0xDEB887));
    public static final RegistryObject<MobEffect> RIGHT_SPLINTED_ARM = EFFECTS.register("right_splinted_arm",
            () -> new RightSplintedArmEffect(MobEffectCategory.HARMFUL, 0xDEB887));
    public static final RegistryObject<MobEffect> LEFT_SPLINTED_LEG = EFFECTS.register("left_splinted_leg",
            () -> new LeftSplintedLegEffect(MobEffectCategory.HARMFUL, 0xDEB887));
    public static final RegistryObject<MobEffect> RIGHT_SPLINTED_LEG = EFFECTS.register("right_splinted_leg",
            () -> new RightSplintedLegEffect(MobEffectCategory.HARMFUL, 0xDEB887));

    // 严重创伤
    public static final RegistryObject<MobEffect> LEFT_ARM_TRAUMA = EFFECTS.register("left_arm_trauma",
            () -> new LeftArmTraumaEffect(MobEffectCategory.HARMFUL, 0x8B4513));
    public static final RegistryObject<MobEffect> RIGHT_ARM_TRAUMA = EFFECTS.register("right_arm_trauma",
            () -> new RightArmTraumaEffect(MobEffectCategory.HARMFUL, 0x8B4513));
    public static final RegistryObject<MobEffect> LEFT_LEG_TRAUMA = EFFECTS.register("left_leg_trauma",
            () -> new LeftLegTraumaEffect(MobEffectCategory.HARMFUL, 0x8B4513));
    public static final RegistryObject<MobEffect> RIGHT_LEG_TRAUMA = EFFECTS.register("right_leg_trauma",
            () -> new RightLegTraumaEffect(MobEffectCategory.HARMFUL, 0x8B4513));
    public static final RegistryObject<MobEffect> STOMACH_TRAUMA = EFFECTS.register("stomach_trauma",
            () -> new StomachTraumaEffect(MobEffectCategory.HARMFUL, 0x556B2F));

    // 其他负面效果
    public static final RegistryObject<MobEffect> FATIGUE = EFFECTS.register("fatigue",
            () -> new FatigueEffect(MobEffectCategory.HARMFUL, 0x696969));
    public static final RegistryObject<MobEffect> PAIN = EFFECTS.register("pain",
            () -> new PainEffect(MobEffectCategory.HARMFUL, 0x2F2F2F));
    public static final RegistryObject<MobEffect> DIZZINESS = EFFECTS.register("dizziness",
            () -> new DizzinessEffect(MobEffectCategory.HARMFUL, 0x1E1E1E));

    // 正面效果
    public static final RegistryObject<MobEffect> IV_FLUID = EFFECTS.register("iv_fluid",
            () -> new IvFluidEffect(MobEffectCategory.BENEFICIAL, 0x88AAFF));

    public static final RegistryObject<MobEffect> PAIN_SUPPRESSION = EFFECTS.register("pain_suppression",
            () -> new PainSuppressionEffect(MobEffectCategory.BENEFICIAL, 0x9ACD32));
    public static final RegistryObject<MobEffect> COAGULATION_BOOST = EFFECTS.register("coagulation_boost",
            () -> new CoagulationBoostEffect(MobEffectCategory.BENEFICIAL, 0x4682B4));
    public static final RegistryObject<MobEffect> INFECTION_RESISTANCE = EFFECTS.register("infection_resistance",
            () -> new InfectionResistanceEffect(MobEffectCategory.BENEFICIAL, 0x32CD32));
    public static final RegistryObject<MobEffect> DIZZINESS_RESISTANCE = EFFECTS.register("dizziness_resistance",
            () -> new DizzinessResistanceEffect(MobEffectCategory.BENEFICIAL, 0x32CD32));
    public static final RegistryObject<MobEffect> POISON_RESISTANCE = EFFECTS.register("poison_resistance",
            () -> new PoisonResistanceEffect(MobEffectCategory.BENEFICIAL, 0x32CD32));

    // 兴奋剂效果
    public static final RegistryObject<MobEffect> CAFFEINE_BOOST = EFFECTS.register("caffeine_boost",
            () -> new CaffeineBoostEffect(MobEffectCategory.BENEFICIAL, 0xD2B48C));
    public static final RegistryObject<MobEffect> MODAFINIL_FOCUS = EFFECTS.register("modafinil_focus",
            () -> new ModafinilFocusEffect(MobEffectCategory.BENEFICIAL, 0x7B68EE));
    public static final RegistryObject<MobEffect> EPINEPHRINE_BOOST = EFFECTS.register("epinephrine_boost",
            () -> new EpinephrineBoostEffect(MobEffectCategory.BENEFICIAL, 0xFF4500));
    public static final RegistryObject<MobEffect> CREATINE_BOOST = EFFECTS.register("creatine_boost",
            () -> new CreatineBoostEffect(MobEffectCategory.BENEFICIAL, 0xE67E22));

    // 针剂效果
    // VG-1 稳定剂（耐力恢复+20%）
    public static final RegistryObject<MobEffect> VG1_BOOST = EFFECTS.register("vg1_boost",
            () -> new VG1BoostEffect(MobEffectCategory.BENEFICIAL, 0x66CCFF));

    // VG-3 屏障（全身+5天然护甲）
    public static final RegistryObject<MobEffect> VG3_BOOST = EFFECTS.register("vg3_boost",
            () -> new VG3BoostEffect(MobEffectCategory.BENEFICIAL, 0xFFAA66));

    // VG-5 凯拉多再生
    public static final RegistryObject<MobEffect> VG5_REGENERATION = EFFECTS.register("vg5_regeneration",
            () -> new VG5RegenerationEffect(MobEffectCategory.BENEFICIAL, 0xFF5555));

    // VG-6 哌甲酯（止痛+属性）
    public static final RegistryObject<MobEffect> VG6_METHYLPHENIDATE = EFFECTS.register("vg6_methylphenidate",
            () -> new VG6MethylphenidateEffect(MobEffectCategory.BENEFICIAL, 0x99FF99));

    // VG-2 法莫辛（免疫恶心+饥饿+饱食度）
    public static final RegistryObject<MobEffect> FAMEXIN_RESISTANCE = EFFECTS.register("famexin_resistance",
            () -> new FamexinEffect(MobEffectCategory.BENEFICIAL, 0xFFAA33));

    // PH-5 Newtype人造血再生
    public static final RegistryObject<MobEffect> PH5_REGENERATION = EFFECTS.register("ph5_regeneration",
            () -> new PH5RegenerationEffect(MobEffectCategory.BENEFICIAL, 0xAA66FF));

    // PH-6 Vitalis 再生
    public static final RegistryObject<MobEffect> PH6_REGENERATION = EFFECTS.register("ph6_regeneration",
            () -> new PH6RegenerationEffect(MobEffectCategory.BENEFICIAL, 0x66CCFF));

    // PH-11 C-1 化合物再生（5秒）
    public static final RegistryObject<MobEffect> PH11_REGENERATION = EFFECTS.register("ph11_regeneration",
            () -> new PH11RegenerationEffect(MobEffectCategory.BENEFICIAL, 0xFF66FF));

    // PH-11 C-1 化合物正面增益
    public static final RegistryObject<MobEffect> C1_BUFF = EFFECTS.register("c1_buff",
            () -> new C1BuffEffect(MobEffectCategory.BENEFICIAL, 0xDDA0DD));

    // PH-11 C-1 化合物副作用（周期性扣血）
    public static final RegistryObject<MobEffect> C1_SIDE_EFFECT = EFFECTS.register("c1_side_effect",
            () -> new C1SideEffectEffect(MobEffectCategory.HARMFUL, 0x8B0000));

    // PH-16 泽诺戴恩再生
    public static final RegistryObject<MobEffect> PH16_REGENERATION = EFFECTS.register("ph16_regeneration",
            () -> new PH16RegenerationEffect(MobEffectCategory.BENEFICIAL, 0x66FF66));

    // PH-16 自动治愈效果（每30秒触发）
    public static final RegistryObject<MobEffect> PH16_HEAL_EFFECT = EFFECTS.register("ph16_heal_effect",
            () -> new PH16HealEffect(MobEffectCategory.BENEFICIAL, 0x88FF88));

    // PH-18 神经稳定剂（属性增益）
    public static final RegistryObject<MobEffect> PH18_NEUROSTABIL = EFFECTS.register("ph18_neurostabil",
            () -> new PH18NeurostabilEffect(MobEffectCategory.BENEFICIAL, 0xAAAAFF));

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}