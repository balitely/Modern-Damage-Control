package com.moderndamage.control.command;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.capability.parthealth.CreaturePartHealthCapability;
import com.moderndamage.control.capability.parthealth.IPartHealth;
import com.moderndamage.control.capability.parthealth.PartHealthCapability;
import com.moderndamage.control.config.EntityHitboxConfigLoader;
import com.moderndamage.control.entity.EntityHitboxHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ModerndamageCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("moderndamage")
                .then(Commands.literal("debug")
                        .then(Commands.literal("hitbox")
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .executes(ctx -> {
                                            Entity target = EntityArgument.getEntity(ctx, "target");
                                            if (!(target instanceof LivingEntity living)) {
                                                ctx.getSource().sendFailure(Component.literal("目标不是生物"));
                                                return 0;
                                            }
                                            Vec3 localPos = new Vec3(0, living.getBoundingBox().getYsize() / 2, 0);
                                            var part = EntityHitboxHelper.getHitPart(living, localPos);
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    String.format("实体 %s: 高度 %.2f -> 部位 %s",
                                                            living.getName().getString(),
                                                            living.getBoundingBox().getYsize(),
                                                            part.name()
                                                    )), false);
                                            return 1;
                                        })
                                )
                        )
                )
                .then(Commands.literal("generate")
                        .then(Commands.literal("entity_config")
                                .executes(ctx -> {
                                    EntityHitboxConfigLoader.generateDefaultConfig();
                                    ctx.getSource().sendSuccess(() -> Component.literal("已生成 entity_config.json"), true);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("parthealth")
                        .executes(ctx -> showPartHealth(ctx.getSource(), null))
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(ctx -> {
                                    Entity target = EntityArgument.getEntity(ctx, "target");
                                    if (!(target instanceof LivingEntity)) {
                                        ctx.getSource().sendFailure(Component.literal("目标不是生物"));
                                        return 0;
                                    }
                                    return showPartHealth(ctx.getSource(), (LivingEntity) target);
                                })
                        )
                )
        );
    }

    private static int showPartHealth(CommandSourceStack source, @Nullable LivingEntity target) {
        if (target == null) {
            if (!(source.getEntity() instanceof Player player)) {
                source.sendFailure(Component.literal("你必须是玩家才能查看自己的部位血量"));
                return 0;
            }
            target = player;
        }

        if (target instanceof Player player) {
            final LivingEntity finalTarget = target;
            player.getCapability(PartHealthCapability.PART_HEALTH_CAP).ifPresent(cap -> {
                sendPartHealthInfo(source, finalTarget, cap);
            });
        } else {
            final LivingEntity finalTarget = target;
            target.getCapability(CreaturePartHealthCapability.CREATURE_PART_HEALTH_CAP).ifPresent(cap -> {
                sendPartHealthInfo(source, finalTarget, cap);
            });
        }
        return 1;
    }

    private static void sendPartHealthInfo(CommandSourceStack source, LivingEntity target, IPartHealth cap) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("§6=== 部位血量: " + target.getName().getString() + " ==="));
        for (ModDamagePart part : ModDamagePart.values()) {
            float current = cap.getHealth(part);
            float max = cap.getMaxHealth(part);
            String destroyed = cap.isPartDestroyed(part) ? " §c[摧毁]" : "";
            String bar = String.format("%3.0f/%-3.0f §7(%.0f%%)", current, max, current / max * 100);
            lines.add(Component.literal("  §e" + part.name() + ": §f" + bar + destroyed));
        }
        lines.add(Component.literal("§6总计: §f" + String.format("%.1f/%.1f §7(%.0f%%)",
                cap.getTotalHealthPercent() * target.getMaxHealth(),
                target.getMaxHealth(),
                cap.getTotalHealthPercent() * 100)));
        for (Component line : lines) {
            source.sendSuccess(() -> line, false);
        }
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }
}