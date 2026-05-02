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

public class RightSplintedLegEffect extends AbstractFractureEffect {

    private static final UUID SPEED_UUID = UUID.fromString("f4f2f3f4-aaaa-bbbb-cccc-dddddddddddd");
    private static final UUID MOVE_SPEED_TAA_UUID = UUID.fromString("f4f2f3f4-cdef-1234-5678-90abcdef1234");
    private static final UUID MAX_STAMINA_UUID = UUID.fromString("f4f2f3f4-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID RECOVERY_UUID = UUID.fromString("f4f2f3f4-cccc-dddd-eeee-ffffffffffff");

    public RightSplintedLegEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (!(entity instanceof Player)) return;

        var speedAttr = Attributes.MOVEMENT_SPEED;
        var speedInst = attributeMap.getInstance(speedAttr);
        if (speedInst != null) {
            speedInst.addPermanentModifier(new AttributeModifier(SPEED_UUID,
                    "right_splinted_leg_speed", -0.075, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        boolean hasTAA = ModList.get().isLoaded("taa");
        boolean hasStamina = ModList.get().isLoaded("staminafortweakers");

        if (hasTAA) {
            var moveSpeedTaaAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "move_speed"));
            if (moveSpeedTaaAttr != null && attributeMap.getInstance(moveSpeedTaaAttr) != null) {
                attributeMap.getInstance(moveSpeedTaaAttr).addPermanentModifier(new AttributeModifier(MOVE_SPEED_TAA_UUID,
                        "right_splinted_leg_move_speed_taa", -0.015, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }

        if (hasStamina) {
            var maxStaminaAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("staminafortweakers", "generic.max_stamina"));
            if (maxStaminaAttr != null && attributeMap.getInstance(maxStaminaAttr) != null) {
                attributeMap.getInstance(maxStaminaAttr).addPermanentModifier(new AttributeModifier(MAX_STAMINA_UUID,
                        "right_splinted_leg_max_stamina", -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            var recoveryAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("staminafortweakers", "generic.stamina_recovery_rate"));
            if (recoveryAttr != null && attributeMap.getInstance(recoveryAttr) != null) {
                attributeMap.getInstance(recoveryAttr).addPermanentModifier(new AttributeModifier(RECOVERY_UUID,
                        "right_splinted_leg_recovery", -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL));
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

        boolean hasTAA = ModList.get().isLoaded("taa");
        boolean hasStamina = ModList.get().isLoaded("staminafortweakers");

        if (hasTAA) {
            var moveSpeedTaaAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "move_speed"));
            if (moveSpeedTaaAttr != null && attributeMap.getInstance(moveSpeedTaaAttr) != null) {
                attributeMap.getInstance(moveSpeedTaaAttr).removeModifier(MOVE_SPEED_TAA_UUID);
            }
        }

        if (hasStamina) {
            var maxStaminaAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("staminafortweakers", "generic.max_stamina"));
            if (maxStaminaAttr != null && attributeMap.getInstance(maxStaminaAttr) != null) {
                attributeMap.getInstance(maxStaminaAttr).removeModifier(MAX_STAMINA_UUID);
            }
            var recoveryAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("staminafortweakers", "generic.stamina_recovery_rate"));
            if (recoveryAttr != null && attributeMap.getInstance(recoveryAttr) != null) {
                attributeMap.getInstance(recoveryAttr).removeModifier(RECOVERY_UUID);
            }
        }

        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }
}