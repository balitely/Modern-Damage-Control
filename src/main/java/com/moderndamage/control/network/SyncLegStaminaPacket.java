package com.moderndamage.control.network;

import com.moderndamage.control.client.ClientLegStaminaCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncLegStaminaPacket {
    private final UUID playerId;
    private final float stamina;

    public SyncLegStaminaPacket(UUID playerId, float stamina) {
        this.playerId = playerId;
        this.stamina = stamina;
    }

    public static void encode(SyncLegStaminaPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerId);
        buf.writeFloat(msg.stamina);
    }

    public static SyncLegStaminaPacket decode(FriendlyByteBuf buf) {
        return new SyncLegStaminaPacket(buf.readUUID(), buf.readFloat());
    }

    public static void handle(SyncLegStaminaPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientLegStaminaCache.update(msg.playerId, msg.stamina);
        });
        ctx.get().setPacketHandled(true);
    }
}