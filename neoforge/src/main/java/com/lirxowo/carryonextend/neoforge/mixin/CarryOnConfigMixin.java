package com.lirxowo.carryonextend.neoforge.mixin;

import net.neoforged.fml.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tschipp.carryon.config.neoforge.ConfigLoaderImpl;

@Mixin(value = ConfigLoaderImpl.class, remap = false)
public class CarryOnConfigMixin {
    @Redirect(method = "*", at = @At(value = "FIELD",
            target = "Lnet/neoforged/fml/config/ModConfig$Type;COMMON:Lnet/neoforged/fml/config/ModConfig$Type;"),
            require = 0)
    private static ModConfig.Type useLocalConfigType() {
        return ModConfig.Type.LOCAL;
    }
}
