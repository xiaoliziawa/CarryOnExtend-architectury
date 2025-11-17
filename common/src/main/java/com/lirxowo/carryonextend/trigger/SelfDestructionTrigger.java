package com.lirxowo.carryonextend.trigger;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class SelfDestructionTrigger extends SimpleCriterionTrigger<SelfDestructionTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, triggerInstance -> true);
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = ContextAwarePredicate.CODEC
                .optionalFieldOf("player")
                .xmap(TriggerInstance::new, TriggerInstance::player)
                .codec();
    }
}
