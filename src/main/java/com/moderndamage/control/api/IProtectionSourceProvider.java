package com.moderndamage.control.api;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import java.util.List;

public interface IProtectionSourceProvider {
    List<ProtectionSource> getAdditionalSources(ItemStack stack, LivingEntity target);
}