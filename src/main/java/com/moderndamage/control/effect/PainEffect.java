package com.moderndamage.control.effect;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.compat.EnhancedVisualsCompat;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class PainEffect extends MobEffect {

    private static final UUID SPEED_UUID = UUID.fromString("e1e2e3e4-1111-2222-3333-aaaaaaaaaaaa");
    private static final UUID MOVE_SPEED_TAA_UUID = UUID.fromString("e1e2e3e4-1234-5678-90ab-cdef12345678");
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("e1e2e3e4-aaaa-bbbb-cccc-dddddddddddd");
    private static final UUID ADS_TIME_UUID = UUID.fromString("e1e2e3e4-2222-3333-4444-bbbbbbbbbbbb");
    private static final UUID RELOAD_SPEED_UUID = UUID.fromString("e1e2e3e4-6666-7777-8888-cccccccccccc");
    private static final UUID RPM_UUID = UUID.fromString("e1e2e3e4-cccc-dddd-eeee-ffffffffffff");
    private static final UUID INACCURACY_AIM_UUID = UUID.fromString("e1e2e3e4-8888-9999-aaaa-bbbbbbbbbbbb");
    private static final UUID INACCURACY_LIE_UUID = UUID.fromString("e1e2e3e4-9999-aaaa-bbbb-cccccccccccc");
    private static final UUID INACCURACY_MOVE_UUID = UUID.fromString("e1e2e3e4-aaaa-bbbb-cccc-dddddddddddd");
    private static final UUID INACCURACY_SNEAK_UUID = UUID.fromString("e1e2e3e4-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID INACCURACY_STAND_UUID = UUID.fromString("e1e2e3e4-cccc-dddd-eeee-ffffffffffff");
    private static final UUID VERTICAL_RECOIL_UUID = UUID.fromString("e1e2e3e4-4444-5555-6666-777777777777");
    private static final UUID HORIZONTAL_RECOIL_UUID = UUID.fromString("e1e2e3e4-5555-6666-7777-888888888888");

    public PainEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (!(entity instanceof Player player)) return;
        if (player.hasEffect(ModEffects.PAIN_SUPPRESSION.get())) return;

        var speedAttr = Attributes.MOVEMENT_SPEED;
        var speedInst = attributeMap.getInstance(speedAttr);
        if (speedInst != null) {
            speedInst.addPermanentModifier(new AttributeModifier(SPEED_UUID,
                    "pain_speed", -0.4, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        var attackSpeedAttr = Attributes.ATTACK_SPEED;
        var attackSpeedInst = attributeMap.getInstance(attackSpeedAttr);
        if (attackSpeedInst != null) {
            attackSpeedInst.addPermanentModifier(new AttributeModifier(ATTACK_SPEED_UUID,
                    "pain_attack_speed", -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        boolean hasTAA = ModList.get().isLoaded("taa");
        boolean hasTACZAttr = ModList.get().isLoaded("tacz_attributes");

        if (hasTAA) {
            var moveSpeedTaaAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "move_speed"));
            if (moveSpeedTaaAttr != null && attributeMap.getInstance(moveSpeedTaaAttr) != null) {
                attributeMap.getInstance(moveSpeedTaaAttr).addPermanentModifier(new AttributeModifier(MOVE_SPEED_TAA_UUID,
                        "pain_move_speed_taa", -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            var adsTimeAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "ads_time"));
            if (adsTimeAttr != null && attributeMap.getInstance(adsTimeAttr) != null) {
                attributeMap.getInstance(adsTimeAttr).addPermanentModifier(new AttributeModifier(ADS_TIME_UUID,
                        "pain_ads_time", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            var reloadAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "reload_speed"));
            if (reloadAttr != null && attributeMap.getInstance(reloadAttr) != null) {
                attributeMap.getInstance(reloadAttr).addPermanentModifier(new AttributeModifier(RELOAD_SPEED_UUID,
                        "pain_reload", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            var rpmAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "rounds_per_minute"));
            if (rpmAttr != null && attributeMap.getInstance(rpmAttr) != null) {
                attributeMap.getInstance(rpmAttr).addPermanentModifier(new AttributeModifier(RPM_UUID,
                        "pain_rpm", -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            String[] inaccuracyTypes = {"inaccuracy_aim", "inaccuracy_lie", "inaccuracy_move", "inaccuracy_sneak", "inaccuracy_stand"};
            UUID[] uuids = {INACCURACY_AIM_UUID, INACCURACY_LIE_UUID, INACCURACY_MOVE_UUID, INACCURACY_SNEAK_UUID, INACCURACY_STAND_UUID};
            for (int i = 0; i < inaccuracyTypes.length; i++) {
                var attr = ForgeRegistries.ATTRIBUTES.getValue(
                        new net.minecraft.resources.ResourceLocation("taa", inaccuracyTypes[i]));
                if (attr != null && attributeMap.getInstance(attr) != null) {
                    attributeMap.getInstance(attr).addPermanentModifier(new AttributeModifier(uuids[i],
                            "pain_inaccuracy", 0.5, AttributeModifier.Operation.ADDITION));
                }
            }
        }

        if (hasTACZAttr) {
            var verticalRecoilAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_vertical_recoil"));
            if (verticalRecoilAttr != null && attributeMap.getInstance(verticalRecoilAttr) != null) {
                attributeMap.getInstance(verticalRecoilAttr).addPermanentModifier(new AttributeModifier(VERTICAL_RECOIL_UUID,
                        "pain_vertical_recoil", 1.0, AttributeModifier.Operation.ADDITION));
            }
            var horizontalRecoilAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_horizontal_recoil"));
            if (horizontalRecoilAttr != null && attributeMap.getInstance(horizontalRecoilAttr) != null) {
                attributeMap.getInstance(horizontalRecoilAttr).addPermanentModifier(new AttributeModifier(HORIZONTAL_RECOIL_UUID,
                        "pain_horizontal_recoil", 1.0, AttributeModifier.Operation.ADDITION));
            }
        }

        super.addAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (!(entity instanceof Player)) return;

        var speedAttr = Attributes.MOVEMENT_SPEED;
        var speedInst = attributeMap.getInstance(speedAttr);
        if (speedInst != null) speedInst.removeModifier(SPEED_UUID);

        var attackSpeedAttr = Attributes.ATTACK_SPEED;
        var attackSpeedInst = attributeMap.getInstance(attackSpeedAttr);
        if (attackSpeedInst != null) attackSpeedInst.removeModifier(ATTACK_SPEED_UUID);

        boolean hasTAA = ModList.get().isLoaded("taa");
        boolean hasTACZAttr = ModList.get().isLoaded("tacz_attributes");

        if (hasTAA) {
            var moveSpeedTaaAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "move_speed"));
            if (moveSpeedTaaAttr != null && attributeMap.getInstance(moveSpeedTaaAttr) != null) {
                attributeMap.getInstance(moveSpeedTaaAttr).removeModifier(MOVE_SPEED_TAA_UUID);
            }
            var adsTimeAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "ads_time"));
            if (adsTimeAttr != null && attributeMap.getInstance(adsTimeAttr) != null) {
                attributeMap.getInstance(adsTimeAttr).removeModifier(ADS_TIME_UUID);
            }
            var reloadAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "reload_speed"));
            if (reloadAttr != null && attributeMap.getInstance(reloadAttr) != null) {
                attributeMap.getInstance(reloadAttr).removeModifier(RELOAD_SPEED_UUID);
            }
            var rpmAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "rounds_per_minute"));
            if (rpmAttr != null && attributeMap.getInstance(rpmAttr) != null) {
                attributeMap.getInstance(rpmAttr).removeModifier(RPM_UUID);
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
            var verticalRecoilAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_vertical_recoil"));
            if (verticalRecoilAttr != null && attributeMap.getInstance(verticalRecoilAttr) != null) {
                attributeMap.getInstance(verticalRecoilAttr).removeModifier(VERTICAL_RECOIL_UUID);
            }
            var horizontalRecoilAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_horizontal_recoil"));
            if (horizontalRecoilAttr != null && attributeMap.getInstance(horizontalRecoilAttr) != null) {
                attributeMap.getInstance(horizontalRecoilAttr).removeModifier(HORIZONTAL_RECOIL_UUID);
            }
        }

        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player player)) return;

        if (player.level().isClientSide) {
            if (ModernDamage.enhancedVisuals != null) {
                boolean hasPain = player.hasEffect(ModEffects.PAIN.get());
                boolean hasSuppression = player.hasEffect(ModEffects.PAIN_SUPPRESSION.get());
                ModernDamage.enhancedVisuals.updatePainVisual(player, hasPain, hasSuppression);
            }
            return;
        }

        if (player.hasEffect(ModEffects.PAIN_SUPPRESSION.get())) {
            if (player.hasEffect(MobEffects.DARKNESS)) player.removeEffect(MobEffects.DARKNESS);
            return;
        }
        if (player.tickCount % 20 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 1, false, true, true));
        }
        if (player.isSprinting()) {
            player.setSprinting(false);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}