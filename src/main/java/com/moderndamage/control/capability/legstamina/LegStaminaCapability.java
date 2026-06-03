package com.moderndamage.control.capability.legstamina;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class LegStaminaCapability {
    public static final Capability<ILegStamina> LEG_STAMINA = CapabilityManager.get(new CapabilityToken<>() {});
}