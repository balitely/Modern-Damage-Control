package com.moderndamage.control.capability.legstamina;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import java.util.Map;
import java.util.UUID;

public interface ILegStamina {
    float getStamina();
    float getMaxStamina();
    void setStamina(float value);
    void addStamina(float amount);
    boolean consumeStamina(float cost, boolean simulate);
    void tick();
    void reset();
    int getLastDrainTick();
    Map<UUID, AttributeModifier> getActiveLowPenalties();
    Map<UUID, AttributeModifier> getActiveCriticalPenalties();
}