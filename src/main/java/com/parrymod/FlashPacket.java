package com.parrymod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sent server -> client after a successful parry to flash the screen white. */
public class FlashPacket {
    public static void encode(FlashPacket msg, FriendlyByteBuf buf) {
    }

    public static FlashPacket decode(FriendlyByteBuf buf) {
        return new FlashPacket();
    }

    public static void handle(FlashPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientHandler::startFlash));
        context.setPacketHandled(true);
    }
}
