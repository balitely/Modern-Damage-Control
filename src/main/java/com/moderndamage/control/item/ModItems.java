package com.moderndamage.control.item;

import com.moderndamage.control.ModernDamage;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ModernDamage.MODID);

    // 伤口处理类
    public static final RegistryObject<Item> STERILE_GAUZE = ITEMS.register("sterile_gauze",
            () -> new SterileGauzeItem(new Item.Properties()));
    public static final RegistryObject<Item> CAT7_TOURNIQUET = ITEMS.register("cat7_tourniquet",
            () -> new Cat7TourniquetItem(new Item.Properties()));
    public static final RegistryObject<Item> ISRAELI_BANDAGE = ITEMS.register("israeli_bandage",
            () -> new IsraeliBandageItem(new Item.Properties()));
    public static final RegistryObject<Item> SAM_SPLINT = ITEMS.register("sam_splint",
            () -> new SamSplintItem(new Item.Properties().durability(3)));
    public static final RegistryObject<Item> CAST_TAPE = ITEMS.register("cast_tape",
            () -> new CastTapeItem(new Item.Properties()));
    public static final RegistryObject<Item> DR_STITCH_SUTURE = ITEMS.register("dr_stitch_suture",
            () -> new DrStitchSutureItem(new Item.Properties()));
    public static final RegistryObject<Item> SKIN_STAPLER = ITEMS.register("skin_stapler",
            () -> new SkinStaplerItem(new Item.Properties().durability(10)));

    // 口服药物
    public static final RegistryObject<Item> IBUPROFEN = ITEMS.register("pfizer_ibuprofen",
            () -> new IbuprofenItem(new Item.Properties().durability(12)));
    public static final RegistryObject<Item> TRAMADOL = ITEMS.register("tramadol_er",
            () -> new TramadolItem(new Item.Properties().durability(4)));
    public static final RegistryObject<Item> CREATINE_CAPSULE = ITEMS.register("creatine_capsule",
            () -> new CreatineCapsuleItem(new Item.Properties().durability(8)));
    public static final RegistryObject<Item> CAFFEINE_PILL = ITEMS.register("caffeine_pill",
            () -> new CaffeinePillItem(new Item.Properties().durability(12)));
    public static final RegistryObject<Item> HOMEMADE_CAFFEINE = ITEMS.register("homemade_caffeine_extract",
            () -> new HomemadeCaffeineExtractItem(new Item.Properties().durability(8)));
    public static final RegistryObject<Item> MODAFINIL = ITEMS.register("modafinil",
            () -> new ModafinilItem(new Item.Properties().durability(8)));
    public static final RegistryObject<Item> AMOXICILLIN = ITEMS.register("amoxicillin",
            () -> new AmoxicillinItem(new Item.Properties()));
    public static final RegistryObject<Item> MECLIZINE = ITEMS.register("meclizine",
            () -> new MeclizineItem(new Item.Properties().durability(6)));
    public static final RegistryObject<Item> ORAL_REHYDRATION_SALTS = ITEMS.register("oral_rehydration_salts",
            () -> new OralRehydrationSaltsItem(new Item.Properties()));

    // 输液
    public static final RegistryObject<Item> SALINE_IV_500ML = ITEMS.register("saline_iv_500ml",
            () -> new SalineIv500mlItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> RINGERS_IV_500ML = ITEMS.register("ringers_iv_500ml",
            () -> new RingersIv500mlItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> VOLUVEN_HES_500ML = ITEMS.register("voluven_hes_500ml",
            () -> new VoluvenHes500mlItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> ALBUTEIN_ALBUMIN_100ML = ITEMS.register("albutein_albumin_100ml",
            () -> new AlbuteinAlbumin100mlItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> OCTAPLAS_PLASMA_200ML = ITEMS.register("octaplas_plasma_200ml",
            () -> new OctaplasPlasma200mlItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> DEXTROSE_IV_500ML = ITEMS.register("dextrose_iv_500ml",
            () -> new DextroseIv500mlItem(new Item.Properties().stacksTo(16)));

    // 医疗包
    public static final RegistryObject<Item> JOHNSON_FIRST_AID_KIT = ITEMS.register("johnson_first_aid_kit",
            () -> new JohnsonFirstAidKitItem(new Item.Properties().durability(10)));
    public static final RegistryObject<Item> ADVENTURE_MEDICAL_KIT = ITEMS.register("adventure_medical_kit",
            () -> new AdventureMedicalKitItem(new Item.Properties().durability(8)));
    public static final RegistryObject<Item> MY_MEDIC_PRO_KIT = ITEMS.register("my_medic_pro_kit",
            () -> new MyMedicProKitItem(new Item.Properties().durability(7)));
    public static final RegistryObject<Item> SURVIVEWARE_SMALL_KIT = ITEMS.register("surviveware_small_kit",
            () -> new SurvivewareSmallKitItem(new Item.Properties().durability(6)));
    public static final RegistryObject<Item> NAR_RAPID_RESPONSE_KIT = ITEMS.register("nar_rapid_response_kit",
            () -> new NarRapidResponseKitItem(new Item.Properties().durability(7)));
    public static final RegistryObject<Item> NAR_MFAK_KIT = ITEMS.register("nar_mfak_kit",
            () -> new NarMfakKitItem(new Item.Properties().durability(10)));

    // 注射器（一次性）
    public static final RegistryObject<Item> EPINEPHRINE_AUTO_INJECTOR = ITEMS.register("epinephrine_auto_injector",
            () -> new EpinephrineAutoInjectorItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> MORPHINE_AUTO_INJECTOR = ITEMS.register("morphine_auto_injector",
            () -> new MorphineAutoInjectorItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> ATROPINE_AUTO_INJECTOR = ITEMS.register("atropine_auto_injector",
            () -> new AtropineAutoInjectorItem(new Item.Properties().stacksTo(16)));


    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}