package com.moderndamage.control.api.event;

import com.moderndamage.control.api.ModDamagePart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class GetArmorLevelEvent extends LivingEvent {
    private final ModDamagePart hitPart;
    private int armorLevel;

    public GetArmorLevelEvent(LivingEntity entity, ModDamagePart hitPart, int originalLevel) {
        super(entity);
        this.hitPart = hitPart;
        this.armorLevel = originalLevel;
    }

    public ModDamagePart getHitPart() {
        return hitPart;
    }

    public int getArmorLevel() {
        return armorLevel;
    }

    public void setArmorLevel(int armorLevel) {
        this.armorLevel = armorLevel;
    }
}