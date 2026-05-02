package com.moderndamage.control.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class CreatineBoostEffect extends MobEffect {
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("c1e2f3a4-1111-2222-3333-aaaaaaaaaaaa");
    private static final UUID DAMAGE_UUID = UUID.fromString("c1e2f3a4-2222-3333-4444-bbbbbbbbbbbb");
    private static final UUID SPEED_UUID = UUID.fromString("c1e2f3a4-3333-4444-5555-cccccccccccc");
    private static final UUID VERTICAL_RECOIL_UUID = UUID.fromString("c1e2f3a4-4444-5555-6666-dddddddddddd");
    private static final UUID HORIZONTAL_RECOIL_UUID = UUID.fromString("c1e2f3a4-5555-6666-7777-eeeeeeeeeeee");

    public CreatineBoostEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (!(entity instanceof Player)) return;

        var attackSpeedAttr = Attributes.ATTACK_SPEED;
        var attackSpeedInst = attributeMap.getInstance(attackSpeedAttr);
        if (attackSpeedInst != null) {
            attackSpeedInst.addPermanentModifier(new AttributeModifier(ATTACK_SPEED_UUID,
                    "creatine_attack_speed", 0.15, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        var damageAttr = Attributes.ATTACK_DAMAGE;
        var damageInst = attributeMap.getInstance(damageAttr);
        if (damageInst != null) {
            damageInst.addPermanentModifier(new AttributeModifier(DAMAGE_UUID,
                    "creatine_damage", 0.15, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        var speedAttr = Attributes.MOVEMENT_SPEED;
        var speedInst = attributeMap.getInstance(speedAttr);
        if (speedInst != null) {
            speedInst.addPermanentModifier(new AttributeModifier(SPEED_UUID,
                    "creatine_speed", 0.15, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        boolean hasTACZAttr = ModList.get().isLoaded("tacz_attributes");
        if (hasTACZAttr) {
            var verticalAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_vertical_recoil"));
            if (verticalAttr != null && attributeMap.getInstance(verticalAttr) != null) {
                attributeMap.getInstance(verticalAttr).addPermanentModifier(new AttributeModifier(VERTICAL_RECOIL_UUID,
                        "creatine_vertical_recoil", -0.1, AttributeModifier.Operation.ADDITION));
            }
            var horizontalAttr = ForgeRegistries.ATTRIBUTES.getValue(
                    new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_horizontal_recoil"));
            if (horizontalAttr != null && attributeMap.getInstance(horizontalAttr) != null) {
                attributeMap.getInstance(horizontalAttr).addPermanentModifier(new AttributeModifier(HORIZONTAL_RECOIL_UUID,
                        "creatine_horizontal_recoil", -0.1, AttributeModifier.Operation.ADDITION));
            }
        }

        super.addAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (!(entity instanceof Player)) return;

        var attackSpeedAttr = Attributes.ATTACK_SPEED;
        var attackSpeedInst = attributeMap.getInstance(attackSpeedAttr);
        if (attackSpeedInst != null) attackSpeedInst.removeModifier(ATTACK_SPEED_UUID);

        var damageAttr = Attributes.ATTACK_DAMAGE;
        var damageInst = attributeMap.getInstance(damageAttr);
        if (damageInst != null) damageInst.removeModifier(DAMAGE_UUID);

        var speedAttr = Attributes.MOVEMENT_SPEED;
        var speedInst = attributeMap.getInstance(speedAttr);
        if (speedInst != null) speedInst.removeModifier(SPEED_UUID);

        boolean hasTACZAttr = ModList.get().isLoaded("tacz_attributes");
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
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}