package com.lirxowo.carryonextend.fabric.datagen;

import com.lirxowo.carryonextend.CarryOnExtend;
import com.lirxowo.carryonextend.trigger.SelfDestructionTrigger;
import com.lirxowo.carryonextend.trigger.TntThrowTrigger;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancementProvider extends AdvancementProvider {

    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, List.of(new ModAdvancements()));
    }

    public static class ModAdvancements implements AdvancementSubProvider {

        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> writer) {
            AdvancementHolder tntThrowerAdvancement = Advancement.Builder.advancement()
                    .display(Items.TNT,
                            Component.translatable("advancement.carryonextend.tnt_thrower.title"),
                            Component.translatable("advancement.carryonextend.tnt_thrower.description"),
                            ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                            AdvancementType.TASK,
                            true, true, false)
                    .addCriterion("throw_tnt", TntThrowTrigger.TriggerInstance.tntThrow())
                    .save(writer, CarryOnExtend.MOD_ID + ":tnt_thrower");

            AdvancementHolder selfDestructionAdvancement = Advancement.Builder.advancement()
                    .parent(tntThrowerAdvancement)
                    .display(Items.TNT_MINECART,
                            Component.translatable("advancement.carryonextend.self_destruction.title"),
                            Component.translatable("advancement.carryonextend.self_destruction.description"),
                            null,
                            AdvancementType.GOAL,
                            true, true, true)
                    .addCriterion("self_destruction", SelfDestructionTrigger.TriggerInstance.selfDestruction())
                    .save(writer, CarryOnExtend.MOD_ID + ":self_destruction");
        }
    }
}
