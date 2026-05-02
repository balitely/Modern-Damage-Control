package com.moderndamage.control.item;

import com.moderndamage.control.effect.ModEffects;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class JohnsonFirstAidKitItem extends Item {
    private static final ResourceLocation MYCELIUM_EFFECT = new ResourceLocation("spore", "mycelium_ef");

    public JohnsonFirstAidKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 100;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && !level.isClientSide) {
            int durability = stack.getDamageValue();
            int maxDurability = stack.getMaxDamage();
            if (durability >= maxDurability - 1) {
                return stack;
            }

            player.addEffect(new MobEffectInstance(ModEffects.IV_FLUID.get(), 2 * 20, 0));
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));

            boolean treated = false;
            if (!treated && player.hasEffect(ModEffects.MINOR_BLEEDING.get())) {
                player.removeEffect(ModEffects.MINOR_BLEEDING.get());
                treated = true;
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            }
            if (!treated && ModList.get().isLoaded("spore")) {
                var effect = ForgeRegistries.MOB_EFFECTS.getValue(MYCELIUM_EFFECT);
                if (effect != null && player.hasEffect(effect)) {
                    player.removeEffect(effect);
                    treated = true;
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
                }
            }

            playUseSound(level, player);
        }
        return stack;
    }

    protected void playUseSound(Level level, Player player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.HONEY_DRINK, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}