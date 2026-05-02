package com.moderndamage.control.item;

import com.moderndamage.control.ModernDamage;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModernDamage.MODID);

    public static final RegistryObject<CreativeModeTab> MEDICAL_TAB = CREATIVE_TABS.register("medical_items",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.moderndamage.medical"))
                    .icon(() -> new ItemStack(ModItems.SAM_SPLINT.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.STERILE_GAUZE.get());
                        output.accept(ModItems.CAT7_TOURNIQUET.get());
                        output.accept(ModItems.ISRAELI_BANDAGE.get());
                        output.accept(ModItems.SAM_SPLINT.get());
                        output.accept(ModItems.CAST_TAPE.get());
                        output.accept(ModItems.DR_STITCH_SUTURE.get());
                        output.accept(ModItems.SKIN_STAPLER.get());

                        output.accept(ModItems.IBUPROFEN.get());
                        output.accept(ModItems.TRAMADOL.get());
                        output.accept(ModItems.CAFFEINE_PILL.get());
                        output.accept(ModItems.HOMEMADE_CAFFEINE.get());
                        output.accept(ModItems.MODAFINIL.get());
                        output.accept(ModItems.CREATINE_CAPSULE.get());
                        output.accept(ModItems.AMOXICILLIN.get());
                        output.accept(ModItems.MECLIZINE.get());
                        output.accept(ModItems.ORAL_REHYDRATION_SALTS.get());

                        output.accept(ModItems.SALINE_IV_500ML.get());
                        output.accept(ModItems.RINGERS_IV_500ML.get());
                        output.accept(ModItems.VOLUVEN_HES_500ML.get());
                        output.accept(ModItems.ALBUTEIN_ALBUMIN_100ML.get());
                        output.accept(ModItems.OCTAPLAS_PLASMA_200ML.get());
                        output.accept(ModItems.DEXTROSE_IV_500ML.get());

                        output.accept(ModItems.JOHNSON_FIRST_AID_KIT.get());
                        output.accept(ModItems.ADVENTURE_MEDICAL_KIT.get());
                        output.accept(ModItems.MY_MEDIC_PRO_KIT.get());
                        output.accept(ModItems.SURVIVEWARE_SMALL_KIT.get());
                        output.accept(ModItems.NAR_RAPID_RESPONSE_KIT.get());
                        output.accept(ModItems.NAR_MFAK_KIT.get());

                        output.accept(ModItems.EPINEPHRINE_AUTO_INJECTOR.get());
                        output.accept(ModItems.MORPHINE_AUTO_INJECTOR.get());
                        output.accept(ModItems.ATROPINE_AUTO_INJECTOR.get());

                    })
                    .build());

    public static void register(IEventBus bus) {
        CREATIVE_TABS.register(bus);
    }
}