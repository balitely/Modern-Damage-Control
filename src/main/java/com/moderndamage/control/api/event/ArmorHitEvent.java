package com.moderndamage.control.api.event;

import com.moderndamage.control.api.ModDamagePart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class ArmorHitEvent extends LivingEvent {
    private final ModDamagePart hitPart;
    private final float originalDamage;
    private float finalDamage;
    private final int armorLevel;

    public ArmorHitEvent(LivingEntity entity, ModDamagePart hitPart, float originalDamage, float finalDamage, int armorLevel) {
        super(entity);
        this.hitPart = hitPart;
        this.originalDamage = originalDamage;
        this.finalDamage = finalDamage;
        this.armorLevel = armorLevel;
    }

    public ModDamagePart getHitPart() {
        return hitPart;
    }

    public float getOriginalDamage() {
        return originalDamage;
    }

    public float getFinalDamage() {
        return finalDamage;
    }

    public void setFinalDamage(float finalDamage) {
        this.finalDamage = finalDamage;
    }

    public int getArmorLevel() {
        return armorLevel;
    }
}