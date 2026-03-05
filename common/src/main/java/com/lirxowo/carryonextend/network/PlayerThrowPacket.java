package com.lirxowo.carryonextend.network;

import com.lirxowo.carryonextend.CarryOnExtend;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PlayerThrowPacket(double x, double y, double z) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PlayerThrowPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CarryOnExtend.MOD_ID, "player_throw"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerThrowPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, PlayerThrowPacket::x,
                    ByteBufCodecs.DOUBLE, PlayerThrowPacket::y,
                    ByteBufCodecs.DOUBLE, PlayerThrowPacket::z,
                    PlayerThrowPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
