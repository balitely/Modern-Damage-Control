package com.moderndamage.control.network;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.client.ClientArmStaminaCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncArmStaminaPacket {
    private final UUID playerId;
    private final float stamina;
    private final boolean isAiming;   // 新增瞄准状态

    public SyncArmStaminaPacket(UUID playerId, float stamina, boolean isAiming) {
        this.playerId = playerId;
        this.stamina = stamina;
        this.isAiming = isAiming;
    }

    public static void encode(SyncArmStaminaPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerId);
        buf.writeFloat(msg.stamina);
        buf.writeBoolean(msg.isAiming);
    }

    public static SyncArmStaminaPacket decode(FriendlyByteBuf buf) {
        return new SyncArmStaminaPacket(buf.readUUID(), buf.readFloat(), buf.readBoolean());
    }

    public static void handle(SyncArmStaminaPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientArmStaminaCache.update(msg.playerId, msg.stamina, msg.isAiming);
        });
        ctx.get().setPacketHandled(true);
    }
}