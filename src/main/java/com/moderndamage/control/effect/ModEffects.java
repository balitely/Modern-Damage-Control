package com.moderndamage.control.effect;

import com.moderndamage.control.ModernDamage;
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

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}