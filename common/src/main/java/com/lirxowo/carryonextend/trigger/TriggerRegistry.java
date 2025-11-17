package com.lirxowo.carryonextend.trigger;

import com.lirxowo.carryonextend.CarryOnExtend;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;

public class TriggerRegistry {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES =
        DeferredRegister.create(CarryOnExtend.MOD_ID, Registries.TRIGGER_TYPE);

    public static final RegistrySupplier<TntThrowTrigger> TNT_THROW =
        TRIGGER_TYPES.register("tnt_throw", TntThrowTrigger::new);

    public static final RegistrySupplier<SelfDestructionTrigger> SELF_DESTRUCTION =
        TRIGGER_TYPES.register("self_destruction", SelfDestructionTrigger::new);

    public static void init() {
        TRIGGER_TYPES.register();
    }
}
