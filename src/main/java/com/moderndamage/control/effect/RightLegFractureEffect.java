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

public class RightLegFractureEffect extends AbstractFractureEffect {

    private static final Random RANDOM = new Random();
    private long lastPainTriggerTime = 0;

    private static final UUID SPEED_UUID = UUID.fromString("f2f2f3f4-4444-5555-6666-bbbbbbbbbbbb");
    private static final UUID MOVE_SPEED_TAA_UUID = UUID.fromString("f2f2f3f4-5678-90ab-cdef-123456789abc");
    private static final UUID MAX_STAMINA_UUID = UUID.fromString("f2f2f3f4-5555-6666-7777-cccccccccccc");
    private static final UUID RECOVERY_UUID = UUID.fromString("f2f2f3f4-6666-7777-8888-dddddddddddd");

    public RightLegFractureEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (!(entity instanceof Player)) return;

        var speedAttr = Attributes.MOVEMENT_SPEED;
        var speedInst = attributeMap.getInstance(speedAttr);
        if (speedInst != null) {
            speedInst.addPermanentModifier(new AttributeModifier(SPEED_UUID,
                    "right_leg_fracture_speed", -0.15, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        boolean hasTAA = ModList.get().isLoaded("taa");
        boolean hasStamina = ModList.get().isLoaded("staminafortweakers");

        if (hasTAA) {
            var moveSpeedTaaAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "move_speed"));
            if (moveSpeedTaaAttr != null && attributeMap.getInstance(moveSpeedTaaAttr) != null) {
                attributeMap.getInstance(moveSpeedTaaAttr).addPermanentModifier(new AttributeModifier(MOVE_SPEED_TAA_UUID,
                        "right_leg_fracture_move_speed_taa", -0.03, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }

        if (hasStamina) {
            var maxStaminaAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("staminafortweakers", "generic.max_stamina"));
            if (maxStaminaAttr != null && attributeMap.getInstance(maxStaminaAttr) != null) {
                attributeMap.getInstance(maxStaminaAttr).addPermanentModifier(new AttributeModifier(MAX_STAMINA_UUID,
                        "right_leg_fracture_max_stamina", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            var recoveryAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("staminafortweakers", "generic.stamina_recovery_rate"));
            if (recoveryAttr != null && attributeMap.getInstance(recoveryAttr) != null) {
                attributeMap.getInstance(recoveryAttr).addPermanentModifier(new AttributeModifier(RECOVERY_UUID,
                        "right_leg_fracture_recovery", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
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

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (entity.level().isClientSide) return;
        if (!(entity instanceof Player player)) return;
        if (player.isSprinting()) player.setSprinting(false);
        long currentTick = player.tickCount;
        if (currentTick - lastPainTriggerTime < 200) return;
        if (RANDOM.nextDouble() < 0.002) {
            lastPainTriggerTime = currentTick;
            int duration = 20 + RANDOM.nextInt(41);
            player.addEffect(new MobEffectInstance(ModEffects.PAIN.get(), duration, 0));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}