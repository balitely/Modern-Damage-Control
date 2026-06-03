package com.moderndamage.control.capability.armstamina;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ArmStaminaCapability {
    public static final Capability<IArmStamina> ARM_STAMINA = CapabilityManager.get(new CapabilityToken<>() {});
}