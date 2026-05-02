package com.moderndamage.control.capability.parthealth;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreaturePartHealthProvider implements ICapabilityProvider {
    private final CreaturePartHealth instance;

    public CreaturePartHealthProvider(LivingEntity entity) {
        this.instance = new CreaturePartHealth(entity);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CreaturePartHealthCapability.CREATURE_PART_HEALTH_CAP) {
            return LazyOptional.of(() -> instance).cast();
        }
        return LazyOptional.empty();
    }
}