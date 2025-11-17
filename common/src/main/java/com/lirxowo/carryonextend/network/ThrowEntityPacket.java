package com.lirxowo.carryonextend.network;

import com.lirxowo.carryonextend.CarryOnExtend;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ThrowEntityPacket(boolean dummy) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ThrowEntityPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CarryOnExtend.MOD_ID, "throw_entity"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ThrowEntityPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, ThrowEntityPacket::dummy,
                    ThrowEntityPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
