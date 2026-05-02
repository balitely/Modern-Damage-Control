package com.moderndamage.control.network;

import com.moderndamage.control.ModernDamage;
import com.moderndamage.control.api.ModDamagePart;
import com.moderndamage.control.client.ClientPartHealthCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class SyncPartHealthPacket {
    private final UUID playerId;
    private final Map<ModDamagePart, Float> health;
    private final Map<ModDamagePart, Float> maxHealth;

    public SyncPartHealthPacket(UUID playerId, Map<ModDamagePart, Float> health, Map<ModDamagePart, Float> maxHealth) {
        this.playerId = playerId;
        this.health = health;
        this.maxHealth = maxHealth;
    }

    public static void encode(SyncPartHealthPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerId);
        buf.writeInt(msg.health.size());
        for (Map.Entry<ModDamagePart, Float> entry : msg.health.entrySet()) {
            buf.writeEnum(entry.getKey());
            buf.writeFloat(entry.getValue());
        }
        for (Map.Entry<ModDamagePart, Float> entry : msg.maxHealth.entrySet()) {
            buf.writeEnum(entry.getKey());
            buf.writeFloat(entry.getValue());
        }
    }

    public static SyncPartHealthPacket decode(FriendlyByteBuf buf) {
        UUID playerId = buf.readUUID();
        int size = buf.readInt();
        Map<ModDamagePart, Float> health = new EnumMap<>(ModDamagePart.class);
        for (int i = 0; i < size; i++) {
            ModDamagePart part = buf.readEnum(ModDamagePart.class);
            float val = buf.readFloat();
            health.put(part, val);
        }
        Map<ModDamagePart, Float> maxHealth = new EnumMap<>(ModDamagePart.class);
        for (int i = 0; i < size; i++) {
            ModDamagePart part = buf.readEnum(ModDamagePart.class);
            float val = buf.readFloat();
            maxHealth.put(part, val);
        }
        return new SyncPartHealthPacket(playerId, health, maxHealth);
    }

    public static void handle(SyncPartHealthPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientPartHealthCache.update(msg.playerId, msg.health, msg.maxHealth);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}