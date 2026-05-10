package com.moderndamage.control.event;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.armor.ArmorCalculator;
import com.moderndamage.control.attribute.ModAttributes;
import com.moderndamage.control.capability.parthealth.CreaturePartHealthCapability;
import com.moderndamage.control.capability.parthealth.PartHealthCapability;
import com.moderndamage.control.compat.hitbox.BodypartHitbox;
import com.moderndamage.control.compat.hitbox.CoordinateTransform;
import com.moderndamage.control.compat.hitbox.EnumPlayerPart;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Random;

public class InjuryEventHandler {
    private static final Random RANDOM = new Random();

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

    private void applyLegsDamage(LivingEntity target, float amount, boolean isHardcore, ModClothConfig config) {
        float half = amount / 2.0f;
        if (isHardcore && target instanceof Player player) {
            player.getCapability(PartHealthCapability.PART_HEALTH_CAP).ifPresent(cap -> {
                cap.damagePart(ModDamagePart.LEFT_LEG, half);
                cap.damagePart(ModDamagePart.RIGHT_LEG, half);
            });
        } else if (isHardcore && config.creaturePartHealthEnabled && target instanceof LivingEntity) {
            target.getCapability(CreaturePartHealthCapability.CREATURE_PART_HEALTH_CAP).ifPresent(cap -> {
                cap.damagePart(ModDamagePart.LEFT_LEG, half);
                cap.damagePart(ModDamagePart.RIGHT_LEG, half);
            });
        } else {
            float newHealth = target.getHealth() - amount;
            if (newHealth <= 0) target.setHealth(0);
            else target.setHealth(newHealth);
        }
    }

    private void applyPartDamage(LivingEntity target, ModDamagePart part, float amount, boolean isHardcore, ModClothConfig config) {
        if (!isHardcore) {
            float newHealth = target.getHealth() - amount;
            if (newHealth <= 0) target.setHealth(0);
            else target.setHealth(newHealth);
            return;
        }
        if (target instanceof Player player) {
            player.getCapability(PartHealthCapability.PART_HEALTH_CAP).ifPresent(cap -> cap.damagePart(part, amount));
        } else if (config.creaturePartHealthEnabled) {
            target.getCapability(CreaturePartHealthCapability.CREATURE_PART_HEALTH_CAP).ifPresent(cap -> cap.damagePart(part, amount));
        } else {
            float newHealth = target.getHealth() - amount;
            if (newHealth <= 0) target.setHealth(0);
            else target.setHealth(newHealth);
        }
    }

    private void applyFullBodyDamage(LivingEntity target, float amount, boolean isHardcore, ModClothConfig config) {
        if (!isHardcore) {
            float newHealth = target.getHealth() - amount;
            if (newHealth <= 0) target.setHealth(0);
            else target.setHealth(newHealth);
            return;
        }
        if (target instanceof Player player) {
            player.getCapability(PartHealthCapability.PART_HEALTH_CAP).ifPresent(cap -> cap.damageAll(amount));
        } else if (config.creaturePartHealthEnabled) {
            target.getCapability(CreaturePartHealthCapability.CREATURE_PART_HEALTH_CAP).ifPresent(cap -> cap.damageAll(amount));
        } else {
            float newHealth = target.getHealth() - amount;
            if (newHealth <= 0) target.setHealth(0);
            else target.setHealth(newHealth);
        }
    }

    private boolean isLowToHigh(Entity attacker, Entity target) {
        if (attacker == null || target == null) return false;
        return attacker.getY() <= target.getY() - 0.5;
    }

    private ModDamagePart randomLimb() {
        ModDamagePart[] limbs = {ModDamagePart.LEFT_ARM, ModDamagePart.RIGHT_ARM, ModDamagePart.LEFT_LEG, ModDamagePart.RIGHT_LEG};
        return limbs[RANDOM.nextInt(limbs.length)];
    }

