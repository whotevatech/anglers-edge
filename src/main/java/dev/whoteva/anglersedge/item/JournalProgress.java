package dev.whoteva.anglersedge.item;

import dev.whoteva.anglersedge.advancement.AnglersCriteria;
import dev.whoteva.anglersedge.util.FishCatalog;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Milestone bookkeeping stored on the journal stack, next to the catch counts.
 * Each step fires once per journal: a chat line, an advancement, and on completion a tackle reward.
 */
public final class JournalProgress {

    private static final String TAG_MILESTONES = "Milestones";
    private static final String TAG_REWARD = "RewardGranted";

    private JournalProgress() {
    }

    public static void afterCatch(Player player, ItemStack journal) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int discovered = FishingJournalItem.discovered(journal);
        int total = FishCatalog.size();

        award(serverPlayer, journal, AnglersCriteria.FIRST_CATCH, discovered >= 1);
        award(serverPlayer, journal, AnglersCriteria.SPECIES_5, discovered >= 5);
        award(serverPlayer, journal, AnglersCriteria.SPECIES_10, discovered >= 10);
        if (discovered >= total) {
            award(serverPlayer, journal, AnglersCriteria.JOURNAL_COMPLETE, true);
            grantCompletionReward(serverPlayer, journal);
        }
    }

    public static boolean milestoneDone(ItemStack journal, String milestone) {
        return milestones(journal).getBoolean(milestone);
    }

    private static void award(ServerPlayer player, ItemStack journal, String milestone, boolean met) {
        if (!met) {
            return;
        }
        CompoundTag root = root(journal);
        CompoundTag milestones = root.getCompound(TAG_MILESTONES);
        if (milestones.getBoolean(milestone)) {
            return;
        }
        milestones.putBoolean(milestone, true);
        root.put(TAG_MILESTONES, milestones);
        write(journal, root);
        AnglersCriteria.trigger(player, milestone);
        player.sendSystemMessage(Component.translatable("message.anglersedge.milestone." + milestone));
    }

    private static void grantCompletionReward(ServerPlayer player, ItemStack journal) {
        CompoundTag root = root(journal);
        if (root.getBoolean(TAG_REWARD)) {
            return;
        }
        root.putBoolean(TAG_REWARD, true);
        write(journal, root);
        player.getInventory().placeItemBackInInventory(new ItemStack(ModItems.SHINY_LURE.get(), 4));
        player.getInventory().placeItemBackInInventory(new ItemStack(ModItems.REEL_UPGRADE.get()));
        player.sendSystemMessage(Component.translatable("message.anglersedge.journal_reward"));
    }

    private static CompoundTag milestones(ItemStack journal) {
        return root(journal).getCompound(TAG_MILESTONES);
    }

    private static CompoundTag root(ItemStack journal) {
        return journal.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void write(ItemStack journal, CompoundTag root) {
        CustomData.set(DataComponents.CUSTOM_DATA, journal, root);
    }
}
