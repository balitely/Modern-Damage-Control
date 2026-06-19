package com.moderndamage.control.armor;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.api.IProtectionSourceProvider;
import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.api.ModDamageSubPart;
import com.moderndamage.control.api.ProtectionSource;
import com.moderndamage.control.api.ProtectionSourceProviderRegistry;
import com.moderndamage.control.api.event.ArmorHitEvent;
import com.moderndamage.control.api.event.GetArmorLevelEvent;
import com.moderndamage.control.attribute.ModAttributes;
import com.moderndamage.control.capability.parthealth.CreaturePartHealthCapability;
import com.moderndamage.control.capability.parthealth.PartHealthCapability;
import com.moderndamage.control.config.ModClothConfig;
import com.moderndamage.control.entity.EntityHitboxHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.*;
import java.util.function.IntConsumer;

public class ArmorCalculator {
    private static final Random RANDOM = new Random();

    public static class PenetrationResult {
        public final float finalDamage;
        public final boolean penetrated;
        public final boolean partial;

        public PenetrationResult(float finalDamage, boolean penetrated, boolean partial) {
            this.finalDamage = finalDamage;
            this.penetrated = penetrated;
            this.partial = partial;
        }
    }

    // ========================= 主部位公共 API =========================

    private static List<ProtectionSource> getAllProtectionSources(LivingEntity target, ModDamagePart part) {
        ModernDamage.LOGGER.info("getAllProtectionSources called for target={}, part={}", target.getName().getString(), part);
        ModernDamage.LOGGER.info("Provider count: {}", ProtectionSourceProviderRegistry.getProviders().size());
        List<ProtectionSource> sources = new ArrayList<>();
        ModClothConfig config = ModClothConfig.get();

        int natural = EntityHitboxHelper.getNaturalArmor(target, part);
        if (natural > 0) {
            Map<ModDamageSubPart, Integer> emptyInt = Map.of();
            Map<ModDamageSubPart, Float> emptyFloat = Map.of();
            sources.add(new ProtectionSource(null, emptyInt, emptyInt, emptyFloat, 1.0f, null, natural, 0, 0.0f, true));
        }

        java.util.function.Consumer<ItemStack> processStack = (stack) -> {
            if (stack.isEmpty()) return;
            ArmorData data = ArmorDataLoader.getArmorData(stack.getItem());
            if (data == null) return;

            int level = data.getProtectionLevel(part);
            if (level > 0) {
                int dynamicLevel = getDynamicProtectionLevel(stack, level);
                int toughness = getDynamicToughness(stack, data.getBaseToughness(part));
                float materialFactor = data.getMaterialFactor(part);
                float ricochet = data.getRicochetChance(part);
                sources.add(new ProtectionSource(stack, Map.of(), Map.of(), Map.of(), materialFactor, null,
                        dynamicLevel, toughness, ricochet, false));
            }

            for (IProtectionSourceProvider provider : ProtectionSourceProviderRegistry.getProviders()) {
                List<ProtectionSource> additional = provider.getAdditionalSources(stack, target);
                if (additional != null) {
                    for (ProtectionSource src : additional) {
                        if (src.getSubProtection() != null && !src.getSubProtection().isEmpty()) {
                            int subMax = src.getSubProtection().values().stream().max(Integer::compare).orElse(0);
                            if (subMax > 0) {
                                sources.add(new ProtectionSource(src.getSourceStack(), Map.of(), Map.of(), Map.of(),
                                        src.getMaterialFactor(), src.getDurabilityConsumer(),
                                        subMax, src.getToughness(), src.getRicochetChance(), false));
                            }
                        } else {
                            sources.add(src);
                        }
                    }
                }
            }
        };

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            processStack.accept(target.getItemBySlot(slot));
        }

