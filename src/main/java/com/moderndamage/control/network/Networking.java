package com.moderndamage.control.network;

import com.moderndamage.control.ModernDamage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class Networking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ModernDamage.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void register() {
        INSTANCE.messageBuilder(SyncPartHealthPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncPartHealthPacket::encode)
                .decoder(SyncPartHealthPacket::decode)
                .consumerMainThread(SyncPartHealthPacket::handle)
                .add();
        INSTANCE.messageBuilder(SyncArmStaminaPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncArmStaminaPacket::encode)
                .decoder(SyncArmStaminaPacket::decode)
                .consumerMainThread(SyncArmStaminaPacket::handle)
                .add();
        INSTANCE.messageBuilder(SyncLegStaminaPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncLegStaminaPacket::encode)
                .decoder(SyncLegStaminaPacket::decode)
                .consumerMainThread(SyncLegStaminaPacket::handle)
                .add();
    }

    public static void sendToPlayer(Object packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }
}