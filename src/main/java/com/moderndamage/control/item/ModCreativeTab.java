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

                        output.accept(ModItems.LIFELINE_VG1_STABILIZER.get());
                        output.accept(ModItems.LIFELINE_VG2_FAMEXIN.get());
                        output.accept(ModItems.LIFELINE_VG3_BARRIER.get());
                        output.accept(ModItems.LIFELINE_VG4_EPINEPHRINE.get());
                        output.accept(ModItems.LIFELINE_VG5_KELADOR.get());
                        output.accept(ModItems.LIFELINE_VG6_METHYLPHENIDATE.get());
                        output.accept(ModItems.LIFELINE_VG8_MODAFINIL_X.get());

                        output.accept(ModItems.LIFELINE_AG1_THROMBIN.get());
                        output.accept(ModItems.LIFELINE_AG2_TRANEXAMIC_ACID.get());
                        output.accept(ModItems.LIFELINE_AG3_MORPHINE.get());
                        output.accept(ModItems.LIFELINE_AG4_CIPROFLOXACIN.get());

                        output.accept(ModItems.LIFELINE_PH1_ATROPINE.get());
                        output.accept(ModItems.LIFELINE_PH3_CLARITY.get());
                        output.accept(ModItems.LIFELINE_PH5_NEWTYPE_HEMOGEN.get());
                        output.accept(ModItems.LIFELINE_PH6_VITALIS.get());
                        output.accept(ModItems.LIFELINE_PH11_C1_COMPOUND.get());
                        output.accept(ModItems.LIFELINE_PH12_C1_EXTENDER.get());
                        output.accept(ModItems.LIFELINE_PH16_XENODYNE.get());
                        output.accept(ModItems.LIFELINE_PH18_NEUROSTABIL.get());

                    })
                    .build());

    public static void register(IEventBus bus) {
        CREATIVE_TABS.register(bus);
    }
}