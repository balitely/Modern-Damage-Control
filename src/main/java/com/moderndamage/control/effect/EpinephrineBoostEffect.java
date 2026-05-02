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

public class EpinephrineBoostEffect extends AbstractStimulantEffect {

    private static final UUID SPEED_UUID = UUID.fromString("e1e2e3e4-1111-2222-3333-aaaaaaaaaaaa");
    private static final UUID TAA_SPEED_UUID = UUID.fromString("e1e2e3e4-2222-3333-4444-bbbbbbbbbbbb");
    private static final UUID MELEE_UUID = UUID.fromString("e1e2e3e4-3333-4444-5555-cccccccccccc");
    private static final UUID ADS_TIME_UUID = UUID.fromString("e1e2e3e4-4444-5555-6666-dddddddddddd");
    private static final UUID VERTICAL_RECOIL_UUID = UUID.fromString("e1e2e3e4-5555-6666-7777-eeeeeeeeeeee");
    private static final UUID HORIZONTAL_RECOIL_UUID = UUID.fromString("e1e2e3e4-6666-7777-8888-ffffffffffff");
    private static final UUID INACCURACY_AIM_UUID = UUID.fromString("e1e2e3e4-7777-8888-9999-aaaaaaaaabbb");
    private static final UUID INACCURACY_LIE_UUID = UUID.fromString("e1e2e3e4-8888-9999-aaaa-bbbbbbbbbccc");
    private static final UUID INACCURACY_MOVE_UUID = UUID.fromString("e1e2e3e4-9999-aaaa-bbbb-cccccccccddd");
    private static final UUID INACCURACY_SNEAK_UUID = UUID.fromString("e1e2e3e4-aaaa-bbbb-cccc-dddddddddddd");
    private static final UUID INACCURACY_STAND_UUID = UUID.fromString("e1e2e3e4-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID RECOVERY_UUID = UUID.fromString("e1e2e3e4-cccc-dddd-eeee-ffffffffffff");

    public EpinephrineBoostEffect(MobEffectCategory category, int color) {
        super(category, color, 120);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (!(entity instanceof Player)) return;

        var speedAttr = Attributes.MOVEMENT_SPEED;
        var speedInst = attributeMap.getInstance(speedAttr);
        if (speedInst != null) {
            speedInst.addPermanentModifier(new AttributeModifier(SPEED_UUID,
                    "epinephrine_speed", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        var meleeAttr = Attributes.ATTACK_DAMAGE;
        var meleeInst = attributeMap.getInstance(meleeAttr);
        if (meleeInst != null) {
            meleeInst.addPermanentModifier(new AttributeModifier(MELEE_UUID,
                    "epinephrine_melee", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        boolean hasTAA = ModList.get().isLoaded("taa");
        boolean hasTACZAttr = ModList.get().isLoaded("tacz_attributes");
        boolean hasStamina = ModList.get().isLoaded("staminafortweakers");

        if (hasTAA) {
            var taaSpeedAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "move_speed"));
            if (taaSpeedAttr != null && attributeMap.getInstance(taaSpeedAttr) != null) {
                attributeMap.getInstance(taaSpeedAttr).addPermanentModifier(new AttributeModifier(TAA_SPEED_UUID,
                        "epinephrine_move_speed_taa", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            var adsTimeAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "ads_time"));
            if (adsTimeAttr != null && attributeMap.getInstance(adsTimeAttr) != null) {
                attributeMap.getInstance(adsTimeAttr).addPermanentModifier(new AttributeModifier(ADS_TIME_UUID,
                        "epinephrine_ads_time", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            String[] inaccuracyTypes = {"inaccuracy_aim", "inaccuracy_lie", "inaccuracy_move", "inaccuracy_sneak", "inaccuracy_stand"};
            UUID[] uuids = {INACCURACY_AIM_UUID, INACCURACY_LIE_UUID, INACCURACY_MOVE_UUID, INACCURACY_SNEAK_UUID, INACCURACY_STAND_UUID};
            for (int i = 0; i < inaccuracyTypes.length; i++) {
                var attr = ForgeRegistries.ATTRIBUTES.getValue(
                        new net.minecraft.resources.ResourceLocation("taa", inaccuracyTypes[i]));
                if (attr != null && attributeMap.getInstance(attr) != null) {
                    attributeMap.getInstance(attr).addPermanentModifier(new AttributeModifier(uuids[i],
                            "epinephrine_inaccuracy", -0.2, AttributeModifier.Operation.ADDITION));
                }
            }
        }

        if (hasTACZAttr) {
            var verticalAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_vertical_recoil"));
            if (verticalAttr != null && attributeMap.getInstance(verticalAttr) != null) {
                attributeMap.getInstance(verticalAttr).addPermanentModifier(new AttributeModifier(VERTICAL_RECOIL_UUID,
                        "epinephrine_vertical_recoil", -0.3, AttributeModifier.Operation.ADDITION));
            }
            var horizontalAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_horizontal_recoil"));
            if (horizontalAttr != null && attributeMap.getInstance(horizontalAttr) != null) {
                attributeMap.getInstance(horizontalAttr).addPermanentModifier(new AttributeModifier(HORIZONTAL_RECOIL_UUID,
                        "epinephrine_horizontal_recoil", -0.3, AttributeModifier.Operation.ADDITION));
            }
        }

        if (hasStamina) {
            var recoveryAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("staminafortweakers", "generic.stamina_recovery_rate"));
            if (recoveryAttr != null && attributeMap.getInstance(recoveryAttr) != null) {
                attributeMap.getInstance(recoveryAttr).addPermanentModifier(new AttributeModifier(RECOVERY_UUID,
                        "epinephrine_recovery", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }

        super.addAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (!(entity instanceof Player)) return;

        var speedAttr = Attributes.MOVEMENT_SPEED;
        if (attributeMap.getInstance(speedAttr) != null)
            attributeMap.getInstance(speedAttr).removeModifier(SPEED_UUID);

        var meleeAttr = Attributes.ATTACK_DAMAGE;
        if (attributeMap.getInstance(meleeAttr) != null)
            attributeMap.getInstance(meleeAttr).removeModifier(MELEE_UUID);

        boolean hasTAA = ModList.get().isLoaded("taa");
        boolean hasTACZAttr = ModList.get().isLoaded("tacz_attributes");
        boolean hasStamina = ModList.get().isLoaded("staminafortweakers");

        if (hasTAA) {
            var taaSpeedAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "move_speed"));
            if (taaSpeedAttr != null && attributeMap.getInstance(taaSpeedAttr) != null)
                attributeMap.getInstance(taaSpeedAttr).removeModifier(TAA_SPEED_UUID);

            var adsTimeAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "ads_time"));
            if (adsTimeAttr != null && attributeMap.getInstance(adsTimeAttr) != null)
                attributeMap.getInstance(adsTimeAttr).removeModifier(ADS_TIME_UUID);

            String[] inaccuracyTypes = {"inaccuracy_aim", "inaccuracy_lie", "inaccuracy_move", "inaccuracy_sneak", "inaccuracy_stand"};
            UUID[] uuids = {INACCURACY_AIM_UUID, INACCURACY_LIE_UUID, INACCURACY_MOVE_UUID, INACCURACY_SNEAK_UUID, INACCURACY_STAND_UUID};
            for (int i = 0; i < inaccuracyTypes.length; i++) {
                var attr = ForgeRegistries.ATTRIBUTES.getValue(
                        new net.minecraft.resources.ResourceLocation("taa", inaccuracyTypes[i]));
                if (attr != null && attributeMap.getInstance(attr) != null)
                    attributeMap.getInstance(attr).removeModifier(uuids[i]);
            }
        }

        if (hasTACZAttr) {
            var verticalAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_vertical_recoil"));
            if (verticalAttr != null && attributeMap.getInstance(verticalAttr) != null)
                attributeMap.getInstance(verticalAttr).removeModifier(VERTICAL_RECOIL_UUID);

            var horizontalAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_horizontal_recoil"));
            if (horizontalAttr != null && attributeMap.getInstance(horizontalAttr) != null)
                attributeMap.getInstance(horizontalAttr).removeModifier(HORIZONTAL_RECOIL_UUID);
        }

        if (hasStamina) {
            var recoveryAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("staminafortweakers", "generic.stamina_recovery_rate"));
            if (recoveryAttr != null && attributeMap.getInstance(recoveryAttr) != null)
                attributeMap.getInstance(recoveryAttr).removeModifier(RECOVERY_UUID);
        }

        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        if (!(entity instanceof Player player)) return;

        if (player.hasEffect(ModEffects.PAIN.get())) {
            player.removeEffect(ModEffects.PAIN.get());
        }
        if (player.hasEffect(ModEffects.DIZZINESS.get())) {
            player.removeEffect(ModEffects.DIZZINESS.get());
        }
        if (player.hasEffect(ModEffects.FATIGUE.get())) {
            player.removeEffect(ModEffects.FATIGUE.get());
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}