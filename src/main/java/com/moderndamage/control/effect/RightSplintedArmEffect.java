package com.moderndamage.control.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class RightSplintedArmEffect extends AbstractFractureEffect {

    private static final UUID MELEE_DAMAGE_UUID = UUID.fromString("d1d2d3d4-8888-9999-aaaa-bbbbbbbbbbbb");
    private static final UUID VERTICAL_RECOIL_UUID = UUID.fromString("d1d2d3d4-9999-aaaa-bbbb-cccccccccccc");
    private static final UUID HORIZONTAL_RECOIL_UUID = UUID.fromString("d1d2d3d4-aaaa-bbbb-cccc-dddddddddddd");
    private static final UUID ADS_TIME_UUID = UUID.fromString("d1d2d3d4-3333-4444-5555-666666666666");
    private static final UUID INACCURACY_AIM_UUID = UUID.fromString("d1d2d3d4-6666-7777-8888-999999999999");
    private static final UUID INACCURACY_LIE_UUID = UUID.fromString("d1d2d3d4-7777-8888-9999-aaaaaaaaaaaa");
    private static final UUID INACCURACY_MOVE_UUID = UUID.fromString("d1d2d3d4-8888-9999-aaaa-bbbbbbbbbbbb");
    private static final UUID INACCURACY_SNEAK_UUID = UUID.fromString("d1d2d3d4-9999-aaaa-bbbb-cccccccccccc");
    private static final UUID INACCURACY_STAND_UUID = UUID.fromString("d1d2d3d4-aaaa-bbbb-cccc-dddddddddddd");

    public RightSplintedArmEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        var damageAttr = Attributes.ATTACK_DAMAGE;
        var damageInstance = attributeMap.getInstance(damageAttr);
        if (damageInstance != null) {
            damageInstance.addPermanentModifier(new AttributeModifier(MELEE_DAMAGE_UUID,
                    "right_splinted_arm_melee", -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        if (entity instanceof Player) {
            boolean hasTAA = ModList.get().isLoaded("taa");
            boolean hasTACZAttr = ModList.get().isLoaded("tacz_attributes");

            if (hasTACZAttr) {
                var verticalRecoilAttr = ForgeRegistries.ATTRIBUTES.getValue(new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_vertical_recoil"));
                if (verticalRecoilAttr != null && attributeMap.getInstance(verticalRecoilAttr) != null) {
                    attributeMap.getInstance(verticalRecoilAttr).addPermanentModifier(new AttributeModifier(VERTICAL_RECOIL_UUID,
                            "right_splinted_arm_vertical_recoil", 0.1, AttributeModifier.Operation.ADDITION));
                }
                var horizontalRecoilAttr = ForgeRegistries.ATTRIBUTES.getValue(new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_horizontal_recoil"));
                if (horizontalRecoilAttr != null && attributeMap.getInstance(horizontalRecoilAttr) != null) {
                    attributeMap.getInstance(horizontalRecoilAttr).addPermanentModifier(new AttributeModifier(HORIZONTAL_RECOIL_UUID,
                            "right_splinted_arm_horizontal_recoil", 0.1, AttributeModifier.Operation.ADDITION));
                }
            }

            if (hasTAA) {
                var adsTimeAttr = ForgeRegistries.ATTRIBUTES.getValue(new net.minecraft.resources.ResourceLocation("taa", "ads_time"));
                if (adsTimeAttr != null && attributeMap.getInstance(adsTimeAttr) != null) {
                    attributeMap.getInstance(adsTimeAttr).addPermanentModifier(new AttributeModifier(ADS_TIME_UUID,
                            "right_splinted_arm_ads_time", 0.1, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                String[] inaccuracyTypes = {"inaccuracy_aim", "inaccuracy_lie", "inaccuracy_move", "inaccuracy_sneak", "inaccuracy_stand"};
                UUID[] uuids = {INACCURACY_AIM_UUID, INACCURACY_LIE_UUID, INACCURACY_MOVE_UUID, INACCURACY_SNEAK_UUID, INACCURACY_STAND_UUID};
                double value = 0.075;
                for (int i = 0; i < inaccuracyTypes.length; i++) {
                    var attr = ForgeRegistries.ATTRIBUTES.getValue(new net.minecraft.resources.ResourceLocation("taa", inaccuracyTypes[i]));
                    if (attr != null && attributeMap.getInstance(attr) != null) {
                        attributeMap.getInstance(attr).addPermanentModifier(new AttributeModifier(uuids[i],
                                "right_splinted_arm_inaccuracy", value, AttributeModifier.Operation.ADDITION));
                    }
                }
            }
        }

        super.addAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        var damageAttr = Attributes.ATTACK_DAMAGE;
        var damageInstance = attributeMap.getInstance(damageAttr);
        if (damageInstance != null) {
            damageInstance.removeModifier(MELEE_DAMAGE_UUID);
        }

        if (entity instanceof Player) {
            boolean hasTAA = ModList.get().isLoaded("taa");
            boolean hasTACZAttr = ModList.get().isLoaded("tacz_attributes");

            if (hasTACZAttr) {
                var verticalRecoilAttr = ForgeRegistries.ATTRIBUTES.getValue(new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_vertical_recoil"));
                if (verticalRecoilAttr != null && attributeMap.getInstance(verticalRecoilAttr) != null) {
                    attributeMap.getInstance(verticalRecoilAttr).removeModifier(VERTICAL_RECOIL_UUID);
                }
                var horizontalRecoilAttr = ForgeRegistries.ATTRIBUTES.getValue(new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_horizontal_recoil"));
                if (horizontalRecoilAttr != null && attributeMap.getInstance(horizontalRecoilAttr) != null) {
                    attributeMap.getInstance(horizontalRecoilAttr).removeModifier(HORIZONTAL_RECOIL_UUID);
                }
            }

            if (hasTAA) {
                var adsTimeAttr = ForgeRegistries.ATTRIBUTES.getValue(new net.minecraft.resources.ResourceLocation("taa", "ads_time"));
                if (adsTimeAttr != null && attributeMap.getInstance(adsTimeAttr) != null) {
                    attributeMap.getInstance(adsTimeAttr).removeModifier(ADS_TIME_UUID);
                }
                String[] inaccuracyTypes = {"inaccuracy_aim", "inaccuracy_lie", "inaccuracy_move", "inaccuracy_sneak", "inaccuracy_stand"};
                UUID[] uuids = {INACCURACY_AIM_UUID, INACCURACY_LIE_UUID, INACCURACY_MOVE_UUID, INACCURACY_SNEAK_UUID, INACCURACY_STAND_UUID};
                for (int i = 0; i < inaccuracyTypes.length; i++) {
                    var attr = ForgeRegistries.ATTRIBUTES.getValue(new net.minecraft.resources.ResourceLocation("taa", inaccuracyTypes[i]));
                    if (attr != null && attributeMap.getInstance(attr) != null) {
                        attributeMap.getInstance(attr).removeModifier(uuids[i]);
                    }
                }
            }
        }

        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }
}