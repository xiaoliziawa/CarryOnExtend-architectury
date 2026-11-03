package com.lirxowo.carryonextend.events;

import com.lirxowo.carryonextend.client.PowerThrowHandler;
import com.lirxowo.carryonextend.network.NetworkHandler;
import com.lirxowo.carryonextend.network.ThrowPowerPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

public class ClientEvents {

    public static boolean onKeyInput(KeyEvent keyEvent, int action) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.isPaused()) return false;

        if (mc.options.keyDrop.matches(keyEvent) && action == InputConstants.PRESS) {
            return throwCarried(player);
        }

        return false;
    }

    public static boolean onMouseInput(MouseButtonInfo buttonInfo, int action) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.isPaused()) return false;

        MouseButtonEvent mouseEvent = new MouseButtonEvent(0, 0, buttonInfo);

        if (mc.options.keyDrop.matchesMouse(mouseEvent) && action == InputConstants.PRESS) {
            return throwCarried(player);
        }

        return false;
    }

    private static boolean throwCarried(Player player) {
        CarryOnData carry = CarryOnDataManager.getCarryData(player);

        if (carry.isCarrying(CarryOnData.CarryType.ENTITY) || carry.isCarrying(CarryOnData.CarryType.PLAYER) ||
                carry.isCarrying(CarryOnData.CarryType.BLOCK)) {

            boolean isEntity = carry.isCarrying(CarryOnData.CarryType.ENTITY) || carry.isCarrying(CarryOnData.CarryType.PLAYER);

            float power = PowerThrowHandler.getPowerFactor();

            NetworkHandler.sendToServer(new ThrowPowerPacket(power, isEntity));

            return true;
        }

        return false;
    }
}
