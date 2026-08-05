package dev.whoteva.anglersedge.util;

import dev.whoteva.anglersedge.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
import java.util.Map;

/**
 * A fixed rarity tier per species - "how hard is this thing to catch at all" - separate from
 * {@link FishWeight}'s per-catch random size roll ("how big was THIS one"). The two stack: a
 * Common Cod can still turn out Massive, and a Boss-tier Leviathan Scale is always exactly as
 * rare as the fight that dropped it.
 */
public final class FishRarity {

    public enum Tier {
        COMMON("Common", ChatFormatting.GRAY),
        UNCOMMON("Uncommon", ChatFormatting.GREEN),
        RARE("Rare", ChatFormatting.BLUE),
        EPIC("Epic", ChatFormatting.DARK_PURPLE),
        BOSS("Boss", ChatFormatting.GOLD);

        private final String label;
        private final ChatFormatting color;

        Tier(String label, ChatFormatting color) {
            this.label = label;
            this.color = color;
        }

        public Component tag() {
            return Component.literal("[" + label + "]").withStyle(color);
        }
    }

    // Lazily built - see FishTrophyBlock#displayable() for why a static field can't eagerly call
    // ModItems.X.get() (blocks register before items, so those DeferredHolders aren't bound yet
    // at class-load time; this class is only ever touched at runtime, well after registration,
    // but the lazy pattern is cheap and keeps the reasoning consistent everywhere in the mod).
    private static Map<Item, Tier> tiers;

    private static Map<Item, Tier> tiers() {
        if (tiers == null) {
            tiers = Map.ofEntries(
                    Map.entry(Items.COD, Tier.COMMON),
                    Map.entry(Items.SALMON, Tier.COMMON),
                    Map.entry(Items.PUFFERFISH, Tier.COMMON),
                    Map.entry(Items.TROPICAL_FISH, Tier.COMMON),
                    Map.entry(ModItems.REEF_CRAB.get(), Tier.COMMON),
                    Map.entry(ModItems.GLIMMERFIN.get(), Tier.UNCOMMON),
                    Map.entry(ModItems.SWAMP_EEL.get(), Tier.UNCOMMON),
                    Map.entry(ModItems.ANGLERFISH.get(), Tier.RARE),
                    Map.entry(ModItems.SHARK_TOOTH.get(), Tier.RARE),
                    Map.entry(ModItems.ABYSSAL_RAY.get(), Tier.EPIC),
                    Map.entry(ModItems.LEVIATHAN_SCALE.get(), Tier.BOSS));
        }
        return tiers;
    }

    private FishRarity() {
    }

    /** Null if this item has no rarity tier assigned (most vanilla/unrelated items). */
    public static Tier tierOf(Item item) {
        return tiers().get(item);
    }

    /** The "[Rare]"-style tag to show in a tooltip/label, or null if this item has no tier. */
    public static Component tagFor(ItemStack stack) {
        Tier tier = tierOf(stack.getItem());
        return tier == null ? null : tier.tag();
    }

    /**
     * For items with no {@link FishWeight} entry (boss-drop trophy materials, currently) - sets
     * the rarity tag as the stack's whole lore, since there's no weight line to sit alongside it.
     * No-op if this item has no tier.
     */
    public static void applyTagOnly(ItemStack stack) {
        Component tag = tagFor(stack);
        if (tag != null) {
            stack.set(DataComponents.LORE, new ItemLore(List.of(tag)));
        }
    }
}
