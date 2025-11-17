package com.lirxowo.carryonextend.registry;

import com.lirxowo.carryonextend.CarryOnExtend;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(CarryOnExtend.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<CustomFallingBlockEntity>> CUSTOM_FALLING_BLOCK =
        ENTITIES.register("custom_falling_block", () ->
            EntityType.Builder.<CustomFallingBlockEntity>of(CustomFallingBlockEntity::new, MobCategory.MISC)
                .sized(0.98F, 0.98F)
                .clientTrackingRange(10)
                .updateInterval(20)
                .build(CarryOnExtend.MOD_ID + ":custom_falling_block")
        );

    public static void init() {
        ENTITIES.register();
    }
}
