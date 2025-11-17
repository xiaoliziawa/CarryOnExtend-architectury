package com.lirxowo.carryonextend.fabric.client;

import com.lirxowo.carryonextend.client.ClientSetup;
import com.lirxowo.carryonextend.fabric.network.FabricNetworkHandler;
import net.fabricmc.api.ClientModInitializer;

public final class CarryOnExtendFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        ClientSetup.init();

        FabricNetworkHandler.registerClientReceivers();

        FabricClientEvents.registerEvents();
    }
}
