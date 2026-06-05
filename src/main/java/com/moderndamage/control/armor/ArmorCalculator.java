package com.moderndamage.control.armor;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.api.ModDamageSubPart;
import com.moderndamage.control.api.event.ArmorHitEvent;
import com.moderndamage.control.api.event.GetArmorLevelEvent;
import com.moderndamage.control.capability.parthealth.CreaturePartHealthCapability;
import com.moderndamage.control.capability.parthealth.PartHealthCapability;
import com.moderndamage.control.config.ModClothConfig;
import com.moderndamage.control.attribute.ModAttributes;
import com.moderndamage.control.entity.EntityHitboxHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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

    private static int getTotalToughness(LivingEntity target, ModDamagePart hitPart) {
        int naturalToughness = EntityHitboxHelper.getNaturalToughness(target, hitPart);
        ModernDamage.LOGGER.info("getTotalToughness: natural toughness = {}", naturalToughness);

        int[] total = {naturalToughness};

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack stack = target.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            ArmorData data = ArmorDataLoader.getArmorData(stack.getItem());
            if (data != null && data.protects(hitPart)) {
                int toughness = getDynamicToughness(stack, hitPart);
                if (toughness > 0) {
                    total[0] += toughness;
                    if (ModClothConfig.get().debugMode) {
                        ModernDamage.LOGGER.debug("[Toughness] Added armor toughness from {} (part {}): +{} → total {}",
                                ForgeRegistries.ITEMS.getKey(stack.getItem()), hitPart, toughness, total[0]);
                    }
                }
            }
        }

        if (ModList.get().isLoaded("curios")) {
            CuriosApi.getCuriosInventory(target).ifPresent(handler -> {
                for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                    IDynamicStackHandler stackHandler = stacksHandler.getStacks(); // 只取普通槽位
                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        ItemStack stack = stackHandler.getStackInSlot(i);
                        if (stack.isEmpty()) continue;
                        ArmorData data = ArmorDataLoader.getArmorData(stack.getItem());
                        if (data != null && data.protects(hitPart)) {
                            int toughness = getDynamicToughness(stack, hitPart);
                            if (toughness > 0) {
                                total[0] += toughness;
                                if (ModClothConfig.get().debugMode) {
                                    ModernDamage.LOGGER.debug("[Toughness] Added curio toughness from {} (part {}): +{} → total {}",
                                            ForgeRegistries.ITEMS.getKey(stack.getItem()), hitPart, toughness, total[0]);
                                }
                            }
                        }
                    }
                }
            });
        }

        return Math.min(total[0], 100);
    }

    public static int getDynamicToughness(ItemStack armorStack, ModDamagePart part) {
        ArmorData data = ArmorDataLoader.getArmorData(armorStack.getItem());
        if (data == null) return 0;
        int base = data.getBaseToughness(part);
        if (base <= 0) return 0;
        int maxDurability = armorStack.getMaxDamage();
        if (maxDurability <= 0) return base;
        int currentDurability = maxDurability - armorStack.getDamageValue();
        float durabilityPercent = (float) currentDurability / (float) maxDurability;
        return Math.round(base * durabilityPercent);
    }

    private static float getMaterialFactor(ItemStack armorStack, ModDamagePart part) {
        ArmorData data = ArmorDataLoader.getArmorData(armorStack.getItem());
        if (data == null) return 1.0f;
        return data.getMaterialFactor(part);
    }

    private static ItemStack getProtectingItem(LivingEntity target, ModDamagePart hitPart) {
        int bestLevel = 0;
        ItemStack bestItem = ItemStack.EMPTY;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack stack = target.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            ArmorData data = ArmorDataLoader.getArmorData(stack.getItem());
            if (data != null && data.protects(hitPart)) {
                int level = data.getProtectionLevel(hitPart);
                if (level > bestLevel) {
                    bestLevel = level;
                    bestItem = stack;
                }
            }
        }
        return bestItem;
    }

    public static PenetrationResult applyArmorPenetration(LivingEntity target, ModDamagePart hitPart,
                                                          float originalDamage, float penetration) {
        if (originalDamage <= 0) return new PenetrationResult(0, false, false);

        int armorLevel = getArmorLevel(target, hitPart);
        ModClothConfig config = ModClothConfig.get();

        List<ItemStack> allProtecting = getAllProtectingItems(target, hitPart);
        ItemStack primaryItem = null;
        int maxLevel = 0;
        for (ItemStack stack : allProtecting) {
            int level = getDynamicProtectionLevel(stack, hitPart);
            if (level > maxLevel) {
                maxLevel = level;
                primaryItem = stack;
            }
        }

        if (config.enableRicochet && primaryItem != null && !primaryItem.isEmpty()) {
            ArmorData data = ArmorDataLoader.getArmorData(primaryItem.getItem());
            if (data != null) {
                float chance = data.getRicochetChance(hitPart);
                if (chance > 0 && target.getRandom().nextFloat() < chance) {
                    float ricochetDamage = originalDamage * config.ricochetDamageRatio;
                    if (ricochetDamage < 1) ricochetDamage = 1;
                    final float finalRicochetDamage = ricochetDamage;
                    final ModDamagePart finalHitPart = hitPart;
                    if (config.debugMode) {
                        ModernDamage.LOGGER.info("[Ricochet] {} ricocheted off {} {}! Damage: {} -> {}",
                                target.getName().getString(),
                                ForgeRegistries.ITEMS.getKey(primaryItem.getItem()),
                                hitPart, originalDamage, finalRicochetDamage);
                    }
                    if (target instanceof Player && config.damageModel == ModClothConfig.DamageModel.HARDCORE) {
                        target.getCapability(PartHealthCapability.PART_HEALTH_CAP).ifPresent(cap -> cap.damagePart(finalHitPart, finalRicochetDamage));
                    } else if (config.creaturePartHealthEnabled && config.damageModel == ModClothConfig.DamageModel.HARDCORE) {
                        target.getCapability(CreaturePartHealthCapability.CREATURE_PART_HEALTH_CAP).ifPresent(cap -> cap.damagePart(finalHitPart, finalRicochetDamage));
                    } else {
                        target.setHealth(Math.max(0, target.getHealth() - finalRicochetDamage));
                    }
                    return new PenetrationResult(finalRicochetDamage, false, false);
                }
            }
        }

        if (armorLevel == 0) {
            return new PenetrationResult(originalDamage, false, false);
        }

        int penetrationValue = (int) (penetration * 100);
        int baseDurabilityLoss = config.durabilityLossBase;
        int maxLoss = config.maxDurabilityLoss;

        float partialMin = config.partialPenetrationMinRatio;
        float partialMax = config.partialPenetrationMaxRatio;

        float finalDamageBeforeToughness;
        boolean penetrated = false;
        boolean partial = false;

        if (penetrationValue >= armorLevel + 5) {
            finalDamageBeforeToughness = originalDamage;
            penetrated = true;
        } else if (penetrationValue >= armorLevel - 10) {
            int diff = (armorLevel + 6) - penetrationValue;
            double chance = (16.0 - diff) / 15.0;
            chance = Math.min(1.0, Math.max(0.0, chance));
            if (RANDOM.nextDouble() <= chance) {
                float ratio = partialMin + RANDOM.nextFloat() * (partialMax - partialMin);
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

        float primaryMaterialFactor = 1.0f;
        if (primaryItem != null && !primaryItem.isEmpty()) {
            primaryMaterialFactor = getMaterialFactor(primaryItem, hitPart);
        }

        float penRatio = (float) penetrationValue / (float) armorLevel;

        int primaryDurabilityLoss = (int) Math.ceil(1 + finalDamageBeforeToughness * penRatio * primaryMaterialFactor);
        primaryDurabilityLoss = Math.min(maxLoss, Math.max(1, primaryDurabilityLoss));

        boolean cancelDurabilityLoss = false;
        if (!target.level().isClientSide) {
            ArmorHitEvent event = new ArmorHitEvent(target, hitPart, originalDamage, finalDamageBeforeToughness, armorLevel);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                cancelDurabilityLoss = true;
            }
            finalDamageBeforeToughness = event.getFinalDamage();
        }

        if (!cancelDurabilityLoss && !allProtecting.isEmpty()) {
            if (primaryItem != null && !primaryItem.isEmpty()) {
                hurtItem(primaryItem, target, primaryDurabilityLoss);
            }

            for (ItemStack stack : allProtecting) {
                if (stack == primaryItem) continue;

                float materialFactor = getMaterialFactor(stack, hitPart);
                int itemDurabilityLoss = (int) Math.ceil(1 + finalDamageBeforeToughness * penRatio * materialFactor);
                itemDurabilityLoss = Math.min(maxLoss, Math.max(1, itemDurabilityLoss));
                int halfLoss = Math.max(1, itemDurabilityLoss / 2);
                hurtItem(stack, target, halfLoss);
            }
        }

        int totalToughness = getTotalToughness(target, hitPart);
        float finalDamage = finalDamageBeforeToughness;
        if (totalToughness > 0) {
            float reduction = totalToughness / 100.0f;
            finalDamage = finalDamageBeforeToughness * (1 - reduction);
            if (finalDamage < 0) finalDamage = 0;
        }

        if (config.debugMode) {
            ModernDamage.LOGGER.info("[Penetration] target={}, part={}, armor={}, pen={}, dmg: orig={}, beforeTough={}, final={}, toughness={}, durLoss(primary)={}, penType={}",
                    target.getName().getString(), hitPart, armorLevel, penetrationValue,
                    originalDamage, finalDamageBeforeToughness, finalDamage, totalToughness, primaryDurabilityLoss,
                    penetrated ? "FULL" : (partial ? "PARTIAL" : "BLUNT"));
        }

        return new PenetrationResult(finalDamage, penetrated, partial);
    }

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

    public static int getDynamicProtectionLevel(ItemStack armorStack, ModDamagePart part) {
        ArmorData data = ArmorDataLoader.getArmorData(armorStack.getItem());
        if (data == null) return 0;
        int baseLevel = data.getProtectionLevel(part);
        if (baseLevel == 0) return 0;
        int maxDurability = armorStack.getMaxDamage();
        if (maxDurability <= 0) return baseLevel;
        int currentDurability = maxDurability - armorStack.getDamageValue();
        if (currentDurability <= 1) return 0;
        float durabilityPercent = (float) currentDurability / (float) maxDurability;
        float multiplier = 0.5f + 0.5f * durabilityPercent;
        return Math.max(1, Math.round(baseLevel * multiplier));
    }

    public static int getProtectionLevel(LivingEntity target, ModDamagePart hitPart) {
        return getArmorLevel(target, hitPart);
    }

    public static int getArmorLevel(LivingEntity target, ModDamagePart hitPart) {
        List<Integer> levels = new ArrayList<>();
        int natural = EntityHitboxHelper.getNaturalArmor(target, hitPart);
        if (natural > 0) levels.add(natural);

        List<ItemStack> protectingItems = getAllProtectingItems(target, hitPart);
        for (ItemStack stack : protectingItems) {
            int level = getDynamicProtectionLevel(stack, hitPart);
            if (level > 0) levels.add(level);
        }

        if (levels.isEmpty()) return 0;
        levels.sort(Collections.reverseOrder());
        int base = levels.get(0);
        if (levels.size() == 1) {
            int total = base;
            if (!target.level().isClientSide) {
                GetArmorLevelEvent event = new GetArmorLevelEvent(target, hitPart, total);
                MinecraftForge.EVENT_BUS.post(event);
                total = event.getArmorLevel();
            }
            return total;
        }
        int otherSum = 0;
        for (int i = 1; i < levels.size(); i++) otherSum += levels.get(i);
        ModClothConfig config = ModClothConfig.get();
        float factor = config.armorStackingFactor;
        int total = base + Math.round(otherSum * factor);
        int cap = config.armorCap;
        if (cap > 0 && total > cap) total = cap;

        if (!target.level().isClientSide) {
            GetArmorLevelEvent event = new GetArmorLevelEvent(target, hitPart, total);
            MinecraftForge.EVENT_BUS.post(event);
            total = event.getArmorLevel();
        }
        return total;
    }

    private static EquipmentSlot getSlotForItem(LivingEntity target, ItemStack itemStack) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (target.getItemBySlot(slot) == itemStack) return slot;
        }
        return null;
    }

    private static List<ItemStack> getAllProtectingItems(LivingEntity target, ModDamagePart hitPart) {
        List<ItemStack> result = new ArrayList<>();

        // 1. 遍历盔甲槽位
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack stack = target.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            ArmorData data = ArmorDataLoader.getArmorData(stack.getItem());
            if (data != null && data.protects(hitPart)) {
                result.add(stack);
            }
        }

        // 2. 遍历 Curios 饰品槽位（如果 Curios 已加载）
        if (ModList.get().isLoaded("curios")) {
            CuriosApi.getCuriosInventory(target).ifPresent(handler -> {
                for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                    IDynamicStackHandler stackHandler = stacksHandler.getStacks(); // 只取普通槽位，不取 cosmetic
                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        ItemStack stack = stackHandler.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            ArmorData data = ArmorDataLoader.getArmorData(stack.getItem());
                            if (data != null && data.protects(hitPart)) {
                                result.add(stack);
                            }
                        }
                    }
                }
            });
        }
        return result;
    }

    public static class ProtectionSource {
        public final ItemStack item;
        public final int level;          // 当前防护等级（已考虑耐久）
        public final int toughness;      // 韧性
        public final float materialFactor;
        public final float ricochetChance;
        public final boolean isNatural;

        public ProtectionSource(ItemStack item, int level, int toughness, float materialFactor, float ricochetChance, boolean isNatural) {
            this.item = item;
            this.level = level;
            this.toughness = toughness;
            this.materialFactor = materialFactor;
            this.ricochetChance = ricochetChance;
            this.isNatural = isNatural;
        }
    }

    /**
     * 获取针对子部位的所有防护源（包括自然护甲和装备）
     */
    private static List<ProtectionSource> getProtectionSourcesForSubPart(LivingEntity target, ModDamageSubPart subPart) {
        List<ProtectionSource> sources = new ArrayList<>();
        ModDamagePart parent = subPart.getParent();

        // 1. 自然护甲（仍按父部位）
        int natural = EntityHitboxHelper.getNaturalArmor(target, parent);
        if (natural > 0) {
            sources.add(new ProtectionSource(null, natural, 0, 1.0f, 0f, true));
        }

        // 2. 遍历盔甲槽位
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack stack = target.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            ArmorData data = ArmorDataLoader.getArmorData(stack.getItem());
            if (data == null) continue;

            // 优先检查子部位配置，若无则检查父部位
            int subLevel = data.getSubProtectionLevel(subPart);
            int level = subLevel > 0 ? subLevel : data.getProtectionLevel(parent);
            if (level == 0) continue;
            level = getDynamicProtectionLevel(stack, level); // 应用耐久动态调整

            int toughness = data.getEffectiveToughness(subPart, parent);
            toughness = getDynamicToughness(stack, toughness); // 应用耐久动态调整（需要重载）

            float materialFactor = data.getMaterialFactor(parent); // 材料因子不分子部位
            float ricochet = data.getEffectiveRicochetChance(subPart, parent);

            sources.add(new ProtectionSource(stack, level, toughness, materialFactor, ricochet, false));
        }

        // 3. 遍历 Curios 饰品槽位（如果 Curios 已加载）
        if (ModList.get().isLoaded("curios")) {
            CuriosApi.getCuriosInventory(target).ifPresent(handler -> {
                for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                    IDynamicStackHandler stackHandler = stacksHandler.getStacks();
                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        ItemStack stack = stackHandler.getStackInSlot(i);
                        if (stack.isEmpty()) continue;
                        ArmorData data = ArmorDataLoader.getArmorData(stack.getItem());
                        if (data == null) continue;
                        int subLevel = data.getSubProtectionLevel(subPart);
                        int level = subLevel > 0 ? subLevel : data.getProtectionLevel(parent);
                        if (level == 0) continue;
                        level = getDynamicProtectionLevel(stack, level);

                        int toughness = data.getEffectiveToughness(subPart, parent);
                        toughness = getDynamicToughness(stack, toughness);

                        float materialFactor = data.getMaterialFactor(parent);
                        float ricochet = data.getEffectiveRicochetChance(subPart, parent);

                        sources.add(new ProtectionSource(stack, level, toughness, materialFactor, ricochet, false));
                    }
                }
            });
        }

        return sources;
    }

    /**
     * 根据子部位获取总护甲等级（自然护甲 + 装备，支持子部位优先）
     */
    public static int getArmorLevelForSubPart(LivingEntity target, ModDamageSubPart subPart) {
        List<ProtectionSource> sources = getProtectionSourcesForSubPart(target, subPart);
        if (sources.isEmpty()) return 0;

        // 按等级降序排序
        sources.sort((a, b) -> Integer.compare(b.level, a.level));
        int highest = sources.get(0).level;
        if (sources.size() == 1) {
            return highest;
        }

        ModClothConfig config = ModClothConfig.get();
        float factor = config.armorStackingFactor;
        int otherSum = 0;
        for (int i = 1; i < sources.size(); i++) {
            otherSum += sources.get(i).level;
        }
        int total = highest + Math.round(otherSum * factor);
        int cap = config.armorCap;
        if (cap > 0 && total > cap) total = cap;

        // 触发事件（暂不传子部位，保持原事件签名）
        if (!target.level().isClientSide) {
            GetArmorLevelEvent event = new GetArmorLevelEvent(target, subPart.getParent(), total);
            MinecraftForge.EVENT_BUS.post(event);
            total = event.getArmorLevel();
        }
        return total;
    }

    /**
     * 获取子部位的总韧性（自然韧性 + 装备韧性）
     */
    private static int getTotalToughnessForSubPart(LivingEntity target, ModDamageSubPart subPart) {
        ModDamagePart parent = subPart.getParent();
        int natural = EntityHitboxHelper.getNaturalToughness(target, parent);
        int total = natural;

        List<ProtectionSource> sources = getProtectionSourcesForSubPart(target, subPart);
        for (ProtectionSource source : sources) {
            if (!source.isNatural) {
                total += source.toughness;
            }
        }
        return Math.min(total, 100);
    }

    /**
     * 重载：针对子部位应用护甲穿透计算
     */
    public static PenetrationResult applyArmorPenetration(LivingEntity target, ModDamageSubPart subPart,
                                                          float originalDamage, float penetration) {
        if (originalDamage <= 0) return new PenetrationResult(0, false, false);

        int armorLevel = getArmorLevelForSubPart(target, subPart);
        ModClothConfig config = ModClothConfig.get();
        ModDamagePart parent = subPart.getParent();

        List<ProtectionSource> sources = getProtectionSourcesForSubPart(target, subPart);

        // ========== 跳弹判定（所有保护源，任意一个触发即跳弹） ==========
        boolean ricochetTriggered = false;
        if (config.enableRicochet) {
            for (ProtectionSource source : sources) {
                if (source.isNatural) continue;
                if (source.ricochetChance > 0 && target.getRandom().nextFloat() < source.ricochetChance) {
                    ricochetTriggered = true;
                    break;
                }
            }
        }
        if (ricochetTriggered) {
            float ricochetDamage = originalDamage * config.ricochetDamageRatio;
            if (ricochetDamage < 1) ricochetDamage = 1;
            final float finalRicochetDamage = ricochetDamage;   // 修复 lambda final 问题
            if (config.debugMode) {
                // 获取任意一个触发跳弹的物品（用于日志）
                ItemStack anyItem = sources.stream()
                        .filter(s -> !s.isNatural && s.item != null)
                        .map(s -> s.item)
                        .findFirst().orElse(null);
                ModernDamage.LOGGER.info("[Ricochet] {} ricocheted off {} (subPart {})! Damage: {} -> {}",
                        target.getName().getString(),
                        anyItem != null ? ForgeRegistries.ITEMS.getKey(anyItem.getItem()) : "unknown",
                        subPart, originalDamage, finalRicochetDamage);
            }
            // 伤害分配（使用父部位）
            if (target instanceof Player && config.damageModel == ModClothConfig.DamageModel.HARDCORE) {
                target.getCapability(PartHealthCapability.PART_HEALTH_CAP).ifPresent(cap -> cap.damagePart(parent, finalRicochetDamage));
            } else if (config.creaturePartHealthEnabled && config.damageModel == ModClothConfig.DamageModel.HARDCORE) {
                target.getCapability(CreaturePartHealthCapability.CREATURE_PART_HEALTH_CAP).ifPresent(cap -> cap.damagePart(parent, finalRicochetDamage));
            } else {
                target.setHealth(Math.max(0, target.getHealth() - finalRicochetDamage));
            }
            return new PenetrationResult(finalRicochetDamage, false, false);
        }

        // ========== 继续原有穿透计算 ==========
        if (armorLevel == 0) {
            return new PenetrationResult(originalDamage, false, false);
        }

        int penetrationValue = (int) (penetration * 100);
        int baseDurabilityLoss = config.durabilityLossBase;
        int maxLoss = config.maxDurabilityLoss;
        float partialMin = config.partialPenetrationMinRatio;
        float partialMax = config.partialPenetrationMaxRatio;

        float finalDamageBeforeToughness;
        boolean penetrated = false;
        boolean partial = false;

        if (penetrationValue >= armorLevel + 5) {
            finalDamageBeforeToughness = originalDamage;
            penetrated = true;
        } else if (penetrationValue >= armorLevel - 10) {
            int diff = (armorLevel + 6) - penetrationValue;
            double chance = (16.0 - diff) / 15.0;
            chance = Math.min(1.0, Math.max(0.0, chance));
            if (RANDOM.nextDouble() <= chance) {
                float ratio = partialMin + RANDOM.nextFloat() * (partialMax - partialMin);
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

        // 耐久损耗
        if (!sources.isEmpty()) {
            float penRatio = (float) penetrationValue / (float) armorLevel;
            boolean cancelDurabilityLoss = false;
            if (!target.level().isClientSide) {
                ArmorHitEvent event = new ArmorHitEvent(target, parent, originalDamage, finalDamageBeforeToughness, armorLevel);
                MinecraftForge.EVENT_BUS.post(event);
                if (event.isCanceled()) {
                    cancelDurabilityLoss = true;
                }
                finalDamageBeforeToughness = event.getFinalDamage();
            }

            if (!cancelDurabilityLoss) {
                // 主要物品（最高等级）损耗全额
                ProtectionSource primary = sources.isEmpty() ? null : sources.get(0);
                if (primary != null && !primary.isNatural && primary.item != null && !primary.item.isEmpty()) {
                    int durabilityLoss = (int) Math.ceil(1 + finalDamageBeforeToughness * penRatio * primary.materialFactor);
                    durabilityLoss = Math.min(maxLoss, Math.max(1, durabilityLoss));
                    hurtItem(primary.item, target, durabilityLoss);
                }
                // 其他保护物品损耗减半
                for (ProtectionSource source : sources) {
                    if (source.isNatural) continue;
                    if (source.item == primary.item) continue;
                    int durabilityLoss = (int) Math.ceil(1 + finalDamageBeforeToughness * penRatio * source.materialFactor);
                    durabilityLoss = Math.min(maxLoss, Math.max(1, durabilityLoss / 2));
                    hurtItem(source.item, target, durabilityLoss);
                }
            }
        }

        int totalToughness = getTotalToughnessForSubPart(target, subPart);
        float finalDamage = finalDamageBeforeToughness;
        if (totalToughness > 0) {
            float reduction = totalToughness / 100.0f;
            finalDamage = finalDamageBeforeToughness * (1 - reduction);
            if (finalDamage < 0) finalDamage = 0;
        }

        if (config.debugMode) {
            ModernDamage.LOGGER.info("[Penetration] target={}, subPart={}, armor={}, pen={}, dmg: orig={}, beforeTough={}, final={}, toughness={}, penType={}",
                    target.getName().getString(), subPart, armorLevel, penetrationValue,
                    originalDamage, finalDamageBeforeToughness, finalDamage, totalToughness,
                    penetrated ? "FULL" : (partial ? "PARTIAL" : "BLUNT"));
        }

        return new PenetrationResult(finalDamage, penetrated, partial);
    }

    // 辅助方法：动态护甲等级（根据耐久百分比调整）
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

    // 辅助方法：动态韧性（根据耐久百分比调整）
    private static int getDynamicToughness(ItemStack stack, int baseToughness) {
        if (baseToughness == 0) return 0;
        int maxDurability = stack.getMaxDamage();
        if (maxDurability <= 0) return baseToughness;
        int currentDurability = maxDurability - stack.getDamageValue();
        float durabilityPercent = (float) currentDurability / (float) maxDurability;
        return Math.round(baseToughness * durabilityPercent);
    }

    public static int getDynamicProtectionLevel(ItemStack armorStack, ModDamageSubPart subPart) {
        ArmorData data = ArmorDataLoader.getArmorData(armorStack.getItem());
        if (data == null) return 0;
        int baseLevel = data.getSubProtectionLevel(subPart);
        if (baseLevel == 0) return 0;
        int maxDurability = armorStack.getMaxDamage();
        if (maxDurability <= 0) return baseLevel;
        int currentDurability = maxDurability - armorStack.getDamageValue();
        if (currentDurability <= 1) return 0;
        float durabilityPercent = (float) currentDurability / (float) maxDurability;
        float multiplier = 0.5f + 0.5f * durabilityPercent;
        return Math.max(1, Math.round(baseLevel * multiplier));
    }


    public static int getDynamicToughness(ItemStack armorStack, ModDamageSubPart subPart) {
        ArmorData data = ArmorDataLoader.getArmorData(armorStack.getItem());
        if (data == null) return 0;
        int base = data.getSubToughness(subPart);
        if (base <= 0) {
            // 回退到父部位韧性
            base = data.getBaseToughness(subPart.getParent());
        }
        if (base <= 0) return 0;
        int maxDurability = armorStack.getMaxDamage();
        if (maxDurability <= 0) return base;
        int currentDurability = maxDurability - armorStack.getDamageValue();
        float durabilityPercent = (float) currentDurability / (float) maxDurability;
        return Math.round(base * durabilityPercent);
    }
}