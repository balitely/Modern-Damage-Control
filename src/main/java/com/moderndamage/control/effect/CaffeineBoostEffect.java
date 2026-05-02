package com.moderndamage.control.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class CaffeineBoostEffect extends AbstractStimulantEffect {

    private static final UUID MAX_STAMINA_UUID = UUID.fromString("b1b2c3d4-1111-2222-3333-aaaaaaaaaaaa");
    private static final UUID RECOVERY_UUID = UUID.fromString("b1b2c3d4-2222-3333-4444-bbbbbbbbbbbb");

    public CaffeineBoostEffect(MobEffectCategory category, int color) {
        super(category, color, 0);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (!(entity instanceof Player)) return;

        boolean hasStamina = ModList.get().isLoaded("staminafortweakers");
        if (!hasStamina) return;

        var maxStaminaAttr = ForgeRegistries.ATTRIBUTES.getValue(
                new net.minecraft.resources.ResourceLocation("staminafortweakers", "generic.max_stamina"));
        if (maxStaminaAttr != null && attributeMap.getInstance(maxStaminaAttr) != null) {
            attributeMap.getInstance(maxStaminaAttr).addPermanentModifier(new AttributeModifier(MAX_STAMINA_UUID,
                    "caffeine_max_stamina", 0.1, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        var recoveryAttr = ForgeRegistries.ATTRIBUTES.getValue(
                new net.minecraft.resources.ResourceLocation("staminafortweakers", "generic.stamina_recovery_rate"));
        if (recoveryAttr != null && attributeMap.getInstance(recoveryAttr) != null) {
            attributeMap.getInstance(recoveryAttr).addPermanentModifier(new AttributeModifier(RECOVERY_UUID,
                    "caffeine_recovery", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        super.addAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (!(entity instanceof Player)) return;

        boolean hasStamina = ModList.get().isLoaded("staminafortweakers");
        if (!hasStamina) return;

        var maxStaminaAttr = ForgeRegistries.ATTRIBUTES.getValue(
                new net.minecraft.resources.ResourceLocation("staminafortweakers", "generic.max_stamina"));
        if (maxStaminaAttr != null && attributeMap.getInstance(maxStaminaAttr) != null)
            attributeMap.getInstance(maxStaminaAttr).removeModifier(MAX_STAMINA_UUID);

        var recoveryAttr = ForgeRegistries.ATTRIBUTES.getValue(
                new net.minecraft.resources.ResourceLocation("staminafortweakers", "generic.stamina_recovery_rate"));
        if (recoveryAttr != null && attributeMap.getInstance(recoveryAttr) != null)
            attributeMap.getInstance(recoveryAttr).removeModifier(RECOVERY_UUID);

        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }
}