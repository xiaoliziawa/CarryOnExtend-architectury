package com.lirxowo.carryonextend.trigger;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PlayerThrowTrigger extends SimpleCriterionTrigger<PlayerThrowTrigger.@NotNull TriggerInstance> {

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

        public static Criterion<TriggerInstance> playerThrow() {
            return TriggerRegistry.PLAYER_THROW.get().createCriterion(new TriggerInstance(Optional.empty()));
        }
    }
}