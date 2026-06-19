package com.moderndamage.control.event;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.api.ModDamageSubPart;
import com.moderndamage.control.armor.ArmorCalculator;
import com.moderndamage.control.attribute.ModAttributes;
import com.moderndamage.control.capability.parthealth.CreaturePartHealthCapability;
import com.moderndamage.control.capability.parthealth.PartHealthCapability;
import com.moderndamage.control.compat.hitbox.BodypartHitbox;
import com.moderndamage.control.compat.hitbox.CoordinateTransform;
import com.moderndamage.control.compat.hitbox.EnumPlayerPart;
import com.moderndamage.control.compat.hitbox.SubBodypartHitbox;
import com.moderndamage.control.config.EffectEntry;
import com.moderndamage.control.config.EntityBodyPartEffectConfig;
import com.moderndamage.control.config.EntityEffectConfig;
import com.moderndamage.control.config.ModClothConfig;
import com.moderndamage.control.entity.EntityHitboxHelper;
import com.moderndamage.control.mixininterface.EntityKineticBulletExtension;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InjuryEventHandler {
    private static final Random RANDOM = new Random();
    private static final ConcurrentHashMap<UUID, Boolean> SOFT_DEATH_MARK = new ConcurrentHashMap<>();

    // ======================== 核心事件 ========================

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;

        DamageSource source = event.getSource();
        ModernDamage.LOGGER.info("=== Hurt Event: target={}, source={}, originalDamage={}",
                target.getName().getString(), source.getMsgId(), event.getAmount());

        if (!target.isAlive() || target.isDeadOrDying()) {
            event.setCanceled(true);
            return;
        }

        float originalDamage = event.getAmount();
        Entity directEntity = source.getDirectEntity();
        Entity attacker = source.getEntity();

        ModClothConfig config = ModClothConfig.get();
        boolean isHardcore = config.damageModel == ModClothConfig.DamageModel.HARDCORE;

        if (isHardcore && !(target instanceof Player)) {
            target.getCapability(CreaturePartHealthCapability.CREATURE_PART_HEALTH_CAP).ifPresent(cap -> {
                if (cap.getTotalHealthPercent() <= 0.001f && target.isAlive()) {
                    ModernDamage.LOGGER.warn("Detected {} alive with zero part health, resetting", target.getName().getString());
                    cap.reset();
                }
            });
        }

        // ======================== 1. TaCZ 子弹 ========================
        if (directEntity instanceof EntityKineticBullet bullet) {
            handleBulletHit(target, bullet, source, originalDamage, attacker, config, isHardcore);
            event.setCanceled(true);
            return;
        }

        // ======================== 2. 原版弹射物（箭、三叉戟） ========================
        if (directEntity instanceof AbstractArrow || directEntity instanceof ThrownTrident) {
            handleProjectileHit(target, (Projectile) directEntity, source, originalDamage, attacker, config, isHardcore);
            event.setCanceled(true);
            return;
        }

        // ======================== 3. 其他伤害 ========================
        boolean ignoreArmor = isArmorIgnored(source);
        float finalDamage;
        if (ignoreArmor) {
            finalDamage = originalDamage;
        } else {
            // 获取攻击者穿透属性
            float penetration = 0f;
            if (attacker instanceof LivingEntity livingAttacker) {
                double attrPen = livingAttacker.getAttributeValue(ModAttributes.PENETRATION.get());
                penetration = (float) attrPen;
            }
            ArmorCalculator.PenetrationResult result =
                    ArmorCalculator.applyArmorPenetration(target, ModDamagePart.CHEST, originalDamage, penetration);
            finalDamage = result.finalDamage;
            if (config.debugMode && attacker instanceof Player attackerPlayer) {
                ModernDamage.LOGGER.info("[ModernDamage] {} damaged {} ({}): {} -> {} ({}%)",
                        attackerPlayer.getName().getString(), target.getName().getString(), source.getMsgId(),
                        originalDamage, finalDamage, (int)((1 - finalDamage/originalDamage)*100));
            }
        }

        if (isHardcore) {
            event.setCanceled(true);
            boolean died = false;
            String msgId = source.getMsgId();

            // ---- 全局真实伤害（full body） ----
            if (isFullBodyDamage(source)) {
                died = applyFullBodyDamage(target, originalDamage, source, config); // 使用originalDamage，因为full body通常无视护甲
                if (died) return;
            }
            // ---- 高温地面（真实伤害，双腿） ----
            else if (msgId.equals("hotFloor")) {
                float half = originalDamage / 2.0f;
                died = applyPartDamage(target, ModDamagePart.LEFT_LEG, half, source, true, config);
                if (died) return;
                died = applyPartDamage(target, ModDamagePart.RIGHT_LEG, half, source, true, config);
                if (died) return;
            }
            // ---- 摔落 / 尖刺（护甲减免最多 20%） ----
            else if (msgId.equals("fall") || msgId.equals("stalagmite")) {
                int leftArmor = ArmorCalculator.getArmorLevel(target, ModDamagePart.LEFT_LEG);
                int rightArmor = ArmorCalculator.getArmorLevel(target, ModDamagePart.RIGHT_LEG);
                int avgArmor = (leftArmor + rightArmor) / 2;
                float reduction = Math.min(config.fallArmorReductionCap, avgArmor * 0.04f);
                float reducedDamage = originalDamage * (1 - reduction);

                float half = reducedDamage / 2.0f;
                died = applyPartDamage(target, ModDamagePart.LEFT_LEG, half, source, true, config);
                if (died) return;
                died = applyPartDamage(target, ModDamagePart.RIGHT_LEG, half, source, true, config);
                if (died) return;
            }
            // ---- 窒息（真实伤害，头部） ----
            else if (msgId.equals("inWall")) {
                died = applyPartDamage(target, ModDamagePart.HEAD, originalDamage, source, true, config);
                if (died) return;
            }
            // ---- 撞墙（钝伤，护甲减免最多 20%，头部） ----
            else if (msgId.equals("flyIntoWall")) {
                int armor = ArmorCalculator.getArmorLevel(target, ModDamagePart.HEAD);
                float reduction = Math.min(config.flyIntoWallArmorReductionCap, armor * 0.04f);
                float reducedDamage = originalDamage * (1 - reduction);
                died = applyPartDamage(target, ModDamagePart.HEAD, reducedDamage, source, true, config);
                if (died) return;
            }
            // ---- 爆炸（冲击波真伤 + 破片低穿甲） ----
            else if (isExplosionDamage(source)) {
                handleExplosionDamage(target, source, originalDamage, config);
                if (!target.isAlive()) return;
            }
            // ---- 仙人掌（随机四肢，护甲已由 finalDamage 处理） ----
            else if (msgId.equals("cactus")) {
                // finalDamage 已经过护甲计算（基于胸部），此处使用它
                died = applyPartDamage(target, randomLimb(), finalDamage, source, true, config);
                if (died) return;
            }
            // ---- 烟花 / 螫刺（随机非重要部位） ----
            else if (msgId.equals("fireworks") || msgId.equals("sting")) {
                died = applyPartDamage(target, randomNonVital(), finalDamage, source, true, config);
                if (died) return;
            }
            // ---- 闪电（半头半胸） ----
            else if (msgId.equals("lightningBolt")) {
                float half = originalDamage / 2;
                died = applyPartDamage(target, ModDamagePart.HEAD, half, source, true, config);
                if (died) return;
                died = applyPartDamage(target, ModDamagePart.CHEST, half, source, true, config);
                if (died) return;
            }
            // ---- 间接魔法（胸部） ----
            else if (msgId.equals("indirectMagic")) {
                died = applyPartDamage(target, ModDamagePart.CHEST, finalDamage, source, true, config);
                if (died) return;
            }
            // ---- 荆棘反伤（魔法性质，无视护甲，打手臂） ----
            else if (msgId.equals("thorns")) {
                ModDamagePart armPart = ModDamagePart.RIGHT_ARM;
                if (target instanceof Player player && player.getMainHandItem().isEmpty()) {
                    armPart = RANDOM.nextBoolean() ? ModDamagePart.LEFT_ARM : ModDamagePart.RIGHT_ARM;
                }
                died = applyPartDamage(target, armPart, originalDamage, source, true, config);
                if (died) return;
            }
            // ---- 其他（默认：按攻击者高度随机部位） ----
            else {
                ModDamagePart randomPart = ModDamagePart.CHEST;
                if (attacker instanceof LivingEntity livingAttacker) {
                    randomPart = getRandomPartByPosition(livingAttacker, target);
                }
                died = applyPartDamage(target, randomPart, finalDamage, source, true, config);
                if (died) return;
            }

            // 应用药水效果（部位触发）
            applyPotionEffects(target, ModDamagePart.CHEST, finalDamage, source);

        } else {
            // ======================== 软核模式 ========================
            event.setAmount(finalDamage);
            applyPotionEffects(target, ModDamagePart.CHEST, finalDamage, source);
        }
    }

    // ======================== 各伤害处理子函数 ========================

    private void handleBulletHit(LivingEntity target, EntityKineticBullet bullet, DamageSource source,
                                 float originalDamage, Entity attacker, ModClothConfig config, boolean isHardcore) {
        ModDamagePart hitPart = ModDamagePart.CHEST;
        ModDamageSubPart subPart = null;

        if (target instanceof Player player) {
            Vec3 hitPos = getHitLocationFromBullet(bullet);
            if (hitPos != null) {
                Vec3 localPos = CoordinateTransform.worldToLocal(hitPos, player);
                if (config.enablePreciseHitbox && isHardcore) {
                    boolean isCrawling = false;
                    try {
                        IGunOperator operator = IGunOperator.fromLivingEntity(player);
                        if (operator != null && operator.getDataHolder() != null) {
                            isCrawling = operator.getDataHolder().isCrawling;
                        }
                    } catch (Throwable ignored) {}
                    subPart = SubBodypartHitbox.getSubPart(localPos, isCrawling);
                    hitPart = subPart.getParent();
                } else {
                    EnumPlayerPart rawPart = BodypartHitbox.getHitPart(localPos);
                    if (rawPart == null) rawPart = BodypartHitbox.getClosestPart(localPos);
                    hitPart = refinePart(rawPart, localPos);
                }
            }
        } else {
            Vec3 hitPos = getHitLocationFromBullet(bullet);
            if (hitPos != null) {
                Vec3 localPos = hitPos.subtract(target.position());
                hitPart = EntityHitboxHelper.getHitPart(target, localPos);
            }
        }

        float penetration = getPenetrationFromShooter(attacker);
        ArmorCalculator.PenetrationResult result;
        if (subPart != null) {
            result = ArmorCalculator.applyArmorPenetration(target, subPart, originalDamage, penetration);
        } else {
            result = ArmorCalculator.applyArmorPenetration(target, hitPart, originalDamage, penetration);
        }
        float finalDamage = result.finalDamage;

        if (config.debugMode && attacker instanceof Player attackerPlayer) {
            float reductionPercent = originalDamage > 0 ? (1 - finalDamage / originalDamage) * 100 : 0;
            String status = result.penetrated ? "PENETRATED" : (result.partial ? "PARTIAL" : "BLUNT");
            String partInfo = (subPart != null) ? (hitPart + "(" + subPart + ")") : hitPart.toString();
            String msg = String.format("[ModernDamage] %s hit %s(%s): %.1f -> %.1f (%.0f%% reduction, %s)",
                    attackerPlayer.getName().getString(), target.getName().getString(), partInfo,
                    originalDamage, finalDamage, reductionPercent, status);
            attackerPlayer.sendSystemMessage(Component.literal(msg));
            ModernDamage.LOGGER.info(msg);
        }

        if (isHardcore) {
            applyPartDamage(target, hitPart, finalDamage, source, true, config);
        } else {
            applySoftcoreDamage(target, finalDamage, source);
        }
        applyPotionEffects(target, hitPart, finalDamage, source);
    }

    private void handleProjectileHit(LivingEntity target, Projectile projectile, DamageSource source,
                                     float originalDamage, Entity attacker, ModClothConfig config, boolean isHardcore) {
        ModDamagePart hitPart = ModDamagePart.CHEST;
        ModDamageSubPart subPart = null;

        Vec3 hitPos = getHitLocationFromProjectile(projectile, target);
        if (target instanceof Player player) {
            Vec3 localPos = CoordinateTransform.worldToLocal(hitPos, player);
            if (config.enablePreciseHitbox && isHardcore) {
                boolean isCrawling = false;
                try {
                    IGunOperator operator = IGunOperator.fromLivingEntity(player);
                    if (operator != null && operator.getDataHolder() != null) {
                        isCrawling = operator.getDataHolder().isCrawling;
                    }
                } catch (Throwable ignored) {}
                subPart = SubBodypartHitbox.getSubPart(localPos, isCrawling);
                hitPart = subPart.getParent();
            } else {
                EnumPlayerPart rawPart = BodypartHitbox.getHitPart(localPos);
                if (rawPart == null) rawPart = BodypartHitbox.getClosestPart(localPos);
                hitPart = refinePart(rawPart, localPos);
            }
        } else {
            Vec3 localPos = hitPos.subtract(target.position());
            hitPart = EntityHitboxHelper.getHitPart(target, localPos);
        }

        float penetration = 0f;
        if (attacker instanceof LivingEntity livingAttacker) {
            double attrPen = livingAttacker.getAttributeValue(ModAttributes.PENETRATION.get());
            penetration = (float) attrPen;
        }

        ArmorCalculator.PenetrationResult result;
        if (subPart != null) {
            result = ArmorCalculator.applyArmorPenetration(target, subPart, originalDamage, penetration);
        } else {
            result = ArmorCalculator.applyArmorPenetration(target, hitPart, originalDamage, penetration);
        }
        float finalDamage = result.finalDamage;

        if (config.debugMode && attacker instanceof Player attackerPlayer) {
            float reductionPercent = originalDamage > 0 ? (1 - finalDamage / originalDamage) * 100 : 0;
            String status = result.penetrated ? "PENETRATED" : (result.partial ? "PARTIAL" : "BLUNT");
            String partInfo = (subPart != null) ? (hitPart + "(" + subPart + ")") : hitPart.toString();
            String msg = String.format("[ModernDamage] %s hit %s(%s) with projectile: %.1f -> %.1f (%.0f%% reduction, %s)",
                    attackerPlayer.getName().getString(), target.getName().getString(), partInfo,
                    originalDamage, finalDamage, reductionPercent, status);
            attackerPlayer.sendSystemMessage(Component.literal(msg));
            ModernDamage.LOGGER.info(msg);
        }

        if (isHardcore) {
            applyPartDamage(target, hitPart, finalDamage, source, true, config);
        } else {
            applySoftcoreDamage(target, finalDamage, source);
        }
        applyPotionEffects(target, hitPart, finalDamage, source);
    }

    private void handleExplosionDamage(LivingEntity target, DamageSource source,
                                       float originalDamage, ModClothConfig config) {
        float blastBase = originalDamage * config.explosionBlastRatio;       // 冲击波部分
        float fragBase = originalDamage * (1 - config.explosionBlastRatio); // 破片部分
        float fragPen = config.explosionFragPenetration;

        // ---- 胸部 (50%) ----
        applyPartDamage(target, ModDamagePart.CHEST, blastBase * 0.5f, source, true, config);
        if (!target.isAlive()) return;
        ArmorCalculator.applyArmorPenetration(target, ModDamagePart.CHEST, fragBase * 0.5f, fragPen);
        if (!target.isAlive()) return;

        // ---- 左腿 (15%) ----
        applyPartDamage(target, ModDamagePart.LEFT_LEG, blastBase * 0.15f, source, true, config);
        if (!target.isAlive()) return;
        ArmorCalculator.applyArmorPenetration(target, ModDamagePart.LEFT_LEG, fragBase * 0.15f, fragPen);
        if (!target.isAlive()) return;

        // ---- 右腿 (15%) ----
        applyPartDamage(target, ModDamagePart.RIGHT_LEG, blastBase * 0.15f, source, true, config);
        if (!target.isAlive()) return;
        ArmorCalculator.applyArmorPenetration(target, ModDamagePart.RIGHT_LEG, fragBase * 0.15f, fragPen);
        if (!target.isAlive()) return;

        // ---- 剩余 20% 随机非重要部位 ----
        ModDamagePart otherPart = randomNonVital();
        applyPartDamage(target, otherPart, blastBase * 0.2f, source, true, config);
        if (!target.isAlive()) return;
        ArmorCalculator.applyArmorPenetration(target, otherPart, fragBase * 0.2f, fragPen);
    }

    // ======================== 辅助判断方法 ========================

    private boolean isArmorIgnored(DamageSource source) {
        String msgId = source.getMsgId();
        return switch (msgId) {
            case "inFire", "onFire", "lava", "hotFloor",
                 "drown", "freeze", "inWall",
                 "magic", "indirectMagic",
                 "sonic_boom", "wither",
                 "starve", "outOfWorld",
                 "thorns" -> true;
            default -> false;
        };
    }

    private boolean isFullBodyDamage(DamageSource source) {
        String msg = source.getMsgId();
        return switch (msg) {
            case "inFire", "onFire", "lava", "drown", "starve", "magic", "wither", "freeze", "sonic_boom", "outOfWorld" -> true;
            default -> false;
        };
    }

    private boolean isExplosionDamage(DamageSource source) {
        String msg = source.getMsgId();
        return msg.equals("explosion") || msg.equals("explosion.player") || msg.equals("fireball") || msg.equals("fireball.player");
    }

    private float getPenetrationFromShooter(Entity attacker) {
        float pen = 0f;
        if (attacker instanceof LivingEntity living) {
            double attrPen = living.getAttributeValue(ModAttributes.PENETRATION.get());
            pen = (float) attrPen;
            if (attacker instanceof Player player) {
                // 可附加物品穿透等
            }
        }
        return pen;
    }

    // ======================== 伤害应用核心 ========================

    private boolean applyPartDamage(LivingEntity target, ModDamagePart part, float amount,
                                    DamageSource source, boolean isHardcore, ModClothConfig config) {
        if (!isHardcore) {
            float newHealth = target.getHealth() - amount;
            if (newHealth <= 0) {
                target.die(source);
                return true;
            }
            target.setHealth(newHealth);
            return false;
        }
        if (target instanceof Player player) {
            player.getCapability(PartHealthCapability.PART_HEALTH_CAP)
                    .ifPresent(cap -> cap.damagePart(part, amount, source));
        } else if (config.creaturePartHealthEnabled) {
            target.getCapability(CreaturePartHealthCapability.CREATURE_PART_HEALTH_CAP)
                    .ifPresent(cap -> cap.damagePart(part, amount, source));
        } else {
            boolean died = target.hurt(source, amount);
            return died;
        }
        return !target.isAlive();
    }

    private boolean applyFullBodyDamage(LivingEntity target, float amount, DamageSource source, ModClothConfig config) {
        if (target instanceof Player player) {
            player.getCapability(PartHealthCapability.PART_HEALTH_CAP).ifPresent(cap -> cap.damageAll(amount));
        } else if (config.creaturePartHealthEnabled) {
            target.getCapability(CreaturePartHealthCapability.CREATURE_PART_HEALTH_CAP).ifPresent(cap -> cap.damageAll(amount));
        } else {
            float newHealth = target.getHealth() - amount;
            if (newHealth <= 0) {
                target.die(source);
                return true;
            }
            target.setHealth(newHealth);
            return false;
        }
        return !target.isAlive();
    }

    private void applySoftcoreDamage(LivingEntity target, float amount, DamageSource source) {
        if (target.isAlive() && SOFT_DEATH_MARK.containsKey(target.getUUID())) {
            ModernDamage.LOGGER.warn("Clearing soft death mark for revived entity in applySoftcoreDamage: {}", target.getName().getString());
            SOFT_DEATH_MARK.remove(target.getUUID());
        }

        if (SOFT_DEATH_MARK.containsKey(target.getUUID())) {
            return;
        }

        float newHealth = target.getHealth() - amount;
        if (newHealth <= 0) {
            SOFT_DEATH_MARK.put(target.getUUID(), true);
            target.setHealth(0);
            target.die(source);
        } else {
            target.setHealth(newHealth);
        }
    }

    // ======================== 部位判定辅助 ========================

    private ModDamagePart getRandomPartByPosition(LivingEntity attacker, LivingEntity target) {
        double dy = attacker.getY() - target.getY();
        double threshold = 1.0;
        if (dy >= threshold) {
            int roll = RANDOM.nextInt(100);
            if (roll < 40) return ModDamagePart.HEAD;
            if (roll < 70) return ModDamagePart.CHEST;
            return RANDOM.nextBoolean() ? ModDamagePart.LEFT_ARM : ModDamagePart.RIGHT_ARM;
        } else if (dy <= -threshold) {
            int roll = RANDOM.nextInt(100);
            if (roll < 30) return ModDamagePart.STOMACH;
            return RANDOM.nextBoolean() ? ModDamagePart.LEFT_LEG : ModDamagePart.RIGHT_LEG;
        } else {
            int roll = RANDOM.nextInt(100);
            if (roll < 15) return ModDamagePart.HEAD;
            if (roll < 55) return ModDamagePart.CHEST;
            if (roll < 70) return ModDamagePart.STOMACH;
            return RANDOM.nextBoolean() ? ModDamagePart.LEFT_ARM : ModDamagePart.RIGHT_ARM;
        }
    }

    private ModDamagePart refinePart(EnumPlayerPart raw, Vec3 localPos) {
        switch (raw) {
            case HEAD: return ModDamagePart.HEAD;
            case LEFT_ARM: return ModDamagePart.LEFT_ARM;
            case RIGHT_ARM: return ModDamagePart.RIGHT_ARM;
            case LEFT_LEG: case LEFT_FOOT: return ModDamagePart.LEFT_LEG;
            case RIGHT_LEG: case RIGHT_FOOT: return ModDamagePart.RIGHT_LEG;
            case BODY:
                return localPos.y >= 0.9 ? ModDamagePart.CHEST : ModDamagePart.STOMACH;
            default: return ModDamagePart.CHEST;
        }
    }

    private ModDamagePart randomLimb() {
        ModDamagePart[] limbs = {ModDamagePart.LEFT_ARM, ModDamagePart.RIGHT_ARM,
                ModDamagePart.LEFT_LEG, ModDamagePart.RIGHT_LEG};
        return limbs[RANDOM.nextInt(limbs.length)];
    }

    private ModDamagePart randomNonVital() {
        ModDamagePart[] parts = {ModDamagePart.STOMACH, ModDamagePart.LEFT_ARM,
                ModDamagePart.RIGHT_ARM, ModDamagePart.LEFT_LEG, ModDamagePart.RIGHT_LEG};
        return parts[RANDOM.nextInt(parts.length)];
    }

    // ======================== 命中位置获取 ========================

    private Vec3 getHitLocationFromBullet(EntityKineticBullet bullet) {
        if (bullet instanceof EntityKineticBulletExtension ext) {
            return ext.moderndamage$getLastHitLocation();
        }
        return null;
    }

    private Vec3 getHitLocationFromProjectile(Projectile projectile, LivingEntity target) {
        Vec3 projPos = projectile.position();
        AABB targetBox = target.getBoundingBox();
        Vec3 targetCenter = target.position().add(0, target.getBbHeight() / 2, 0);
        Vec3 direction = targetCenter.subtract(projPos).normalize();
        return targetBox.clip(projPos, projPos.add(direction.scale(5.0))).orElse(targetCenter);
    }

    // ======================== 药水效果触发 ========================

    private void applyPotionEffects(LivingEntity target, ModDamagePart hitPart, float effectiveDamage, DamageSource source) {
        if (target instanceof Player player) {
            List<EffectEntry> effects = ModClothConfig.getEffects(hitPart);
            for (EffectEntry entry : effects) {
                if (entry.isValid() && effectiveDamage > entry.getThreshold() && RANDOM.nextDouble() <= entry.getChance()) {
                    player.addEffect(new MobEffectInstance(entry.getEffect(), entry.getDuration(), entry.getAmplifier()));
                }
            }
        } else {
            EntityEffectConfig entityConfig = ModClothConfig.getEntityEffectConfig(target.getType());
            if (entityConfig.enabled) {
                EntityBodyPartEffectConfig partConfig = entityConfig.getPartConfig(hitPart);
                if (partConfig.enabled) {
                    float thresholdDamage = target.getMaxHealth() * partConfig.thresholdPercent;
                    for (EffectEntry entry : partConfig.effects) {
                        if (entry.isValid() && effectiveDamage > thresholdDamage && RANDOM.nextDouble() <= entry.getChance()) {
                            target.addEffect(new MobEffectInstance(entry.getEffect(), entry.getDuration(), entry.getAmplifier()));
                        }
                    }
                }
            }
        }
    }

    // ======================== 外部工具 ========================

    public static void clearDeathMark(UUID uuid) {
        SOFT_DEATH_MARK.remove(uuid);
    }
}