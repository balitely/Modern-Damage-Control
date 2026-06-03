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
    public static final RegistryObject<Item> LIFELINE_VG1_STABILIZER = ITEMS.register("lifeline_vg1_stabilizer", LifelineVG1StabilizerItem::new);
    public static final RegistryObject<Item> LIFELINE_VG2_FAMEXIN = ITEMS.register("lifeline_vg2_famexin", LifelineVG2FamexinItem::new);
    public static final RegistryObject<Item> LIFELINE_VG3_BARRIER = ITEMS.register("lifeline_vg3_barrier", LifelineVG3BarrierItem::new);
    public static final RegistryObject<Item> LIFELINE_VG4_EPINEPHRINE = ITEMS.register("lifeline_vg4_epinephrine", LifelineVG4EpinephrineItem::new);
    public static final RegistryObject<Item> LIFELINE_VG5_KELADOR = ITEMS.register("lifeline_vg5_kelador", LifelineVG5KeladorItem::new);
    public static final RegistryObject<Item> LIFELINE_VG6_METHYLPHENIDATE = ITEMS.register("lifeline_vg6_methylphenidate", LifelineVG6MethylphenidateItem::new);
    public static final RegistryObject<Item> LIFELINE_VG8_MODAFINIL_X = ITEMS.register("lifeline_vg8_modafinil_x", LifelineVG8ModafinilXItem::new);

    public static final RegistryObject<Item> LIFELINE_AG1_THROMBIN = ITEMS.register("lifeline_ag1_thrombin", LifelineAG1ThrombinItem::new);
    public static final RegistryObject<Item> LIFELINE_AG2_TRANEXAMIC_ACID = ITEMS.register("lifeline_ag2_tranexamic_acid", LifelineAG2TranexamicAcidItem::new);
    public static final RegistryObject<Item> LIFELINE_AG3_MORPHINE = ITEMS.register("lifeline_ag3_morphine", LifelineAG3MorphineItem::new);
    public static final RegistryObject<Item> LIFELINE_AG4_CIPROFLOXACIN = ITEMS.register("lifeline_ag4_ciprofloxacin", LifelineAG4CiprofloxacinItem::new);

    public static final RegistryObject<Item> LIFELINE_PH1_ATROPINE = ITEMS.register("lifeline_ph1_atropine", LifelinePH1AtropineItem::new);
    public static final RegistryObject<Item> LIFELINE_PH3_CLARITY = ITEMS.register("lifeline_ph3_clarity", LifelinePH3ClarityItem::new);
    public static final RegistryObject<Item> LIFELINE_PH5_NEWTYPE_HEMOGEN = ITEMS.register("lifeline_ph5_newtype_hemogen", LifelinePH5NewtypeHemogenItem::new);
    public static final RegistryObject<Item> LIFELINE_PH6_VITALIS = ITEMS.register("lifeline_ph6_vitalis", LifelinePH6VitalisItem::new);
    public static final RegistryObject<Item> LIFELINE_PH11_C1_COMPOUND = ITEMS.register("lifeline_ph11_c1_compound", LifelinePH11C1CompoundItem::new);
    public static final RegistryObject<Item> LIFELINE_PH12_C1_EXTENDER = ITEMS.register("lifeline_ph12_c1_extender", LifelinePH12C1ExtenderItem::new);
    public static final RegistryObject<Item> LIFELINE_PH16_XENODYNE = ITEMS.register("lifeline_ph16_xenodyne", LifelinePH16XenodyneItem::new);
    public static final RegistryObject<Item> LIFELINE_PH18_NEUROSTABIL = ITEMS.register("lifeline_ph18_neurostabil", LifelinePH18NeurostabilItem::new);


    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}