package com.moderndamage.control.attribute;

import com.moderndamage.control.ModernDamage;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, ModernDamage.MODID);

    public static final RegistryObject<Attribute> HEAD_NATURAL_ARMOR =
            register("head_natural_armor", 0.0, 0.0, 1000.0);
    public static final RegistryObject<Attribute> CHEST_NATURAL_ARMOR =
            register("chest_natural_armor", 0.0, 0.0, 1000.0);
    public static final RegistryObject<Attribute> STOMACH_NATURAL_ARMOR =
            register("stomach_natural_armor", 0.0, 0.0, 1000.0);
    public static final RegistryObject<Attribute> ARM_NATURAL_ARMOR =
            register("arm_natural_armor", 0.0, 0.0, 1000.0);
    public static final RegistryObject<Attribute> LEG_NATURAL_ARMOR =
            register("leg_natural_armor", 0.0, 0.0, 1000.0);
    public static final RegistryObject<Attribute> PENETRATION =
            register("penetration", 0.0, 0.0, 1000.0);

    private static RegistryObject<Attribute> register(String name, double defaultValue, double min, double max) {
        return ATTRIBUTES.register(name, () -> new RangedAttribute(name, defaultValue, min, max).setSyncable(true));
    }
}