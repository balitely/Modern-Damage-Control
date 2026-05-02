package com.moderndamage.control.effect;

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

import java.util.Random;
import java.util.UUID;

public class StomachTraumaEffect extends AbstractTraumaEffect {

    private static final Random RANDOM = new Random();
    private long lastPainTriggerTime = 0;

    private static final UUID SPEED_UUID = UUID.fromString("a3a3a3a4-1111-2222-3333-aaaaaaaaaaaa");
    private static final UUID MOVE_SPEED_TAA_UUID = UUID.fromString("a3a3a3a4-1234-5678-90ab-cdef12345678");
    private static final UUID KNOCKBACK_UUID = UUID.fromString("a3a3a3a4-2222-3333-4444-bbbbbbbbbbbb");
    private static final UUID MAX_STAMINA_UUID = UUID.fromString("a3a3a3a4-3333-4444-5555-cccccccccccc");
    private static final UUID RECOVERY_UUID = UUID.fromString("a3a3a3a4-4444-5555-6666-dddddddddddd");

    public StomachTraumaEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (!(entity instanceof Player)) return;

        var speedAttr = Attributes.MOVEMENT_SPEED;
        var speedInst = attributeMap.getInstance(speedAttr);
        if (speedInst != null) {
            speedInst.addPermanentModifier(new AttributeModifier(SPEED_UUID,
                    "stomach_trauma_speed", -0.075, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        var knockbackAttr = Attributes.KNOCKBACK_RESISTANCE;
        var knockbackInst = attributeMap.getInstance(knockbackAttr);
        if (knockbackInst != null) {
            knockbackInst.addPermanentModifier(new AttributeModifier(KNOCKBACK_UUID,
                    "stomach_trauma_knockback", -0.5, AttributeModifier.Operation.ADDITION));
        }

        boolean hasTAA = ModList.get().isLoaded("taa");
        boolean hasStamina = ModList.get().isLoaded("staminafortweakers");

        if (hasTAA) {
            var moveSpeedTaaAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("taa", "move_speed"));
            if (moveSpeedTaaAttr != null && attributeMap.getInstance(moveSpeedTaaAttr) != null) {
                attributeMap.getInstance(moveSpeedTaaAttr).addPermanentModifier(new AttributeModifier(MOVE_SPEED_TAA_UUID,
                        "stomach_trauma_move_speed_taa", -0.015, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }

        if (hasStamina) {
            var maxStaminaAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("staminafortweakers", "generic.max_stamina"));
            if (maxStaminaAttr != null && attributeMap.getInstance(maxStaminaAttr) != null) {
                attributeMap.getInstance(maxStaminaAttr).addPermanentModifier(new AttributeModifier(MAX_STAMINA_UUID,
                        "stomach_trauma_max_stamina", -0.15, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            var recoveryAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("staminafortweakers", "generic.stamina_recovery_rate"));
            if (recoveryAttr != null && attributeMap.getInstance(recoveryAttr) != null) {
                attributeMap.getInstance(recoveryAttr).addPermanentModifier(new AttributeModifier(RECOVERY_UUID,
                        "stomach_trauma_recovery", -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL));
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

        var knockbackAttr = Attributes.KNOCKBACK_RESISTANCE;
        var knockbackInst = attributeMap.getInstance(knockbackAttr);
        if (knockbackInst != null) knockbackInst.removeModifier(KNOCKBACK_UUID);

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

        long currentTick = player.tickCount;
        if (currentTick % 40 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, false, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0, false, true, true));
        }

        if (currentTick - lastPainTriggerTime < 200) return;
        if (RANDOM.nextDouble() < 0.005) {
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