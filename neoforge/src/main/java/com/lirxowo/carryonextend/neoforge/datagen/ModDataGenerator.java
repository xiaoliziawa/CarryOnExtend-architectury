package com.lirxowo.carryonextend.neoforge.datagen;

import com.lirxowo.carryonextend.CarryOnExtend;
import com.lirxowo.carryonextend.datagen.ModAdvancements;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

@EventBusSubscriber(modid = CarryOnExtend.MOD_ID)
public final class ModDataGenerator {
    @SubscribeEvent
    public static void gatherRegistries(GatherDataRegistryEntriesEvent event) {
        event.add(Registries.ADVANCEMENT, context ->
                ModAdvancements.generate(advancement -> advancement.register(context)));
    }
}
