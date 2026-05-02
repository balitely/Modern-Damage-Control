package com.moderndamage.control.capability.parthealth;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CreaturePartHealthCapability {
    public static final Capability<IPartHealth> CREATURE_PART_HEALTH_CAP =
            CapabilityManager.get(new CapabilityToken<>() {});
}