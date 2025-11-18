package com.lirxowo.carryonextend.neoforge;

import com.lirxowo.carryonextend.CarryOnExtend;
import com.lirxowo.carryonextend.client.ClientSetup;
import com.lirxowo.carryonextend.client.renderer.CustomFallingBlockRenderer;
import com.lirxowo.carryonextend.events.PlayerDeathHandler;
import com.lirxowo.carryonextend.neoforge.network.NeoForgeNetworkHandler;
import com.lirxowo.carryonextend.registry.EntityRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@Mod(CarryOnExtend.MOD_ID)
public final class CarryOnExtendNeoForge {
    public CarryOnExtendNeoForge(IEventBus modEventBus) {

        CarryOnExtend.init();

        NeoForgeNetworkHandler.registerServerReceivers();
//        NeoForgeNetworkHandler.registerClientReceivers();

        NeoForge.EVENT_BUS.addListener(this::onPlayerDeath);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::onClientSetup);
            modEventBus.addListener(this::onRegisterEntityRenderers);
        }
    }

    private void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerDeathHandler.onPlayerDeath(player, event.getSource());
        }
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ClientSetup::init);
    }

    private void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.CUSTOM_FALLING_BLOCK.get(), CustomFallingBlockRenderer::new);
    }
}
