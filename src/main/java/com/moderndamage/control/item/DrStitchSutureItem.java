package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class DrStitchSutureItem extends Item {
    public DrStitchSutureItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 160; // 8秒
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
            if (healed) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.HONEY_DRINK, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return stack;
    }
}