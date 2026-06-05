package com.moderndamage.control.event;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.config.ModClothConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = ModernDamage.MODID)
public class MilkBlockHandler {

    @SubscribeEvent
    public static void onEffectRemove(MobEffectEvent.Remove event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        boolean isDrinkingMilk = !entity.getUseItem().isEmpty() &&
                entity.getUseItem().getItem() instanceof MilkBucketItem;
        if (!isDrinkingMilk) return;

        MobEffect effect = event.getEffect();
        if (effect == null) return;

        var effectKey = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        if (effectKey == null) return;

        if (effect.getCategory() == MobEffectCategory.HARMFUL &&
                effectKey.getNamespace().equals(ModernDamage.MODID)) {

            event.setCanceled(true);
            if (ModClothConfig.get().debugMode) {
                ModernDamage.LOGGER.debug("Milk prevented removing {} from {}",
                        effectKey, entity.getName().getString());
            }
        }
    }
}