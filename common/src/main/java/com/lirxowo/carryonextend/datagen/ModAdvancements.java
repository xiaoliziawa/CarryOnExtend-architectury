package com.lirxowo.carryonextend.datagen;

import com.lirxowo.carryonextend.CarryOnExtend;
import com.lirxowo.carryonextend.trigger.PlayerThrowTrigger;
import com.lirxowo.carryonextend.trigger.SelfDestructionTrigger;
import com.lirxowo.carryonextend.trigger.TntThrowTrigger;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public final class ModAdvancements {
    public static void generate(Consumer<AdvancementHolder> writer) {
        AdvancementHolder tntThrowerAdvancement = Advancement.Builder.advancement()
                .rootDisplay(Items.TNT,
                        Component.translatable("advancement.carryonextend.tnt_thrower.title"),
                        Component.translatable("advancement.carryonextend.tnt_thrower.description"),
                        Identifier.withDefaultNamespace("gui/advancements/backgrounds/adventure"),
                        AdvancementType.TASK,
                        true, true, false)
                .addCriterion("throw_tnt", TntThrowTrigger.TriggerInstance.tntThrow())
                .build(Identifier.fromNamespaceAndPath(CarryOnExtend.MOD_ID, "tnt_thrower"));
        writer.accept(tntThrowerAdvancement);

        writer.accept(Advancement.Builder.advancement()
                .parent(tntThrowerAdvancement)
                .display(Items.TNT_MINECART,
                        Component.translatable("advancement.carryonextend.self_destruction.title"),
                        Component.translatable("advancement.carryonextend.self_destruction.description"),
                        AdvancementType.CHALLENGE,
                        true, true, true)
                .addCriterion("self_destruction", SelfDestructionTrigger.TriggerInstance.selfDestruction())
                .build(Identifier.fromNamespaceAndPath(CarryOnExtend.MOD_ID, "self_destruction")));

        writer.accept(Advancement.Builder.advancement()
                .parent(tntThrowerAdvancement)
                .display(Items.DIAMOND,
                        Component.translatable("advancement.carryonextend.player_throw.title"),
                        Component.translatable("advancement.carryonextend.player_throw.description"),
                        AdvancementType.CHALLENGE,
                        true, true, true)
                .addCriterion("throw_player", PlayerThrowTrigger.TriggerInstance.playerThrow())
                .build(Identifier.fromNamespaceAndPath(CarryOnExtend.MOD_ID, "player_throw")));
    }
}
