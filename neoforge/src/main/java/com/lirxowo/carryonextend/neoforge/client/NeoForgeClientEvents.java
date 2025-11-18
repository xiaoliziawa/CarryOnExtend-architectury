package com.lirxowo.carryonextend.neoforge.client;

import com.lirxowo.carryonextend.CarryOnExtend;
import com.lirxowo.carryonextend.client.PowerThrowHandler;
import com.lirxowo.carryonextend.events.ClientEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = CarryOnExtend.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {

        boolean handled = ClientEvents.onKeyInput(event.getKey(), event.getAction());

    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {

        double scrollDelta = event.getScrollDeltaY();

        boolean handled = PowerThrowHandler.onMouseScroll(scrollDelta);

        if (handled) {
            event.setCanceled(true);
        }
    }
}
