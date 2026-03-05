package com.lirxowo.carryonextend.network;

import com.lirxowo.carryonextend.CarryOnExtend;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ThrowPowerPacket(float power, boolean isEntity) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ThrowPowerPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CarryOnExtend.MOD_ID, "throw_power"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ThrowPowerPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, ThrowPowerPacket::power,
                    ByteBufCodecs.BOOL, ThrowPowerPacket::isEntity,
                    ThrowPowerPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
