package com.lirxowo.carryonextend.fabric.client;

import com.lirxowo.carryonextend.client.PowerThrowHandler;
import com.lirxowo.carryonextend.network.NetworkHandler;
import com.lirxowo.carryonextend.network.ThrowPowerPacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

public class FabricClientEvents {

    private static boolean wasDropKeyDown = false;

    public static void registerEvents() {

        ClientTickEvents.END_CLIENT_TICK.register(FabricClientEvents::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        Player player = client.player;
        if (player == null || client.isPaused()) {
            wasDropKeyDown = false;
            return;
        }

        boolean isDropKeyDown = client.options.keyDrop.isDown();

        if (isDropKeyDown && !wasDropKeyDown) {
            CarryOnData carry = CarryOnDataManager.getCarryData(player);

            if (carry.isCarrying(CarryOnData.CarryType.ENTITY) ||
                    carry.isCarrying(CarryOnData.CarryType.PLAYER) ||
                    carry.isCarrying(CarryOnData.CarryType.BLOCK)) {

                boolean isEntity = carry.isCarrying(CarryOnData.CarryType.ENTITY) ||
                        carry.isCarrying(CarryOnData.CarryType.PLAYER);

                float power = PowerThrowHandler.getPowerFactor();

                NetworkHandler.sendToServer(new ThrowPowerPacket(power, isEntity));
            }
        }

        wasDropKeyDown = isDropKeyDown;
    }
}
