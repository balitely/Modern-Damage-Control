package com.moderndamage.control.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;
import java.util.UUID;

public class RightArmTraumaEffect extends AbstractTraumaEffect {

    private static final Random RANDOM = new Random();
    private long lastPainTriggerTime = 0;

    private static final UUID MELEE_DAMAGE_UUID = UUID.fromString("b1b2c3d4-1111-2222-3333-aaaaaaaaaaaa");
    private static final UUID ADS_TIME_UUID = UUID.fromString("b1b2c3d4-2222-3333-4444-bbbbbbbbbbbb");
    private static final UUID VERTICAL_RECOIL_UUID = UUID.fromString("b1b2c3d4-4444-5555-6666-dddddddddddd");
    private static final UUID HORIZONTAL_RECOIL_UUID = UUID.fromString("b1b2c3d4-5555-6666-7777-eeeeeeeeeeee");
    private static final UUID INACCURACY_AIM_UUID = UUID.fromString("b1b2c3d4-6666-7777-8888-ffffffffffff");
    private static final UUID INACCURACY_LIE_UUID = UUID.fromString("b1b2c3d4-7777-8888-9999-aaaaaaaaabbb");
    private static final UUID INACCURACY_MOVE_UUID = UUID.fromString("b1b2c3d4-8888-9999-aaaa-bbbbbbbbbccc");
    private static final UUID INACCURACY_SNEAK_UUID = UUID.fromString("b1b2c3d4-9999-aaaa-bbbb-cccccccccddd");
    private static final UUID INACCURACY_STAND_UUID = UUID.fromString("b1b2c3d4-aaaa-bbbb-cccc-dddddddddddd");

    public RightArmTraumaEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (!(entity instanceof Player)) return;

        // 近战伤害 -20%
        var meleeAttr = Attributes.ATTACK_DAMAGE;
        var meleeInst = attributeMap.getInstance(meleeAttr);
        if (meleeInst != null) {
            meleeInst.addPermanentModifier(new AttributeModifier(MELEE_DAMAGE_UUID,
                    "right_arm_trauma_melee", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        boolean hasTAA = ModList.get().isLoaded("taa");
        boolean hasTACZAttr = ModList.get().isLoaded("tacz_attributes");

        if (hasTAA) {
            // 开镜速度 +30% (乘法)
            var adsTimeAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "ads_time"));
            if (adsTimeAttr != null && attributeMap.getInstance(adsTimeAttr) != null) {
                attributeMap.getInstance(adsTimeAttr).addPermanentModifier(new AttributeModifier(ADS_TIME_UUID,
                        "right_arm_trauma_ads_time", 0.3, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }

            // 五种不精准度 +0.15
            String[] inaccuracyTypes = {"inaccuracy_aim", "inaccuracy_lie", "inaccuracy_move", "inaccuracy_sneak", "inaccuracy_stand"};
            UUID[] uuids = {INACCURACY_AIM_UUID, INACCURACY_LIE_UUID, INACCURACY_MOVE_UUID, INACCURACY_SNEAK_UUID, INACCURACY_STAND_UUID};
            for (int i = 0; i < inaccuracyTypes.length; i++) {
                var attr = ForgeRegistries.ATTRIBUTES.getValue(
                        new net.minecraft.resources.ResourceLocation("taa", inaccuracyTypes[i]));
                if (attr != null && attributeMap.getInstance(attr) != null) {
                    attributeMap.getInstance(attr).addPermanentModifier(new AttributeModifier(uuids[i],
                            "right_arm_trauma_inaccuracy", 0.15, AttributeModifier.Operation.ADDITION));
                }
            }
        }

        if (hasTACZAttr) {
            // 垂直后坐力 +0.2
            var verticalAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_vertical_recoil"));
            if (verticalAttr != null && attributeMap.getInstance(verticalAttr) != null) {
                attributeMap.getInstance(verticalAttr).addPermanentModifier(new AttributeModifier(VERTICAL_RECOIL_UUID,
                        "right_arm_trauma_vertical_recoil", 0.2, AttributeModifier.Operation.ADDITION));
            }
            // 水平后坐力 +0.2
            var horizontalAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_horizontal_recoil"));
            if (horizontalAttr != null && attributeMap.getInstance(horizontalAttr) != null) {
                attributeMap.getInstance(horizontalAttr).addPermanentModifier(new AttributeModifier(HORIZONTAL_RECOIL_UUID,
                        "right_arm_trauma_horizontal_recoil", 0.2, AttributeModifier.Operation.ADDITION));
            }
        }

        super.addAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (!(entity instanceof Player)) return;

        var meleeAttr = Attributes.ATTACK_DAMAGE;
        var meleeInst = attributeMap.getInstance(meleeAttr);
        if (meleeInst != null) meleeInst.removeModifier(MELEE_DAMAGE_UUID);

        boolean hasTAA = ModList.get().isLoaded("taa");
        boolean hasTACZAttr = ModList.get().isLoaded("tacz_attributes");

        if (hasTAA) {
            var adsTimeAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "ads_time"));
            if (adsTimeAttr != null && attributeMap.getInstance(adsTimeAttr) != null) {
                attributeMap.getInstance(adsTimeAttr).removeModifier(ADS_TIME_UUID);
            }
            String[] inaccuracyTypes = {"inaccuracy_aim", "inaccuracy_lie", "inaccuracy_move", "inaccuracy_sneak", "inaccuracy_stand"};
            UUID[] uuids = {INACCURACY_AIM_UUID, INACCURACY_LIE_UUID, INACCURACY_MOVE_UUID, INACCURACY_SNEAK_UUID, INACCURACY_STAND_UUID};
            for (int i = 0; i < inaccuracyTypes.length; i++) {
                var attr = ForgeRegistries.ATTRIBUTES.getValue(
                        new net.minecraft.resources.ResourceLocation("taa", inaccuracyTypes[i]));
                if (attr != null && attributeMap.getInstance(attr) != null) {
                    attributeMap.getInstance(attr).removeModifier(uuids[i]);
                }
            }
        }

        if (hasTACZAttr) {
            var verticalAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_vertical_recoil"));
            if (verticalAttr != null && attributeMap.getInstance(verticalAttr) != null) {
                attributeMap.getInstance(verticalAttr).removeModifier(VERTICAL_RECOIL_UUID);
            }
            var horizontalAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_horizontal_recoil"));
            if (horizontalAttr != null && attributeMap.getInstance(horizontalAttr) != null) {
                attributeMap.getInstance(horizontalAttr).removeModifier(HORIZONTAL_RECOIL_UUID);
            }
        }

        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (entity.level().isClientSide) return;
        if (!(entity instanceof Player player)) return;

        long currentTick = player.tickCount;
        if (currentTick - lastPainTriggerTime < 200) return;

        if (RANDOM.nextDouble() < 0.00333) {
            lastPainTriggerTime = currentTick;
            int duration = 40 + RANDOM.nextInt(41);
            player.addEffect(new MobEffectInstance(ModEffects.PAIN.get(), duration, 0));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}