package dev.whoteva.anglersedge.advancement;

import dev.whoteva.anglersedge.AnglersEdge;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AnglersCriteria {

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, AnglersEdge.MOD_ID);

    public static final DeferredHolder<CriterionTrigger<?>, MilestoneTrigger> MILESTONE =
            TRIGGERS.register("milestone", MilestoneTrigger::new);

    public static final String FIRST_CATCH = "first_catch";
    public static final String SPECIES_5 = "species_5";
    public static final String SPECIES_10 = "species_10";
    public static final String JOURNAL_COMPLETE = "journal_complete";
    public static final String BAIT_USED = "bait_used";
    public static final String TROPHY_MOUNTED = "trophy_mounted";
    public static final String FISH_BRED = "fish_bred";
    public static final String SHARK_SLAIN = "shark_slain";
    public static final String LEVIATHAN_SLAIN = "leviathan_slain";
    public static final String WHALE_SIGHTED = "whale_sighted";

    private AnglersCriteria() {
    }

    public static void trigger(ServerPlayer player, String milestone) {
        MILESTONE.get().trigger(player, milestone);
    }
}
