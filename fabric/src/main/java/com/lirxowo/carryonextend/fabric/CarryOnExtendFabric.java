package com.lirxowo.carryonextend.fabric;

import com.lirxowo.carryonextend.CarryOnExtend;
import com.lirxowo.carryonextend.events.PlayerDeathHandler;
import com.lirxowo.carryonextend.fabric.network.FabricNetworkHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;

public final class CarryOnExtendFabric implements ModInitializer {
    @Override
    public void onInitialize() {

        CarryOnExtend.init();

        FabricNetworkHandler.registerServerReceivers();

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) {
                PlayerDeathHandler.onPlayerDeath(player, damageSource);
            }
        });
    }
}