    private ModDamagePart randomNonVital() {
        ModDamagePart[] parts = {ModDamagePart.STOMACH, ModDamagePart.LEFT_ARM, ModDamagePart.RIGHT_ARM, ModDamagePart.LEFT_LEG, ModDamagePart.RIGHT_LEG};
        return parts[RANDOM.nextInt(parts.length)];
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;
        if (!target.isAlive()) return;

        float originalDamage = event.getAmount();
        DamageSource source = event.getSource();
        Entity directEntity = source.getDirectEntity();
        Entity attacker = source.getEntity();

        ModClothConfig config = ModClothConfig.get();
        boolean isHardcore = config.damageModel == ModClothConfig.DamageModel.HARDCORE;

        if (directEntity instanceof EntityKineticBullet bullet) {
            ModDamagePart hitPart = ModDamagePart.CHEST;
            if (target instanceof Player player) {
                Vec3 hitPos = getHitLocationFromBullet(bullet);
                if (hitPos != null) {
                    Vec3 localPos = CoordinateTransform.worldToLocal(hitPos, player);
                    EnumPlayerPart rawPart = BodypartHitbox.getHitPart(localPos);
                    if (rawPart == null) rawPart = BodypartHitbox.getClosestPart(localPos);
                    hitPart = refinePart(rawPart, localPos);
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
            ArmorCalculator.PenetrationResult result = ArmorCalculator.applyArmorPenetration(target, hitPart, originalDamage, penetration);
            float finalDamage = result.finalDamage;

            if (config.debugMode && attacker instanceof Player attackerPlayer) {
                float reductionPercent = originalDamage > 0 ? (1 - finalDamage / originalDamage) * 100 : 0;
                String status = result.penetrated ? "PENETRATED" : (result.partial ? "PARTIAL" : "BLUNT");
                String msg = String.format("[ModernDamage] %s hit %s(%s): %.1f -> %.1f (%.0f%% reduction, %s)",
                        attackerPlayer.getName().getString(), target.getName().getString(), hitPart.name(),
                        originalDamage, finalDamage, reductionPercent, status);
                attackerPlayer.sendSystemMessage(Component.literal(msg));
                ModernDamage.LOGGER.info(msg);
            }

            event.setCanceled(true);
            if (isHardcore) {
                applyPartDamage(target, hitPart, finalDamage, true, config);
            } else {
                float newHealth = target.getHealth() - finalDamage;
                if (newHealth <= 0) target.setHealth(0);
                else target.setHealth(newHealth);
            }
            applyPotionEffects(target, hitPart, finalDamage, source);
            return;
        }

        ModDamagePart assignedPart = null;
        boolean fullBody = false;
        boolean legsAverage = false;
        boolean randomLimbFlag = false;
        boolean randomNonVitalFlag = false;
        boolean explosionDistribution = false;
        boolean lightningHeadChest = false;
        boolean thornsArm = false;
        boolean lowToHighLegOrStomach = false;
        float explosionChestRatio = 0.5f;
        float explosionLegsRatio = 0.3f;
        float explosionOtherRatio = 0.2f;

        String msgId = source.getMsgId();

        if (isFullBodyDamage(source)) {
            fullBody = true;
        } else if (msgId.equals("hotFloor") || msgId.equals("fall") || msgId.equals("stalagmite")) {
            legsAverage = true;
        } else if (msgId.equals("inWall") || msgId.equals("flyIntoWall")) {
            assignedPart = ModDamagePart.HEAD;
        } else if (msgId.equals("cactus")) {
            randomLimbFlag = true;
        } else if (isExplosionDamage(source)) {
            explosionDistribution = true;
        } else if (msgId.equals("fireworks") || msgId.equals("sting")) {
            randomNonVitalFlag = true;
        } else if (msgId.equals("lightningBolt")) {
            lightningHeadChest = true;
        } else if (msgId.equals("indirectMagic")) {
            assignedPart = ModDamagePart.CHEST;
        } else if (msgId.equals("thorns")) {
            thornsArm = true;
        } else if (directEntity instanceof AbstractArrow || directEntity instanceof ThrownTrident) {
            assignedPart = ModDamagePart.CHEST;
        } else if (msgId.equals("mobAttack") || msgId.equals("playerAttack")) {
            if (isLowToHigh(attacker, target)) {
                lowToHighLegOrStomach = true;
            } else {
                assignedPart = ModDamagePart.CHEST;
            }
        } else {
            assignedPart = ModDamagePart.CHEST;
        }

        ModDamagePart primaryPart = (assignedPart != null) ? assignedPart : ModDamagePart.CHEST;
        float penetration = 0f;
        ArmorCalculator.PenetrationResult result = ArmorCalculator.applyArmorPenetration(target, primaryPart, originalDamage, penetration);
        float finalDamage = result.finalDamage;

        if (config.debugMode && attacker instanceof Player attackerPlayer) {
            String distributionDesc = fullBody ? "FullBody" : (legsAverage ? "LegsAvg" : (assignedPart != null ? assignedPart.name() : "Unknown"));
            String msg = String.format("[ModernDamage] %s damaged %s (%s): %.1f -> %.1f (%.0f%% reduction, distribution: %s)",
                    attackerPlayer.getName().getString(), target.getName().getString(), msgId,
                    originalDamage, finalDamage, (1 - finalDamage/originalDamage)*100, distributionDesc);
            attackerPlayer.sendSystemMessage(Component.literal(msg));
            ModernDamage.LOGGER.info(msg);
        }

        event.setCanceled(true);
        applyPotionEffects(target, primaryPart, finalDamage, source);

        if (fullBody) {
            applyFullBodyDamage(target, finalDamage, isHardcore, config);
        } else if (legsAverage) {
            applyLegsDamage(target, finalDamage, isHardcore, config);
        } else if (randomLimbFlag) {
            applyPartDamage(target, randomLimb(), finalDamage, isHardcore, config);
        } else if (randomNonVitalFlag) {
            applyPartDamage(target, randomNonVital(), finalDamage, isHardcore, config);
        } else if (explosionDistribution) {
            float chestDamage = finalDamage * explosionChestRatio;
            float legsTotal = finalDamage * explosionLegsRatio;
            float leftLegDamage = legsTotal / 2;
            float rightLegDamage = legsTotal / 2;
            float otherTotal = finalDamage * explosionOtherRatio;
            applyPartDamage(target, ModDamagePart.CHEST, chestDamage, isHardcore, config);
            applyPartDamage(target, ModDamagePart.LEFT_LEG, leftLegDamage, isHardcore, config);
            applyPartDamage(target, ModDamagePart.RIGHT_LEG, rightLegDamage, isHardcore, config);
            if (otherTotal > 0) {
                applyPartDamage(target, randomNonVital(), otherTotal, isHardcore, config);
            }
        } else if (lightningHeadChest) {
            float half = finalDamage / 2;
            applyPartDamage(target, ModDamagePart.HEAD, half, isHardcore, config);
            applyPartDamage(target, ModDamagePart.CHEST, half, isHardcore, config);
        } else if (thornsArm) {
            ModDamagePart armPart = ModDamagePart.RIGHT_ARM;
            if (target instanceof Player player && player.getMainHandItem().isEmpty()) {
                armPart = RANDOM.nextBoolean() ? ModDamagePart.LEFT_ARM : ModDamagePart.RIGHT_ARM;
            }
            applyPartDamage(target, armPart, finalDamage, isHardcore, config);
        } else if (lowToHighLegOrStomach) {
            if (RANDOM.nextBoolean()) {
                applyLegsDamage(target, finalDamage, isHardcore, config);
            } else {
                applyPartDamage(target, ModDamagePart.STOMACH, finalDamage, isHardcore, config);
            }
        } else {
            if (assignedPart == null) assignedPart = ModDamagePart.CHEST;
            applyPartDamage(target, assignedPart, finalDamage, isHardcore, config);
        }

        if (!isHardcore && target.getHealth() <= 0 && target instanceof ServerPlayer serverPlayer) {
            serverPlayer.setHealth(0);
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket(
                    serverPlayer.getId(),
                    serverPlayer.getCombatTracker().getDeathMessage()
            ));
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
}