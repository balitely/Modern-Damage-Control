package com.moderndamage.control.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class InfectionResistanceEffect extends AbstractResistanceEffect {
    private static final ResourceLocation MYCELIUM_EFFECT = new ResourceLocation("spore", "mycelium_ef");
    private static boolean isSporeLoaded = ModList.get().isLoaded("spore");

    public InfectionResistanceEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        if (!(entity instanceof Player player)) return;

        if (isSporeLoaded) {
            MobEffect myceliumEffect = ForgeRegistries.MOB_EFFECTS.getValue(MYCELIUM_EFFECT);
            if (myceliumEffect != null && player.hasEffect(myceliumEffect)) {
                player.removeEffect(myceliumEffect);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}