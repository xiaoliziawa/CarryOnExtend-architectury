package com.lirxowo.carryonextend.neoforge.network;

import com.lirxowo.carryonextend.handler.BlockThrowHandler;
import com.lirxowo.carryonextend.handler.EntityThrowHandler;
import com.lirxowo.carryonextend.network.ThrowBlockPacket;
import com.lirxowo.carryonextend.network.ThrowEntityPacket;
import com.lirxowo.carryonextend.network.ThrowPowerPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class NeoForgeNetworkHandler {

    public static void registerServerReceivers() {

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                ThrowEntityPacket.TYPE,
                ThrowEntityPacket.CODEC,
                (packet, context) -> {
                    Player player = context.getPlayer();
                    if (player instanceof ServerPlayer serverPlayer) {
                        context.queue(() -> {
                            EntityThrowHandler.throwCarriedEntity(serverPlayer);
                        });
                    }
                }
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                ThrowBlockPacket.TYPE,
                ThrowBlockPacket.CODEC,
                (packet, context) -> {
                    Player player = context.getPlayer();
                    if (player instanceof ServerPlayer serverPlayer) {
                        context.queue(() -> {
                            BlockThrowHandler.throwCarriedBlock(serverPlayer);
                        });
                    }
                }
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                ThrowPowerPacket.TYPE,
                ThrowPowerPacket.CODEC,
                (packet, context) -> context.queue(() ->{
                    Player player = context.getPlayer();
                    if (player instanceof ServerPlayer serverPlayer) {
                        float power = packet.power();
                        if (packet.isEntity()) {
                            EntityThrowHandler.throwCarriedEntityWithPower(serverPlayer, power);
                        } else {
                            BlockThrowHandler.throwCarriedBlockWithPower(serverPlayer, power);
                        }

                    }
                })
        );
    }

    public static void registerClientReceivers() {

//        NetworkManager.registerReceiver(
//                NetworkManager.Side.S2C,
//                PlayerThrowPacket.TYPE,
//                PlayerThrowPacket.CODEC,
//                (packet, context) -> context.queue(() -> {
//                    Player player = context.getPlayer();
//                    if (player != null) {
//                        handlePlayerThrowPacket(player);
//                    }
//                })
//        );
    }

//    private static void handlePlayerThrowPacket(Player player) {
//        player.setOnGround(false);
//        player.fallDistance = 0.0f;
//    }
}
