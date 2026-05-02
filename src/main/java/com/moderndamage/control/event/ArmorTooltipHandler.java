package com.moderndamage.control.event;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.armor.ArmorData;
import com.moderndamage.control.armor.ArmorDataLoader;
import com.moderndamage.control.api.ModDamagePart;
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

        event.getToolTip().add(Component.translatable("tooltip.moderndamage.armor_protection").withStyle(ChatFormatting.GRAY));

        for (Map.Entry<ModDamagePart, Integer> entry : data.getCoverage().entrySet()) {
            ModDamagePart part = entry.getKey();
            int level = entry.getValue();
            String roman = RomanNumberHelper.toRomanGrade(level);
            String partKey = "tooltip.moderndamage.part." + part.name().toLowerCase();
            Component partName = Component.translatable(partKey);
            Component line = Component.translatable("tooltip.moderndamage.protection_line_roman", partName, roman, level)
                    .withStyle(ChatFormatting.DARK_GREEN);
            event.getToolTip().add(line);
        }
    }
}