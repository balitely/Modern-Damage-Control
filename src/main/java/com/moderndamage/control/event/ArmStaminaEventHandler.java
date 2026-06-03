package com.moderndamage.control.event;

import com.moderndamage.control.attribute.ModAttributes;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraftforge.event.TickEvent;
import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.capability.armstamina.ArmStaminaCapability;
import com.moderndamage.control.config.ModClothConfig;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModernDamage.MODID)
public class ArmStaminaEventHandler {

    // 近战攻击消耗（攻击动作开始时）
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        ModClothConfig config = ModClothConfig.get();
        if (!config.enableArmStamina) return;
        // 创造模式不消耗耐力
        if (player.isCreative()) return;

        player.getCapability(ArmStaminaCapability.ARM_STAMINA).ifPresent(stamina -> {
            if (!stamina.consumeStamina(config.meleeAttackCost, false)) {
                event.setCanceled(true);
            }
        });
    }

    // 每 tick 消耗（拉弓、弩装填、枪械瞄准）
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        ModClothConfig config = ModClothConfig.get();
        if (!config.enableArmStamina) return;
        // 创造模式不消耗耐力
        if (player.isCreative()) return;

        player.getCapability(ArmStaminaCapability.ARM_STAMINA).ifPresent(stamina -> {
            // 1. 弓蓄力
            if (player.isUsingItem() && player.getUseItem().getItem() instanceof BowItem) {
                stamina.consumeStamina(config.bowDrawCostPerTick, false);
            }
            // 2. 弩装填
            else if (player.isUsingItem() && player.getUseItem().getItem() instanceof CrossbowItem) {
                if (!CrossbowItem.isCharged(player.getUseItem())) {
                    stamina.consumeStamina(config.bowDrawCostPerTick, false);
                }
            }
            // 3. TACZ 枪械瞄准（动态消耗：人机功效 + 枪械重量）
            else {
                try {
                    IGunOperator operator = IGunOperator.fromLivingEntity(player);
                    if (operator != null) {
                        ShooterDataHolder dataHolder = operator.getDataHolder();
                        if (dataHolder != null && dataHolder.isAiming) {
                            float cost = config.adsCostPerTick;
                            // 动态消耗因子（人机功效 + 枪械重量）
                            float factor = getDynamicADSCostFactor(player, config);
                            cost *= factor;
                            // 趴下时减免 50%
                            if (dataHolder.isCrawling) {
                                cost *= 0.5f;
                            }
                            stamina.consumeStamina(cost, false);
                        }
                    }
                } catch (NoClassDefFoundError | Exception ignored) {
                    // TaCZ 未安装或接口缺失
                }
            }
        });
    }

    // 获取动态瞄准消耗因子（人机功效 + 枪械重量）
    private static float getDynamicADSCostFactor(Player player, ModClothConfig config) {
        float factor = 1.0f;

        // 1. 人机功效因子
        double ergo = player.getAttributeValue(ModAttributes.ERGONOMICS.get());
        if (ergo < 100.0) {
            factor *= 1.0f + (float)((100.0 - ergo) / 100.0);
        } else if (ergo > 100.0) {
            factor *= 1.0f - (float)((ergo - 100.0) / 100.0);
        }
        // 限制因子范围 [0.5, 3.0]
        factor = Math.min(3.0f, Math.max(0.5f, factor));

        // 2. 枪械重量因子（如果启用）
        if (config.enableWeightCost) {
            float weightFactor = getWeightFactor(player, config);
            factor *= weightFactor;
        }

        return factor;
    }

    // 获取枪械重量因子
    private static float getWeightFactor(Player player, ModClothConfig config) {
        ItemStack mainHand = player.getMainHandItem();
        if (!(mainHand.getItem() instanceof IGun iGun)) {
            return 1.0f;
        }
        try {
            var gunId = iGun.getGunId(mainHand);
            var gunIndexOpt = TimelessAPI.getCommonGunIndex(gunId);
            if (gunIndexOpt.isEmpty()) return 1.0f;
            var gunData = gunIndexOpt.get().getGunData();
            float weight = gunData.getWeight(); // 单位 kg
            float weightFactor = 1.0f;
            if (weight > config.referenceWeight) {
                float extra = (weight - config.referenceWeight) / config.referenceWeight;
                weightFactor += extra * config.weightFactorPerKg;
            }
            return Math.min(3.0f, weightFactor);
        } catch (Exception e) {
            ModernDamage.LOGGER.warn("Failed to get gun weight for {}", player.getName().getString(), e);
            return 1.0f;
        }
    }

    // 挖掘消耗
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        ModClothConfig config = ModClothConfig.get();
        if (!config.enableArmStamina) return;
        // 创造模式不消耗耐力
        if (player.isCreative()) return;

        player.getCapability(ArmStaminaCapability.ARM_STAMINA).ifPresent(stamina -> {
            if (!stamina.consumeStamina(config.miningCostPerBlock, false)) {
                event.setCanceled(true);
            }
        });
    }
}