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

    private Vec3 getHitLocationFromProjectile(Projectile projectile, LivingEntity target) {
        Vec3 projPos = projectile.position();
        AABB targetBox = target.getBoundingBox();
        Vec3 targetCenter = target.position().add(0, target.getBbHeight() / 2, 0);
        Vec3 direction = targetCenter.subtract(projPos).normalize();
        return targetBox.clip(projPos, projPos.add(direction.scale(5.0))).orElse(targetCenter);
    }

    private ModDamagePart getRandomPartByPosition(LivingEntity attacker, LivingEntity target) {
        if (attacker == null) {
            ModernDamage.LOGGER.warn("getRandomPartByPosition: attacker is null, using target Y for estimation");
            int roll = RANDOM.nextInt(100);
            if (roll < 34) return ModDamagePart.CHEST;
            if (roll < 67) return ModDamagePart.STOMACH;
            return RANDOM.nextBoolean() ? ModDamagePart.LEFT_ARM : ModDamagePart.RIGHT_ARM;
        }
        double dy = attacker.getY() - target.getY();
        double threshold = 1.0;
        ModernDamage.LOGGER.info("getRandomPartByPosition: attackerY={}, targetY={}, dy={}", attacker.getY(), target.getY(), dy);
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

    private boolean isArmorIgnored(DamageSource source) {
        String msgId = source.getMsgId();
        return switch (msgId) {
            case "inFire", "onFire", "lava", "hotFloor", "drown", "freeze",
                 "fall", "stalagmite", "flyIntoWall", "magic", "indirectMagic",
                 "sonic_boom", "wither", "starve", "outOfWorld" -> true;
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;

        // 【调试】打印伤害源信息
        DamageSource source = event.getSource();
        ModernDamage.LOGGER.info("=== Hurt Event: target={}, source={}, originalDamage={}", target.getName().getString(), source.getMsgId(), event.getAmount());

        if (!target.isAlive() || target.isDeadOrDying()) {
            event.setCanceled(true);
            return;
        }

        float originalDamage = event.getAmount();
        Entity directEntity = source.getDirectEntity();
        Entity attacker = source.getEntity();

        ModClothConfig config = ModClothConfig.get();
        boolean isHardcore = config.damageModel == ModClothConfig.DamageModel.HARDCORE;

        // ========== 子弹伤害（TaCZ） ==========
        if (directEntity instanceof EntityKineticBullet bullet) {
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

            float penetration = 0f;
            if (attacker instanceof LivingEntity livingShooter) {
                IGunOperator operator = IGunOperator.fromLivingEntity(livingShooter);
                if (operator != null) {
                    AttachmentCacheProperty cache = operator.getCacheProperty();
                    if (cache != null) {
                        Object val = cache.getCache(GunProperties.ARMOR_IGNORE.name());
                        if (val instanceof Float) penetration = (Float) val;
                    }
                }
                double attrPen = 0.0;
                if (livingShooter.getAttribute(ModAttributes.PENETRATION.get()) != null) {
                    attrPen = livingShooter.getAttributeValue(ModAttributes.PENETRATION.get());
                }
                penetration += (float) attrPen;
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
                String msg = String.format("[ModernDamage] %s hit %s(%s): %.1f -> %.1f (%.0f%% reduction, %s)",
                        attackerPlayer.getName().getString(), target.getName().getString(), partInfo,
                        originalDamage, finalDamage, reductionPercent, status);
                attackerPlayer.sendSystemMessage(Component.literal(msg));
                ModernDamage.LOGGER.info(msg);
            }

            event.setCanceled(true);
            if (isHardcore) {
                applyPartDamage(target, hitPart, finalDamage, source, true, config);
            } else {
                applySoftcoreDamage(target, finalDamage, source);
            }
            applyPotionEffects(target, hitPart, finalDamage, source);
            return;
        }

        // ========== 原版弹射物伤害（箭、三叉戟）精准判定 ==========
        if (directEntity instanceof AbstractArrow || directEntity instanceof ThrownTrident) {
            Projectile projectile = (Projectile) directEntity;
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

            float penetration = 0f; // 箭矢无额外穿透，可后续扩展
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

            event.setCanceled(true);
            if (isHardcore) {
                applyPartDamage(target, hitPart, finalDamage, source, true, config);
            } else {
                applySoftcoreDamage(target, finalDamage, source);
            }
            applyPotionEffects(target, hitPart, finalDamage, source);
            return;
        }

        // ========== 非子弹、非弹射物伤害 ==========
        boolean ignoreArmor = isArmorIgnored(source);
        float finalDamage;
        if (ignoreArmor) {
            finalDamage = originalDamage;
            if (config.debugMode && attacker instanceof Player attackerPlayer) {
                ModernDamage.LOGGER.info("[ModernDamage] {} damaged {} with armor-ignoring source {}: {} -> {}",
                        attackerPlayer.getName().getString(), target.getName().getString(), source.getMsgId(),
                        originalDamage, finalDamage);
            }
        } else {
            ArmorCalculator.PenetrationResult result = ArmorCalculator.applyArmorPenetration(target, ModDamagePart.CHEST, originalDamage, 0f);
            finalDamage = result.finalDamage;
            if (config.debugMode && attacker instanceof Player attackerPlayer) {
                ModernDamage.LOGGER.info("[ModernDamage] {} damaged {} ({}): {} -> {} (%.0f%% reduction, %s)",
                        attackerPlayer.getName().getString(), target.getName().getString(), source.getMsgId(),
                        originalDamage, finalDamage, (1 - finalDamage/originalDamage)*100,
                        result.penetrated ? "PENETRATED" : (result.partial ? "PARTIAL" : "BLUNT"));
            }
        }

        if (isHardcore) {
            event.setCanceled(true);
            boolean died = false;
            String msgId = source.getMsgId();

            if (isFullBodyDamage(source)) {
                died = applyFullBodyDamage(target, finalDamage, source, config);
                if (died) return;
            } else if (msgId.equals("hotFloor") || msgId.equals("fall") || msgId.equals("stalagmite")) {
                died = applyLegsDamage(target, finalDamage, source, config);
                if (died) return;
            } else if (msgId.equals("inWall") || msgId.equals("flyIntoWall")) {
                died = applyPartDamage(target, ModDamagePart.HEAD, finalDamage, source, true, config);
                if (died) return;
            } else if (msgId.equals("cactus")) {
                died = applyPartDamage(target, randomLimb(), finalDamage, source, true, config);
                if (died) return;
            } else if (isExplosionDamage(source)) {
                float chestDamage = finalDamage * 0.5f;
                float legsTotal = finalDamage * 0.3f;
                float leftLegDamage = legsTotal / 2;
                float rightLegDamage = legsTotal / 2;
                float otherTotal = finalDamage * 0.2f;
                died = applyPartDamage(target, ModDamagePart.CHEST, chestDamage, source, true, config);
                if (died) return;
                died = applyPartDamage(target, ModDamagePart.LEFT_LEG, leftLegDamage, source, true, config);
                if (died) return;
                died = applyPartDamage(target, ModDamagePart.RIGHT_LEG, rightLegDamage, source, true, config);
                if (died) return;
                if (otherTotal > 0) {
                    died = applyPartDamage(target, randomNonVital(), otherTotal, source, true, config);
                    if (died) return;
                }
            } else if (msgId.equals("fireworks") || msgId.equals("sting")) {
                died = applyPartDamage(target, randomNonVital(), finalDamage, source, true, config);
                if (died) return;
            } else if (msgId.equals("lightningBolt")) {
                float half = finalDamage / 2;
                died = applyPartDamage(target, ModDamagePart.HEAD, half, source, true, config);
                if (died) return;
                died = applyPartDamage(target, ModDamagePart.CHEST, half, source, true, config);
                if (died) return;
            } else if (msgId.equals("indirectMagic")) {
                died = applyPartDamage(target, ModDamagePart.CHEST, finalDamage, source, true, config);
                if (died) return;
            } else if (msgId.equals("thorns")) {
                ModDamagePart armPart = ModDamagePart.RIGHT_ARM;
                if (target instanceof Player player && player.getMainHandItem().isEmpty()) {
                    armPart = RANDOM.nextBoolean() ? ModDamagePart.LEFT_ARM : ModDamagePart.RIGHT_ARM;
                }
                died = applyPartDamage(target, armPart, finalDamage, source, true, config);
                if (died) return;
            } else {
                // ========== 关键修改：所有其他伤害，如果有攻击者则随机部位 ==========
                ModDamagePart randomPart = ModDamagePart.CHEST;
                if (attacker instanceof LivingEntity livingAttacker) {
                    randomPart = getRandomPartByPosition(livingAttacker, target);
                    ModernDamage.LOGGER.info("未分类伤害类型: {}，根据位置随机部位: {}", msgId, randomPart);
                } else {
                    ModernDamage.LOGGER.warn("未分类伤害类型且无攻击者: {}，默认使用胸部", msgId);
                }
                died = applyPartDamage(target, randomPart, finalDamage, source, true, config);
                if (died) return;
            }

            applyPotionEffects(target, ModDamagePart.CHEST, finalDamage, source);
        } else {
            // 软核模式
            event.setAmount(finalDamage);
            applyPotionEffects(target, ModDamagePart.CHEST, finalDamage, source);
            return;
        }
    }

    // ========== 硬核模式下的伤害分配方法 ==========
    private boolean applyPartDamage(LivingEntity target, ModDamagePart part, float amount, DamageSource source, boolean isHardcore, ModClothConfig config) {
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
            player.getCapability(PartHealthCapability.PART_HEALTH_CAP).ifPresent(cap -> cap.damagePart(part, amount, source));
        } else if (config.creaturePartHealthEnabled) {
            target.getCapability(CreaturePartHealthCapability.CREATURE_PART_HEALTH_CAP).ifPresent(cap -> cap.damagePart(part, amount, source));
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

    private boolean applyLegsDamage(LivingEntity target, float amount, DamageSource source, ModClothConfig config) {
        float half = amount / 2.0f;
        if (target instanceof Player player) {
            player.getCapability(PartHealthCapability.PART_HEALTH_CAP).ifPresent(cap -> {
                cap.damagePart(ModDamagePart.LEFT_LEG, half, source);
                cap.damagePart(ModDamagePart.RIGHT_LEG, half, source);
            });
        } else if (config.creaturePartHealthEnabled) {
            target.getCapability(CreaturePartHealthCapability.CREATURE_PART_HEALTH_CAP).ifPresent(cap -> {
                cap.damagePart(ModDamagePart.LEFT_LEG, half, source);
                cap.damagePart(ModDamagePart.RIGHT_LEG, half, source);
            });
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

    private Vec3 getHitLocationFromBullet(EntityKineticBullet bullet) {
        if (bullet instanceof EntityKineticBulletExtension ext) {
            return ext.moderndamage$getLastHitLocation();
        }
        return null;
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

    private ModDamagePart randomLimb() {
        ModDamagePart[] limbs = {ModDamagePart.LEFT_ARM, ModDamagePart.RIGHT_ARM, ModDamagePart.LEFT_LEG, ModDamagePart.RIGHT_LEG};
        return limbs[RANDOM.nextInt(limbs.length)];
    }

    private ModDamagePart randomNonVital() {
        ModDamagePart[] parts = {ModDamagePart.STOMACH, ModDamagePart.LEFT_ARM, ModDamagePart.RIGHT_ARM, ModDamagePart.LEFT_LEG, ModDamagePart.RIGHT_LEG};
        return parts[RANDOM.nextInt(parts.length)];
    }
}