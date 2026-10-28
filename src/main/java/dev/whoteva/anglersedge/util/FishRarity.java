package dev.whoteva.anglersedge.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
import java.util.Locale;

/**
 * A fixed rarity tier per species — how hard it is to come by at all — separate from
 * {@link FishWeight}'s per-catch size roll. Tiers live in {@link FishCatalog}.
 */
public final class FishRarity {

    public enum Tier {
        COMMON(ChatFormatting.GRAY),
        UNCOMMON(ChatFormatting.GREEN),
        RARE(ChatFormatting.BLUE),
        EPIC(ChatFormatting.DARK_PURPLE),
        BOSS(ChatFormatting.GOLD);

        private final ChatFormatting color;

        Tier(ChatFormatting color) {
            this.color = color;
        }

        public ChatFormatting color() {
            return color;
        }

        public Component tag() {
            return Component.translatable("rarity.anglersedge." + name().toLowerCase(Locale.ROOT)).withStyle(color);
        }
    }

    private FishRarity() {
    }

    /** Null if this item has no rarity tier assigned. */
    public static Tier tierOf(Item item) {
        FishCatalog.Species species = FishCatalog.byItem(item);
        return species == null ? null : species.rarity();
    }

    /** The "[Rare]"-style tag, or null if this item has no tier. */
    public static Component tagFor(ItemStack stack) {
        Tier tier = tierOf(stack.getItem());
        return tier == null ? null : tier.tag();
    }

    /**
     * For species with no weight line (shark teeth, leviathan scales): the rarity tag is the
     * whole lore. No-op if this item has no tier.
     */
    public static void applyTagOnly(ItemStack stack) {
        Component tag = tagFor(stack);
        if (tag != null) {
            stack.set(DataComponents.LORE, new ItemLore(List.of(tag)));
        }
    }
}
