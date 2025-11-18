package com.lirxowo.carryonextend.fabric.network;

import com.lirxowo.carryonextend.handler.BlockThrowHandler;
import com.lirxowo.carryonextend.handler.EntityThrowHandler;
import com.lirxowo.carryonextend.network.PlayerThrowPacket;
import com.lirxowo.carryonextend.network.ThrowBlockPacket;
import com.lirxowo.carryonextend.network.ThrowEntityPacket;
import com.lirxowo.carryonextend.network.ThrowPowerPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class FabricNetworkHandler {

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
                (packet, context) -> {
                    Player player = context.getPlayer();
                    if (player instanceof ServerPlayer serverPlayer) {
                        context.queue(() -> {
                            if (packet.isEntity()) {
                                EntityThrowHandler.throwCarriedEntityWithPower(serverPlayer, packet.power());
                            } else {
                                BlockThrowHandler.throwCarriedBlockWithPower(serverPlayer, packet.power());
                            }
                        });
                    }
                }
        );
    }

    public static void registerClientReceivers() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                PlayerThrowPacket.TYPE,
                PlayerThrowPacket.CODEC,
                (packet, context) -> {
                    Player player = context.getPlayer();
                    if (player != null) {
                        context.queue(() -> {
                            handlePlayerThrowPacket(packet, player);
                        });
                    }
                }
        );
    }

    private static void handlePlayerThrowPacket(PlayerThrowPacket packet, Player player) {
        if (player == null) {
            return;
        }

        // 设置速度
        player.setDeltaMovement(packet.x(), packet.y(), packet.z());
        player.hurtMarked = true;
        player.setOnGround(false);
        player.fallDistance = 0.0f;
    }
}
