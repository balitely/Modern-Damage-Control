package com.moderndamage.control.capability.legstamina;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LegStaminaProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    private final LegStamina instance;
    private final LazyOptional<ILegStamina> optional;

    public LegStaminaProvider(LivingEntity entity) {
        this.instance = new LegStamina(entity);
        this.optional = LazyOptional.of(() -> instance);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == LegStaminaCapability.LEG_STAMINA) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.deserializeNBT(nbt);
    }
}