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

public class LeftArmFractureEffect extends AbstractFractureEffect {

    private static final Random RANDOM = new Random();
    private long lastPainTriggerTime = 0;

    private static final UUID MELEE_DAMAGE_UUID = UUID.fromString("a1b2c3d4-8888-9999-aaaa-bbbbbbbbbbbb");
    private static final UUID RELOAD_SPEED_UUID = UUID.fromString("a1b2c3d4-1111-2222-3333-444444444444");
    private static final UUID INACCURACY_AIM_UUID = UUID.fromString("a1b2c3d4-3333-4444-5555-666666666666");
    private static final UUID INACCURACY_LIE_UUID = UUID.fromString("a1b2c3d4-4444-5555-6666-777777777777");
    private static final UUID INACCURACY_MOVE_UUID = UUID.fromString("a1b2c3d4-5555-6666-7777-888888888888");
    private static final UUID INACCURACY_SNEAK_UUID = UUID.fromString("a1b2c3d4-6666-7777-8888-999999999999");
    private static final UUID INACCURACY_STAND_UUID = UUID.fromString("a1b2c3d4-7777-8888-9999-aaaaaaaaaaaa");
    private static final UUID VERTICAL_RECOIL_UUID = UUID.fromString("a1b2c3d4-9999-aaaa-bbbb-cccccccccccc");
    private static final UUID HORIZONTAL_RECOIL_UUID = UUID.fromString("a1b2c3d4-aaaa-bbbb-cccc-dddddddddddd");

    public LeftArmFractureEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        var damageAttr = Attributes.ATTACK_DAMAGE;
        var damageInstance = attributeMap.getInstance(damageAttr);
        if (damageInstance != null) {
            damageInstance.addPermanentModifier(new AttributeModifier(MELEE_DAMAGE_UUID,
                    "left_arm_fracture_melee", -0.15, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        if (entity instanceof Player) {
            boolean hasTAA = ModList.get().isLoaded("taa");
            boolean hasTACZAttr = ModList.get().isLoaded("tacz_attributes");

            if (hasTAA) {
                var reloadAttr = ForgeRegistries.ATTRIBUTES.getValue(new net.minecraft.resources.ResourceLocation("taa", "reload_speed"));
                if (reloadAttr != null && attributeMap.getInstance(reloadAttr) != null) {
                    attributeMap.getInstance(reloadAttr).addPermanentModifier(new AttributeModifier(RELOAD_SPEED_UUID,
                            "left_arm_fracture_reload", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }

                String[] inaccuracyTypes = {"inaccuracy_aim", "inaccuracy_lie", "inaccuracy_move", "inaccuracy_sneak", "inaccuracy_stand"};
                UUID[] uuids = {INACCURACY_AIM_UUID, INACCURACY_LIE_UUID, INACCURACY_MOVE_UUID, INACCURACY_SNEAK_UUID, INACCURACY_STAND_UUID};
                for (int i = 0; i < inaccuracyTypes.length; i++) {
                    var attr = ForgeRegistries.ATTRIBUTES.getValue(new net.minecraft.resources.ResourceLocation("taa", inaccuracyTypes[i]));
                    if (attr != null && attributeMap.getInstance(attr) != null) {
                        attributeMap.getInstance(attr).addPermanentModifier(new AttributeModifier(uuids[i],
                                "left_arm_fracture_inaccuracy", 0.2, AttributeModifier.Operation.ADDITION));
                    }
                }
            }

            if (hasTACZAttr) {
                var verticalRecoilAttr = ForgeRegistries.ATTRIBUTES.getValue(new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_vertical_recoil"));
                if (verticalRecoilAttr != null && attributeMap.getInstance(verticalRecoilAttr) != null) {
                    attributeMap.getInstance(verticalRecoilAttr).addPermanentModifier(new AttributeModifier(VERTICAL_RECOIL_UUID,
                            "left_arm_fracture_vertical_recoil", 0.2, AttributeModifier.Operation.ADDITION));
                }
                var horizontalRecoilAttr = ForgeRegistries.ATTRIBUTES.getValue(new net.minecraft.resources.ResourceLocation("tacz_attributes", "ads_horizontal_recoil"));
                if (horizontalRecoilAttr != null && attributeMap.getInstance(horizontalRecoilAttr) != null) {
                    attributeMap.getInstance(horizontalRecoilAttr).addPermanentModifier(new AttributeModifier(HORIZONTAL_RECOIL_UUID,
                            "left_arm_fracture_horizontal_recoil", 0.2, AttributeModifier.Operation.ADDITION));
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

            if (hasTAA) {
                var reloadAttr = ForgeRegistries.ATTRIBUTES.getValue(new net.minecraft.resources.ResourceLocation("taa", "reload_speed"));
                if (reloadAttr != null && attributeMap.getInstance(reloadAttr) != null) {
                    attributeMap.getInstance(reloadAttr).removeModifier(RELOAD_SPEED_UUID);
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