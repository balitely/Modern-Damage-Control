package com.moderndamage.control.effect;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.config.ModClothConfig;
import com.moderndamage.control.effect.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class PH16HealEffect extends MobEffect {
    private int healTimer = 0;

    public PH16HealEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        ModClothConfig config = ModClothConfig.get();
        int interval = config.ph16HealInterval;
        if (interval <= 0) return;
        healTimer++;
        if (healTimer >= interval) {
            healTimer = 0;
            if (entity instanceof Player player) {
                boolean healed = false;
                if (player.hasEffect(ModEffects.LEFT_ARM_TRAUMA.get())) {
                    player.removeEffect(ModEffects.LEFT_ARM_TRAUMA.get());
                    healed = true;
                } else if (player.hasEffect(ModEffects.RIGHT_ARM_TRAUMA.get())) {
                    player.removeEffect(ModEffects.RIGHT_ARM_TRAUMA.get());
                    healed = true;
                } else if (player.hasEffect(ModEffects.LEFT_LEG_TRAUMA.get())) {
                    player.removeEffect(ModEffects.LEFT_LEG_TRAUMA.get());
                    healed = true;
                } else if (player.hasEffect(ModEffects.RIGHT_LEG_TRAUMA.get())) {
                    player.removeEffect(ModEffects.RIGHT_LEG_TRAUMA.get());
                    healed = true;
                } else if (player.hasEffect(ModEffects.STOMACH_TRAUMA.get())) {
                    player.removeEffect(ModEffects.STOMACH_TRAUMA.get());
                    healed = true;
                }
                if (!healed) {
                    if (player.hasEffect(ModEffects.LEFT_ARM_FRACTURE.get())) {
                        player.removeEffect(ModEffects.LEFT_ARM_FRACTURE.get());
                        healed = true;
                    } else if (player.hasEffect(ModEffects.RIGHT_ARM_FRACTURE.get())) {
                        player.removeEffect(ModEffects.RIGHT_ARM_FRACTURE.get());
                        healed = true;
                    } else if (player.hasEffect(ModEffects.LEFT_LEG_FRACTURE.get())) {
                        player.removeEffect(ModEffects.LEFT_LEG_FRACTURE.get());
                        healed = true;
                    } else if (player.hasEffect(ModEffects.RIGHT_LEG_FRACTURE.get())) {
                        player.removeEffect(ModEffects.RIGHT_LEG_FRACTURE.get());
                        healed = true;
                    }
                }
                if (!healed) {
                    if (player.hasEffect(ModEffects.LEFT_SPLINTED_ARM.get())) {
                        player.removeEffect(ModEffects.LEFT_SPLINTED_ARM.get());
                        healed = true;
                    } else if (player.hasEffect(ModEffects.RIGHT_SPLINTED_ARM.get())) {
                        player.removeEffect(ModEffects.RIGHT_SPLINTED_ARM.get());
                        healed = true;
                    } else if (player.hasEffect(ModEffects.LEFT_SPLINTED_LEG.get())) {
                        player.removeEffect(ModEffects.LEFT_SPLINTED_LEG.get());
                        healed = true;
                    } else if (player.hasEffect(ModEffects.RIGHT_SPLINTED_LEG.get())) {
                        player.removeEffect(ModEffects.RIGHT_SPLINTED_LEG.get());
                        healed = true;
                    }
                }
                if (!healed) {
                    player.heal(5.0f);
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}