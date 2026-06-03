package com.moderndamage.control.event;

import com.moderndamage.control.ModernDamage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ModernDamage.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MedicalItemTooltipHandler {
    private static final Map<ResourceLocation, String> TOOLTIP_KEYS = new HashMap<>();

    static {
        // 伤口处理
        put("sterile_gauze", "tooltip.moderndamage.sterile_gauze");
        put("cat7_tourniquet", "tooltip.moderndamage.cat7_tourniquet");
        put("israeli_bandage", "tooltip.moderndamage.israeli_bandage");
        put("sam_splint", "tooltip.moderndamage.sam_splint");
        put("cast_tape", "tooltip.moderndamage.cast_tape");
        put("dr_stitch_suture", "tooltip.moderndamage.dr_stitch_suture");
        put("skin_stapler", "tooltip.moderndamage.skin_stapler");

        // 口服药品
        put("pfizer_ibuprofen", "tooltip.moderndamage.pfizer_ibuprofen");
        put("tramadol_er", "tooltip.moderndamage.tramadol_er");
        put("creatine_capsule", "tooltip.moderndamage.creatine_capsule");
        put("caffeine_pill", "tooltip.moderndamage.caffeine_pill");
        put("homemade_caffeine_extract", "tooltip.moderndamage.homemade_caffeine_extract");
        put("modafinil", "tooltip.moderndamage.modafinil");
        put("amoxicillin", "tooltip.moderndamage.amoxicillin");
        put("meclizine", "tooltip.moderndamage.meclizine");
        put("oral_rehydration_salts", "tooltip.moderndamage.oral_rehydration_salts");

        // 输液袋
        put("saline_iv_500ml", "tooltip.moderndamage.saline_iv");
        put("ringers_iv_500ml", "tooltip.moderndamage.ringers_iv");
        put("voluven_hes_500ml", "tooltip.moderndamage.voluven_hes");
        put("albutein_albumin_100ml", "tooltip.moderndamage.albutein_albumin");
        put("octaplas_plasma_200ml", "tooltip.moderndamage.octaplas_plasma");
        put("dextrose_iv_500ml", "tooltip.moderndamage.dextrose_iv");

        // 医疗包
        put("johnson_first_aid_kit", "tooltip.moderndamage.johnson_kit");
        put("adventure_medical_kit", "tooltip.moderndamage.adventure_kit");
        put("my_medic_pro_kit", "tooltip.moderndamage.my_medic_kit");
        put("surviveware_small_kit", "tooltip.moderndamage.surviveware_kit");
        put("nar_rapid_response_kit", "tooltip.moderndamage.nar_rapid_kit");
        put("nar_mfak_kit", "tooltip.moderndamage.nar_mfak_kit");

        // 注射器
        put("lifeline_vg1_stabilizer", "tooltip.moderndamage.lifeline_vg1_stabilizer");
        put("lifeline_vg2_famexin", "tooltip.moderndamage.lifeline_vg2_famexin");
        put("lifeline_vg3_barrier", "tooltip.moderndamage.lifeline_vg3_barrier");
        put("lifeline_vg4_epinephrine", "tooltip.moderndamage.lifeline_vg4_epinephrine");
        put("lifeline_vg5_kelador", "tooltip.moderndamage.lifeline_vg5_kelador");
        put("lifeline_vg6_methylphenidate", "tooltip.moderndamage.lifeline_vg6_methylphenidate");
        put("lifeline_vg8_modafinil_x", "tooltip.moderndamage.lifeline_vg8_modafinil_x");
        put("lifeline_ag1_thrombin", "tooltip.moderndamage.lifeline_ag1_thrombin");
        put("lifeline_ag2_tranexamic_acid", "tooltip.moderndamage.lifeline_ag2_tranexamic_acid");
        put("lifeline_ag3_morphine", "tooltip.moderndamage.lifeline_ag3_morphine");
        put("lifeline_ag4_ciprofloxacin", "tooltip.moderndamage.lifeline_ag4_ciprofloxacin");
        put("lifeline_ph1_atropine", "tooltip.moderndamage.lifeline_ph1_atropine");
        put("lifeline_ph3_clarity", "tooltip.moderndamage.lifeline_ph3_clarity");
        put("lifeline_ph5_newtype_hemogen", "tooltip.moderndamage.lifeline_ph5_newtype_hemogen");
        put("lifeline_ph6_vitalis", "tooltip.moderndamage.lifeline_ph6_vitalis");
        put("lifeline_ph11_c1_compound", "tooltip.moderndamage.lifeline_ph11_c1_compound");
        put("lifeline_ph12_c1_extender", "tooltip.moderndamage.lifeline_ph12_c1_extender");
        put("lifeline_ph16_xenodyne", "tooltip.moderndamage.lifeline_ph16_xenodyne");
        put("lifeline_ph18_neurostabil", "tooltip.moderndamage.lifeline_ph18_neurostabil");
    }

    private static void put(String itemPath, String key) {
        TOOLTIP_KEYS.put(new ResourceLocation(ModernDamage.MODID, itemPath), key);
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(item);
        if (registryName != null && TOOLTIP_KEYS.containsKey(registryName)) {
            String key = TOOLTIP_KEYS.get(registryName);
            event.getToolTip().add(Component.translatable(key).withStyle(style -> style.withColor(0x7F7F7F)));
        }
    }
}