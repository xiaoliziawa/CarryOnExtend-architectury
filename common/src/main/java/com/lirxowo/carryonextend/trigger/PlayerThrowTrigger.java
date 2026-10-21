package com.lirxowo.carryonextend.trigger;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
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

    public record TriggerInstance(Optional<Holder<LootItemCondition>> player) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = LootItemCondition.CODEC
                .optionalFieldOf("player")
                .xmap(TriggerInstance::new, TriggerInstance::player)
                .codec();

        public static Criterion<TriggerInstance> playerThrow() {
            return TriggerRegistry.PLAYER_THROW.get().createCriterion(new TriggerInstance(Optional.empty()));
        }
    }
}