        if (ModList.get().isLoaded("curios")) {
            CuriosApi.getCuriosInventory(target).ifPresent(handler -> {
                for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                    IDynamicStackHandler stackHandler = stacksHandler.getStacks();
                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        processStack.accept(stackHandler.getStackInSlot(i));
                    }
                }
            });
        }

        return sources;
    }

    public static int getArmorLevel(LivingEntity target, ModDamagePart part) {
        List<ProtectionSource> sources = getAllProtectionSources(target, part);
        if (sources.isEmpty()) return 0;

        List<Integer> levels = new ArrayList<>();
        for (ProtectionSource src : sources) {
            int level = src.getPrimaryProtectionLevel();
            if (level > 0) levels.add(level);
        }
        levels.sort(Collections.reverseOrder());

        if (levels.isEmpty()) return 0;
        int highest = levels.get(0);
        if (levels.size() == 1) {
            int total = highest;
            if (!target.level().isClientSide) {
                GetArmorLevelEvent event = new GetArmorLevelEvent(target, part, total);
                MinecraftForge.EVENT_BUS.post(event);
                total = event.getArmorLevel();
            }
            return total;
        }
        int otherSum = 0;
        for (int i = 1; i < levels.size(); i++) otherSum += levels.get(i);
        ModClothConfig config = ModClothConfig.get();
        float factor = config.armorStackingFactor;
        int total = highest + Math.round(otherSum * factor);
        int cap = config.armorCap;
        if (cap > 0 && total > cap) total = cap;

        if (!target.level().isClientSide) {
            GetArmorLevelEvent event = new GetArmorLevelEvent(target, part, total);
            MinecraftForge.EVENT_BUS.post(event);
            total = event.getArmorLevel();
        }
        return total;
    }

    private static int getTotalToughness(LivingEntity target, ModDamagePart part) {
        int natural = EntityHitboxHelper.getNaturalToughness(target, part);
        int total = natural;
        List<ProtectionSource> sources = getAllProtectionSources(target, part);
        for (ProtectionSource src : sources) {
            total += src.getToughness();
        }
        return Math.min(total, 100);
    }

    // 主部位护甲穿透计算
    public static PenetrationResult applyArmorPenetration(LivingEntity target, ModDamagePart hitPart,
                                                          float originalDamage, float penetration) {
        if (originalDamage <= 0) return new PenetrationResult(0, false, false);

        int armorLevel = getArmorLevel(target, hitPart);
        ModClothConfig config = ModClothConfig.get();
        List<ProtectionSource> sources = getAllProtectionSources(target, hitPart);

        // 跳弹判定
        boolean ricochetTriggered = false;
        if (config.enableRicochet) {
            for (ProtectionSource src : sources) {
                if (src.isNatural()) continue;
                if (src.getRicochetChance() > 0 && target.getRandom().nextFloat() < src.getRicochetChance()) {
                    ricochetTriggered = true;
                    break;
                }
            }
        }
        if (ricochetTriggered) {
            float ricochetDamage = originalDamage * config.ricochetDamageRatio;
            if (ricochetDamage < 1) ricochetDamage = 1;
            final float finalRicochetDamage = ricochetDamage;
            if (config.debugMode) {
                ModernDamage.LOGGER.info("[Ricochet] {} ricocheted! Damage: {} -> {}",
                        target.getName().getString(), originalDamage, finalRicochetDamage);
            }
            if (target instanceof Player && config.damageModel == ModClothConfig.DamageModel.HARDCORE) {
                target.getCapability(PartHealthCapability.PART_HEALTH_CAP).ifPresent(cap -> cap.damagePart(hitPart, finalRicochetDamage));
            } else if (config.creaturePartHealthEnabled && config.damageModel == ModClothConfig.DamageModel.HARDCORE) {
                target.getCapability(CreaturePartHealthCapability.CREATURE_PART_HEALTH_CAP).ifPresent(cap -> cap.damagePart(hitPart, finalRicochetDamage));
            } else {
                target.setHealth(Math.max(0, target.getHealth() - finalRicochetDamage));
            }
            return new PenetrationResult(finalRicochetDamage, false, false);
        }

        // 计算穿甲后伤害（先穿透再韧性）
        float finalDamageBeforeToughness = originalDamage;
        boolean penetrated = false;
        boolean partial = false;
        boolean cancelDurabilityLoss = false;

        if (armorLevel > 0) {
            int penetrationValue = (int) (penetration * 100);
            if (penetrationValue >= armorLevel + 5) {
                finalDamageBeforeToughness = originalDamage;
                penetrated = true;
            } else if (penetrationValue >= armorLevel - 10) {
                int diff = (armorLevel + 6) - penetrationValue;
                double chance = (16.0 - diff) / 15.0;
                chance = Math.min(1.0, Math.max(0.0, chance));
                if (RANDOM.nextDouble() <= chance) {
                    float ratio = config.partialPenetrationMinRatio + RANDOM.nextFloat() * (config.partialPenetrationMaxRatio - config.partialPenetrationMinRatio);
                    finalDamageBeforeToughness = originalDamage * ratio;
                    partial = true;
                } else {
                    int bluntDiff = armorLevel - penetrationValue;
                    finalDamageBeforeToughness = originalDamage * calculateBluntRatio(config, bluntDiff);
                }
            } else {
                int bluntDiff = armorLevel - penetrationValue;
                finalDamageBeforeToughness = originalDamage * calculateBluntRatio(config, bluntDiff);
            }
        } else {
            // 无护甲，保持原始伤害
            finalDamageBeforeToughness = originalDamage;
        }

        // 发送事件
        if (!target.level().isClientSide) {
            ArmorHitEvent event = new ArmorHitEvent(target, hitPart, null, originalDamage, finalDamageBeforeToughness, armorLevel);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                cancelDurabilityLoss = true;
            }
            finalDamageBeforeToughness = event.getFinalDamage();
        }

        // ========== 耐久损耗（使用新公式） ==========
        if (armorLevel > 0 && !cancelDurabilityLoss && !sources.isEmpty()) {
            // 计算穿透因子和护甲抵抗因子
            float penFactor = 1.0f + penetration * config.durabilityPenFactorMultiplier;
            float armorResist = 1.0f / (1.0f + armorLevel * config.durabilityArmorResistFactor);

            // 对主防具（最高等级）扣除耐久
            ProtectionSource primary = null;
            int maxLevel = 0;
            for (ProtectionSource src : sources) {
                if (src.isNatural()) continue;
                int level = src.getPrimaryProtectionLevel();
                if (level > maxLevel) {
                    maxLevel = level;
                    primary = src;
                }
            }
            if (primary != null && primary.getSourceStack() != null) {
                float baseLoss = 1 + originalDamage * primary.getMaterialFactor() * penFactor * armorResist * config.durabilityBaseMultiplier;
                int durabilityLoss = (int) Math.ceil(baseLoss);
                durabilityLoss = Math.min(config.maxDurabilityLoss, Math.max(1, durabilityLoss));
                if (primary.getDurabilityConsumer() != null) {
                    primary.getDurabilityConsumer().accept(durabilityLoss);
                } else {
                    hurtItem(primary.getSourceStack(), target, durabilityLoss);
                }
            }

            // 对其它防具扣除耐久（减半）
            for (ProtectionSource src : sources) {
                if (src == primary) continue;
                if (src.isNatural()) continue;
                if (src.getSourceStack() == null) continue;
                float baseLoss = 1 + originalDamage * src.getMaterialFactor() * penFactor * armorResist * config.durabilityBaseMultiplier;
                int durabilityLoss = (int) Math.ceil(baseLoss);
                durabilityLoss = Math.min(config.maxDurabilityLoss, Math.max(1, durabilityLoss / 2));
                if (src.getDurabilityConsumer() != null) {
                    src.getDurabilityConsumer().accept(durabilityLoss);
                } else {
                    hurtItem(src.getSourceStack(), target, durabilityLoss);
                }
            }
        }

        // 韧性减伤
        int totalToughness = getTotalToughness(target, hitPart);
        float finalDamage = finalDamageBeforeToughness;
        if (totalToughness > 0) {
            float reduction = totalToughness / 100.0f;
            finalDamage = finalDamageBeforeToughness * (1 - reduction);
            if (finalDamage < 0) finalDamage = 0;
        }

        if (config.debugMode) {
            ModernDamage.LOGGER.info("[Penetration] target={}, part={}, armor={}, pen={}, dmg: orig={}, beforeTough={}, final={}, toughness={}, penType={}",
                    target.getName().getString(), hitPart, armorLevel, (int)(penetration*100),
                    originalDamage, finalDamageBeforeToughness, finalDamage, totalToughness,
                    penetrated ? "FULL" : (partial ? "PARTIAL" : (armorLevel>0?"BLUNT":"NONE")));
        }
        return new PenetrationResult(finalDamage, penetrated, partial);
    }

    // ========================= 子部位专用 API =========================

    private static List<ProtectionSource> getAllProtectionSourcesForSubPart(LivingEntity target, ModDamageSubPart subPart) {
        List<ProtectionSource> sources = new ArrayList<>();
        ModDamagePart parent = subPart.getParent();
        ModClothConfig config = ModClothConfig.get();

        int natural = EntityHitboxHelper.getNaturalArmor(target, parent);
        if (natural > 0) {
            sources.add(new ProtectionSource(null, Map.of(), Map.of(), Map.of(), 1.0f, null, natural, 0, 0.0f, true));
        }

        java.util.function.Consumer<ItemStack> processStack = (stack) -> {
            if (stack.isEmpty()) return;
            ArmorData data = ArmorDataLoader.getArmorData(stack.getItem());
            if (data == null) return;

            int level = 0;
            int toughness = 0;
            float ricochet = 0.0f;
            int subLevel = data.getSubProtectionLevel(subPart);
            if (subLevel > 0) {
                level = subLevel;
                toughness = data.getEffectiveToughness(subPart, parent);
                ricochet = data.getEffectiveRicochetChance(subPart, parent);
            } else {
                int parentLevel = data.getProtectionLevel(parent);
                if (parentLevel > 0) {
                    level = parentLevel;
                    toughness = data.getBaseToughness(parent);
                    ricochet = data.getRicochetChance(parent);
                }
            }
            if (level > 0) {
                int dynamicLevel = getDynamicProtectionLevel(stack, level);
                int dynamicToughness = getDynamicToughness(stack, toughness);
                float materialFactor = data.getMaterialFactor(parent);
                sources.add(new ProtectionSource(stack, Map.of(), Map.of(), Map.of(), materialFactor, null,
                        dynamicLevel, dynamicToughness, ricochet, false));
            }

            for (IProtectionSourceProvider provider : ProtectionSourceProviderRegistry.getProviders()) {
                List<ProtectionSource> additional = provider.getAdditionalSources(stack, target);
                if (additional != null) {
                    for (ProtectionSource src : additional) {
                        if (src.getSubProtection() != null && src.getSubProtection().containsKey(subPart)) {
                            int plateLevel = src.getSubProtection().get(subPart);
                            if (plateLevel > 0) {
                                int plateTough = src.getSubToughness() != null ? src.getSubToughness().getOrDefault(subPart, 0) : 0;
                                float plateRicochet = src.getSubRicochetChance() != null ? src.getSubRicochetChance().getOrDefault(subPart, 0.0f) : 0.0f;
                                sources.add(new ProtectionSource(src.getSourceStack(), Map.of(), Map.of(), Map.of(),
                                        src.getMaterialFactor(), src.getDurabilityConsumer(),
                                        plateLevel, plateTough, plateRicochet, false));
                            }
                        } else if (src.getPrimaryProtectionLevel() > 0) {
                            if (src.getSourceStack() != null) {
                                ArmorData attData = ArmorDataLoader.getArmorData(src.getSourceStack().getItem());
                                if (attData != null && attData.protects(parent)) {
                                    sources.add(src);
                                }
                            }
                        }
                    }
                }
            }
        };

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            processStack.accept(target.getItemBySlot(slot));
        }

        if (ModList.get().isLoaded("curios")) {
            CuriosApi.getCuriosInventory(target).ifPresent(handler -> {
                for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                    IDynamicStackHandler stackHandler = stacksHandler.getStacks();
                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        processStack.accept(stackHandler.getStackInSlot(i));
                    }
                }
            });
        }

        return sources;
    }

    public static int getArmorLevelForSubPart(LivingEntity target, ModDamageSubPart subPart) {
        List<ProtectionSource> sources = getAllProtectionSourcesForSubPart(target, subPart);
        if (sources.isEmpty()) return 0;

        List<Integer> levels = new ArrayList<>();
        for (ProtectionSource src : sources) {
            int level = src.getPrimaryProtectionLevel();
            if (level > 0) levels.add(level);
        }
        levels.sort(Collections.reverseOrder());

        if (levels.isEmpty()) return 0;
        int highest = levels.get(0);
        if (levels.size() == 1) {
            return highest;
        }
        int otherSum = 0;
        for (int i = 1; i < levels.size(); i++) otherSum += levels.get(i);
        ModClothConfig config = ModClothConfig.get();
        float factor = config.armorStackingFactor;
        int total = highest + Math.round(otherSum * factor);
        int cap = config.armorCap;
        if (cap > 0 && total > cap) total = cap;
        if (!target.level().isClientSide) {
            GetArmorLevelEvent event = new GetArmorLevelEvent(target, subPart.getParent(), total);
            MinecraftForge.EVENT_BUS.post(event);
            total = event.getArmorLevel();
        }
        return total;
    }

    private static int getTotalToughnessForSubPart(LivingEntity target, ModDamageSubPart subPart) {
        int natural = EntityHitboxHelper.getNaturalToughness(target, subPart.getParent());
        int total = natural;
        List<ProtectionSource> sources = getAllProtectionSourcesForSubPart(target, subPart);
        for (ProtectionSource src : sources) {
            total += src.getToughness();
        }
        return Math.min(total, 100);
    }

    // 子部位护甲穿透计算
    public static PenetrationResult applyArmorPenetration(LivingEntity target, ModDamageSubPart subPart,
                                                          float originalDamage, float penetration) {
        if (originalDamage <= 0) return new PenetrationResult(0, false, false);

        int armorLevel = getArmorLevelForSubPart(target, subPart);
        ModClothConfig config = ModClothConfig.get();
        ModDamagePart parent = subPart.getParent();
        List<ProtectionSource> sources = getAllProtectionSourcesForSubPart(target, subPart);

        // 跳弹判定
        boolean ricochetTriggered = false;
        if (config.enableRicochet) {
            for (ProtectionSource src : sources) {
                if (src.isNatural()) continue;
                if (src.getRicochetChance() > 0 && target.getRandom().nextFloat() < src.getRicochetChance()) {
                    ricochetTriggered = true;
                    break;
                }
            }
        }
        if (ricochetTriggered) {
            float ricochetDamage = originalDamage * config.ricochetDamageRatio;
            if (ricochetDamage < 1) ricochetDamage = 1;
            final float finalRicochetDamage = ricochetDamage;
            if (config.debugMode) {
                ModernDamage.LOGGER.info("[Ricochet] {} ricocheted (subPart {})! Damage: {} -> {}",
                        target.getName().getString(), subPart, originalDamage, finalRicochetDamage);
            }
            if (target instanceof Player && config.damageModel == ModClothConfig.DamageModel.HARDCORE) {
                target.getCapability(PartHealthCapability.PART_HEALTH_CAP).ifPresent(cap -> cap.damagePart(parent, finalRicochetDamage));
            } else if (config.creaturePartHealthEnabled && config.damageModel == ModClothConfig.DamageModel.HARDCORE) {
                target.getCapability(CreaturePartHealthCapability.CREATURE_PART_HEALTH_CAP).ifPresent(cap -> cap.damagePart(parent, finalRicochetDamage));
            } else {
                target.setHealth(Math.max(0, target.getHealth() - finalRicochetDamage));
            }
            return new PenetrationResult(finalRicochetDamage, false, false);
        }

        float finalDamageBeforeToughness = originalDamage;
        boolean penetrated = false;
        boolean partial = false;
        boolean cancelDurabilityLoss = false;

        if (armorLevel > 0) {
            int penetrationValue = (int) (penetration * 100);
            if (penetrationValue >= armorLevel + 5) {
                finalDamageBeforeToughness = originalDamage;
                penetrated = true;
            } else if (penetrationValue >= armorLevel - 10) {
                int diff = (armorLevel + 6) - penetrationValue;
                double chance = (16.0 - diff) / 15.0;
                chance = Math.min(1.0, Math.max(0.0, chance));
                if (RANDOM.nextDouble() <= chance) {
                    float ratio = config.partialPenetrationMinRatio + RANDOM.nextFloat() * (config.partialPenetrationMaxRatio - config.partialPenetrationMinRatio);
                    finalDamageBeforeToughness = originalDamage * ratio;
                    partial = true;
                } else {
                    int bluntDiff = armorLevel - penetrationValue;
                    finalDamageBeforeToughness = originalDamage * calculateBluntRatio(config, bluntDiff);
                }
            } else {
                int bluntDiff = armorLevel - penetrationValue;
                finalDamageBeforeToughness = originalDamage * calculateBluntRatio(config, bluntDiff);
            }
        } else {
            finalDamageBeforeToughness = originalDamage;
        }

        // 发送事件
        if (!target.level().isClientSide) {
            ArmorHitEvent event = new ArmorHitEvent(target, parent, subPart, originalDamage, finalDamageBeforeToughness, armorLevel);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                cancelDurabilityLoss = true;
            }
            finalDamageBeforeToughness = event.getFinalDamage();
        }

        if (armorLevel > 0 && !cancelDurabilityLoss && !sources.isEmpty()) {
            float penFactor = 1.0f + penetration * config.durabilityPenFactorMultiplier;
            float armorResist = 1.0f / (1.0f + armorLevel * config.durabilityArmorResistFactor);

            ProtectionSource primary = null;
            int maxLevel = 0;
            for (ProtectionSource src : sources) {
                if (src.isNatural()) continue;
                int level = src.getPrimaryProtectionLevel();
                if (level > maxLevel) {
                    maxLevel = level;
                    primary = src;
                }
            }
            if (primary != null && primary.getSourceStack() != null) {
                float baseLoss = 1 + originalDamage * primary.getMaterialFactor() * penFactor * armorResist * config.durabilityBaseMultiplier;
                int durabilityLoss = (int) Math.ceil(baseLoss);
                durabilityLoss = Math.min(config.maxDurabilityLoss, Math.max(1, durabilityLoss));
                if (primary.getDurabilityConsumer() != null) {
                    primary.getDurabilityConsumer().accept(durabilityLoss);
                } else {
                    hurtItem(primary.getSourceStack(), target, durabilityLoss);
                }
            }

            for (ProtectionSource src : sources) {
                if (src == primary) continue;
                if (src.isNatural()) continue;
                if (src.getSourceStack() == null) continue;
                float baseLoss = 1 + originalDamage * src.getMaterialFactor() * penFactor * armorResist * config.durabilityBaseMultiplier;
                int durabilityLoss = (int) Math.ceil(baseLoss);
                durabilityLoss = Math.min(config.maxDurabilityLoss, Math.max(1, durabilityLoss / 2));
                if (src.getDurabilityConsumer() != null) {
                    src.getDurabilityConsumer().accept(durabilityLoss);
                } else {
                    hurtItem(src.getSourceStack(), target, durabilityLoss);
                }
            }
        }

        // 韧性减伤
        int totalToughness = getTotalToughnessForSubPart(target, subPart);
        float finalDamage = finalDamageBeforeToughness;
        if (totalToughness > 0) {
            float reduction = totalToughness / 100.0f;
            finalDamage = finalDamageBeforeToughness * (1 - reduction);
            if (finalDamage < 0) finalDamage = 0;
        }

        if (config.debugMode) {
            ModernDamage.LOGGER.info("[Penetration] target={}, subPart={}, armor={}, pen={}, dmg: orig={}, beforeTough={}, final={}, toughness={}, penType={}",
                    target.getName().getString(), subPart, armorLevel, (int)(penetration*100),
                    originalDamage, finalDamageBeforeToughness, finalDamage, totalToughness,
                    penetrated ? "FULL" : (partial ? "PARTIAL" : (armorLevel>0?"BLUNT":"NONE")));
        }
        return new PenetrationResult(finalDamage, penetrated, partial);
    }

    // ========================= 辅助方法 =========================

    private static void hurtItem(ItemStack stack, LivingEntity target, int amount) {
        if (stack.isEmpty()) return;
        int maxDamage = stack.getMaxDamage();
        if (maxDamage <= 0) return;
        int currentDamage = stack.getDamageValue();
        int remaining = maxDamage - currentDamage;
        if (remaining <= 1) return;
        int actualAmount = Math.min(amount, remaining - 1);
        if (actualAmount > 0) {
            stack.hurtAndBreak(actualAmount, target, (p) -> {});
        }
    }

    private static float calculateBluntRatio(ModClothConfig config, int diff) {
        float base = config.bluntDamageRatio;
        float halveDist = config.bluntDamageHalveDistance;
        float minRatio = config.bluntDamageMinRatio;
        float exponent = (diff - 10) / halveDist;
        float ratio = (float) (base * Math.pow(0.5, exponent));
        return Math.max(ratio, minRatio);
    }

    // 动态等级（根据耐久百分比）
    private static int getDynamicProtectionLevel(ItemStack stack, int baseLevel) {
        if (baseLevel == 0) return 0;
        int maxDurability = stack.getMaxDamage();
        if (maxDurability <= 0) return baseLevel;
        int currentDurability = maxDurability - stack.getDamageValue();
        if (currentDurability <= 1) return 0;
        float durabilityPercent = (float) currentDurability / (float) maxDurability;
        float multiplier = 0.5f + 0.5f * durabilityPercent;
        return Math.max(1, Math.round(baseLevel * multiplier));
    }

    private static int getDynamicToughness(ItemStack stack, int baseToughness) {
        if (baseToughness == 0) return 0;
        int maxDurability = stack.getMaxDamage();
        if (maxDurability <= 0) return baseToughness;
        int currentDurability = maxDurability - stack.getDamageValue();
        float durabilityPercent = (float) currentDurability / (float) maxDurability;
        return Math.round(baseToughness * durabilityPercent);
    }

    // 以下为向后兼容的公共方法
    public static int getDynamicProtectionLevel(ItemStack armorStack, ModDamagePart part) {
        ArmorData data = ArmorDataLoader.getArmorData(armorStack.getItem());
        if (data == null) return 0;
        int baseLevel = data.getProtectionLevel(part);
        return getDynamicProtectionLevel(armorStack, baseLevel);
    }

    public static int getDynamicToughness(ItemStack armorStack, ModDamagePart part) {
        ArmorData data = ArmorDataLoader.getArmorData(armorStack.getItem());
        if (data == null) return 0;
        int base = data.getBaseToughness(part);
        return getDynamicToughness(armorStack, base);
    }

    public static int getDynamicProtectionLevel(ItemStack armorStack, ModDamageSubPart subPart) {
        ArmorData data = ArmorDataLoader.getArmorData(armorStack.getItem());
        if (data == null) return 0;
        int baseLevel = data.getSubProtectionLevel(subPart);
        if (baseLevel == 0) {
            baseLevel = data.getProtectionLevel(subPart.getParent());
        }
        return getDynamicProtectionLevel(armorStack, baseLevel);
    }

    public static int getDynamicToughness(ItemStack armorStack, ModDamageSubPart subPart) {
        ArmorData data = ArmorDataLoader.getArmorData(armorStack.getItem());
        if (data == null) return 0;
        int base = data.getSubToughness(subPart);
        if (base <= 0) {
            base = data.getBaseToughness(subPart.getParent());
        }
        return getDynamicToughness(armorStack, base);
    }

    public static int getProtectionLevel(LivingEntity target, ModDamagePart hitPart) {
        return getArmorLevel(target, hitPart);
    }

    @Deprecated
    private static List<ItemStack> getAllProtectingItems(LivingEntity target, ModDamagePart hitPart) {
        return Collections.emptyList();
    }
}