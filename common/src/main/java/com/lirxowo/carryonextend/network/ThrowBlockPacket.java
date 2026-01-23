package com.lirxowo.carryonextend.network;

import com.lirxowo.carryonextend.CarryOnExtend;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ThrowBlockPacket(boolean dummy) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ThrowBlockPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CarryOnExtend.MOD_ID, "throw_block"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ThrowBlockPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, ThrowBlockPacket::dummy,
                    ThrowBlockPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
