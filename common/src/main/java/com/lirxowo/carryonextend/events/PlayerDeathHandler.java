package com.lirxowo.carryonextend.events;

import com.lirxowo.carryonextend.trigger.TriggerRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.PrimedTnt;

public class PlayerDeathHandler {

    public static void onPlayerDeath(ServerPlayer player, DamageSource damageSource) {
        if (damageSource.getEntity() instanceof PrimedTnt tnt) {
            String throwerTag = "thrownBy:" + player.getUUID().toString();
            if (tnt.getTags().contains(throwerTag)) {
                TriggerRegistry.SELF_DESTRUCTION.get().trigger(player);
            }
        }
    }
}
