package com.lirxowo.carryonextend.events;

import com.lirxowo.carryonextend.trigger.TriggerRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;

public class PlayerDeathHandler {

    public static void onPlayerDeath(ServerPlayer player, DamageSource damageSource) {
        PrimedTnt tnt = getTntFromDamageSource(damageSource);
        if (tnt != null && tnt.getTags().contains("thrownBy:" + player.getUUID())) {
            TriggerRegistry.SELF_DESTRUCTION.get().trigger(player);
        }
    }

    private static PrimedTnt getTntFromDamageSource(DamageSource damageSource) {
        Entity directEntity = damageSource.getDirectEntity();
        if (directEntity instanceof PrimedTnt tnt) {
            return tnt;
        }
        Entity entity = damageSource.getEntity();
        if (entity instanceof PrimedTnt tnt) {
            return tnt;
        }
        return null;
    }
}
