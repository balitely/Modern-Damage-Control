package com.moderndamage.control.item;

import com.moderndamage.control.config.ModClothConfig;
import com.moderndamage.control.effect.ModEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class SamSplintItem extends Item {
    public SamSplintItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 80;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            if (player.hasEffect(ModEffects.LEFT_ARM_FRACTURE.get())) {
                player.removeEffect(ModEffects.LEFT_ARM_FRACTURE.get());
                int healTime = ModClothConfig.get().splintedHealTimeSeconds * 20;
                player.addEffect(new MobEffectInstance(ModEffects.LEFT_SPLINTED_ARM.get(), healTime, 0, false, true, true));
                applyDamageAndSound(stack, player, level);
            } else if (player.hasEffect(ModEffects.RIGHT_ARM_FRACTURE.get())) {
                player.removeEffect(ModEffects.RIGHT_ARM_FRACTURE.get());
                int healTime = ModClothConfig.get().splintedHealTimeSeconds * 20;
                player.addEffect(new MobEffectInstance(ModEffects.LEFT_SPLINTED_ARM.get(), healTime, 0, false, true, true));
                applyDamageAndSound(stack, player, level);
            } else if (player.hasEffect(ModEffects.LEFT_LEG_FRACTURE.get())) {
                player.removeEffect(ModEffects.LEFT_LEG_FRACTURE.get());
                int healTime = ModClothConfig.get().splintedHealTimeSeconds * 20;
                player.addEffect(new MobEffectInstance(ModEffects.LEFT_SPLINTED_ARM.get(), healTime, 0, false, true, true));
                applyDamageAndSound(stack, player, level);
            } else if (player.hasEffect(ModEffects.RIGHT_LEG_FRACTURE.get())) {
                player.removeEffect(ModEffects.RIGHT_LEG_FRACTURE.get());
                int healTime = ModClothConfig.get().splintedHealTimeSeconds * 20;
                player.addEffect(new MobEffectInstance(ModEffects.LEFT_SPLINTED_ARM.get(), healTime, 0, false, true, true));
                applyDamageAndSound(stack, player, level);
            }
        }
        return stack;
    }

    private void applyDamageAndSound(ItemStack stack, Player player, Level level) {
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(p.getUsedItemHand()));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.HONEY_DRINK, SoundSource.PLAYERS, 1.0f, 1.0f);
        if (!player.getAbilities().instabuild && stack.isEmpty()) {
        }
    }
}