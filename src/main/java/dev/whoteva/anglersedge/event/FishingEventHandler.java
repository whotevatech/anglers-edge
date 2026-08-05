package dev.whoteva.anglersedge.event;

import dev.whoteva.anglersedge.item.BaitItem;
import dev.whoteva.anglersedge.item.FishingJournalItem;
import dev.whoteva.anglersedge.item.ModItems;
import dev.whoteva.anglersedge.item.TieredFishingRodItem;
import dev.whoteva.anglersedge.util.BiomeCategory;
import dev.whoteva.anglersedge.util.FishWeight;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;

/**
 * Applies bait effects on a successful catch.
 *
 * <p>Bait isn't "loaded" onto the rod - it's simply held in the off-hand. Every time
 * {@link ItemFishedEvent} fires (after vanilla has already rolled the fishing loot table but
 * before the items are handed to the player) this checks the off-hand for one of our bait items,
 * mutates {@link ItemFishedEvent#getDrops()} accordingly, and consumes one bait. This works with
 * any fishing rod, not just this mod's own tiers, which felt more useful than gating it.
 */
public final class FishingEventHandler {

    private FishingEventHandler() {
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        Player player = event.getEntity();
        NonNullList<ItemStack> drops = event.getDrops();

        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!drops.isEmpty() && offhand.getItem() instanceof BaitItem bait) {
            bait.effect().apply(drops, player.getRandom());
            offhand.shrink(1);
        }

        applyRodBonus(player, drops);
        applyBiomeFlavor(event, drops);
        applyFishWeights(player, drops);
        logToJournal(player, drops);
    }

    /** Gives every sizeable fish species its own randomly-rolled weight and size title - see {@link FishWeight}. */
    private static void applyFishWeights(Player player, NonNullList<ItemStack> drops) {
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                FishWeight.apply(drop, player.getRandom());
            }
        }
    }

    private static final float BIOME_FISH_CHANCE = 0.15F;

    /**
     * Handheld rods roll vanilla's shared fishing table internally (inside {@code FishingHook},
     * before this event fires) so there's no clean way to swap in a different loot table the way
     * the auto-fisher does. Instead, post-process: a small chance to replace one drop with a
     * species from {@link dev.whoteva.anglersedge.util.BiomeCategory} for wherever the bobber
     * landed, so handheld fishing gets a taste of the same biome variety without needing a mixin.
     */
    private static void applyBiomeFlavor(ItemFishedEvent event, NonNullList<ItemStack> drops) {
        if (drops.isEmpty()) {
            return;
        }
        Player player = event.getEntity();
        if (player.getRandom().nextFloat() >= BIOME_FISH_CHANCE) {
            return;
        }
        String category = BiomeCategory.at(player.level(), event.getHookEntity().blockPosition());
        Item species = switch (category) {
            case "ocean" -> ModItems.GLIMMERFIN.get();
            case "swamp" -> ModItems.SWAMP_EEL.get();
            case "river" -> player.getRandom().nextBoolean() ? ModItems.GLIMMERFIN.get() : ModItems.SWAMP_EEL.get();
            default -> null;
        };
        if (species == null) {
            return;
        }
        int index = player.getRandom().nextInt(drops.size());
        drops.set(index, new ItemStack(species));
    }

    /** Rod-tier special ability (currently only the Leviathan Rod): chance of a bonus duplicate drop. */
    private static void applyRodBonus(Player player, NonNullList<ItemStack> drops) {
        if (drops.isEmpty()) {
            return;
        }
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(mainHand.getItem() instanceof TieredFishingRodItem rod)) {
            return;
        }
        float chance = rod.bonusRollChance();
        if (chance <= 0f || player.getRandom().nextFloat() >= chance) {
            return;
        }
        ItemStack bonus = drops.get(player.getRandom().nextInt(drops.size())).copy();
        bonus.setCount(1);
        drops.add(bonus);
    }

    /** If a Fishing Journal is anywhere in the player's inventory, record every species just caught. */
    private static void logToJournal(Player player, NonNullList<ItemStack> drops) {
        if (drops.isEmpty()) {
            return;
        }
        ItemStack journal = findJournal(player);
        if (journal.isEmpty()) {
            return;
        }
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) {
                continue;
            }
            String id = BuiltInRegistries.ITEM.getKey(drop.getItem()).toString();
            FishingJournalItem.record(journal, id);
        }
    }

    private static ItemStack findJournal(Player player) {
        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (main.getItem() instanceof FishingJournalItem) {
            return main;
        }
        ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
        if (off.getItem() instanceof FishingJournalItem) {
            return off;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof FishingJournalItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
