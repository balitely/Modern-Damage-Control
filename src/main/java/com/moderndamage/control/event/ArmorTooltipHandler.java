package com.moderndamage.control.event;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.armor.ArmorCalculator;
import com.moderndamage.control.armor.ArmorData;
import com.moderndamage.control.armor.ArmorDataLoader;
import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.api.ModDamageSubPart;
import com.moderndamage.control.config.ModClothConfig;
import com.moderndamage.control.util.RomanNumberHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

public class ArmorTooltipHandler {

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ArmorData data = ArmorDataLoader.getArmorData(stack.getItem());
        if (data == null) return;

        ModClothConfig config = ModClothConfig.get();
        boolean preciseMode = config.enablePreciseHitbox && config.damageModel == ModClothConfig.DamageModel.HARDCORE;

        event.getToolTip().add(Component.translatable("tooltip.moderndamage.armor_protection").withStyle(ChatFormatting.GRAY));

        if (preciseMode) {
            for (ModDamageSubPart subPart : ModDamageSubPart.values()) {
                int baseLevel = data.getSubProtectionLevel(subPart);
                if (baseLevel == 0) continue;

                int dynamicLevel = ArmorCalculator.getDynamicProtectionLevel(stack, subPart);
                int dynamicToughness = ArmorCalculator.getDynamicToughness(stack, subPart);
                String roman = RomanNumberHelper.toRomanGrade(dynamicLevel);
                String subPartKey = "tooltip.moderndamage.subpart." + subPart.getSubKey();
                Component subPartName = Component.translatable(subPartKey);
                Component line = Component.translatable("tooltip.moderndamage.protection_line_roman", subPartName, roman, dynamicLevel)
                        .append(Component.literal(" [" + dynamicToughness + "]").withStyle(ChatFormatting.GRAY));
                event.getToolTip().add(line);
            }
        } else {
            // 原有主部位显示
            for (Map.Entry<ModDamagePart, Integer> entry : data.getCoverage().entrySet()) {
                ModDamagePart part = entry.getKey();
                int dynamicLevel = ArmorCalculator.getDynamicProtectionLevel(stack, part);
                int dynamicToughness = ArmorCalculator.getDynamicToughness(stack, part);
                String roman = RomanNumberHelper.toRomanGrade(dynamicLevel);
                String partKey = "tooltip.moderndamage.part." + part.name().toLowerCase();
                Component partName = Component.translatable(partKey);
                Component line = Component.translatable("tooltip.moderndamage.protection_line_roman", partName, roman, dynamicLevel)
                        .append(Component.literal(" [" + dynamicToughness + "]").withStyle(ChatFormatting.GRAY));
                event.getToolTip().add(line);
            }
        }
    }
}