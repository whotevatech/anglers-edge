package dev.whoteva.anglersedge.util;

import dev.whoteva.anglersedge.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
import java.util.Map;

/**
 * Rolls a random weight (and a size title derived from it) onto an individual fish {@link ItemStack}
 * at the moment it's caught - so two Cod in your inventory aren't identical, the way two "Massive
 * Bass" in a real fishing game aren't. Applied via plain {@link net.minecraft.core.component.DataComponentType}s
 * ({@code CUSTOM_NAME} + {@code LORE}) rather than a custom {@code Item} subclass, since several
 * of the species this applies to (Cod, Salmon, Pufferfish, Tropical Fish) are vanilla items this
 * mod doesn't own the class of - components work on any item, vanilla or modded, without needing one.
 */
public final class FishWeight {

    private record Range(float min, float max) {
    }

    // Lazily built - see FishTrophyBlock#displayable() for why a static field that calls
    // ModItems.X.get() can't be eagerly evaluated at class-load time.
    private static Map<Item, Range> ranges;

    private static Map<Item, Range> ranges() {
        if (ranges == null) {
            ranges = Map.ofEntries(
                    Map.entry(Items.COD, new Range(1f, 8f)),
                    Map.entry(Items.SALMON, new Range(2f, 15f)),
                    Map.entry(Items.PUFFERFISH, new Range(0.5f, 3f)),
                    Map.entry(Items.TROPICAL_FISH, new Range(0.2f, 2f)),
                    Map.entry(ModItems.GLIMMERFIN.get(), new Range(1f, 6f)),
                    Map.entry(ModItems.SWAMP_EEL.get(), new Range(2f, 10f)),
                    Map.entry(ModItems.ABYSSAL_RAY.get(), new Range(10f, 60f)),
                    Map.entry(ModItems.REEF_CRAB.get(), new Range(0.5f, 4f)),
                    Map.entry(ModItems.ANGLERFISH.get(), new Range(3f, 20f)));
        }
        return ranges;
    }

    private static final Range DEFAULT_RANGE = new Range(1f, 10f);

    private FishWeight() {
    }

    public static boolean isSizeable(Item item) {
        return ranges().containsKey(item);
    }

    /** Rolls and applies a weight + size title to this stack. No-op for items {@link #isSizeable} doesn't cover. */
    public static void apply(ItemStack stack, RandomSource random) {
        if (!isSizeable(stack.getItem())) {
            return;
        }
        Range range = ranges().getOrDefault(stack.getItem(), DEFAULT_RANGE);
        float t = random.nextFloat();
        float weight = range.min() + t * (range.max() - range.min());
        String title = titleFor(t);
        ChatFormatting color = colorFor(t);

        Component baseName = Component.translatable(stack.getItem().getDescriptionId());
        Component displayName = title.isEmpty()
                ? baseName.copy().withStyle(color)
                : Component.literal(title + " ").append(baseName).withStyle(color);
        stack.set(DataComponents.CUSTOM_NAME, displayName);

        String weightText = weight < 1f
                ? String.format("%.1f oz", weight * 16f)
                : String.format("%.1f lb", weight);
        Component weightLine = Component.literal(weightText).withStyle(ChatFormatting.GRAY);

        Component rarityTag = FishRarity.tagFor(stack);
        List<Component> lore = rarityTag == null ? List.of(weightLine) : List.of(rarityTag, weightLine);
        stack.set(DataComponents.LORE, new ItemLore(lore));
    }

    /** Empty string means "no prefix" - an exactly-average fish doesn't need calling out. */
    private static String titleFor(float t) {
        if (t < 0.15F) return "Tiny";
        if (t < 0.35F) return "Small";
        if (t < 0.65F) return "";
        if (t < 0.85F) return "Large";
        if (t < 0.97F) return "Massive";
        return "Record Breaker";
    }

    private static ChatFormatting colorFor(float t) {
        if (t < 0.15F) return ChatFormatting.GRAY;
        if (t < 0.65F) return ChatFormatting.WHITE;
        if (t < 0.85F) return ChatFormatting.GREEN;
        if (t < 0.97F) return ChatFormatting.GOLD;
        return ChatFormatting.LIGHT_PURPLE;
    }
}
