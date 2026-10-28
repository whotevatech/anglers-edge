package dev.whoteva.anglersedge.inventory;

import dev.whoteva.anglersedge.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The upgrade station's bait recipes: (ingredient A, ingredient B x count) -> bait, using the
 * same rod/material input slots {@link UpgradePath} uses for rod upgrades. The two lookups never
 * collide - {@link UpgradeStationMenu#broadcastChanges()} tries {@link UpgradePath} first (it
 * requires a rod stack of exactly 1 in slot A, which no bait recipe below matches) and falls back
 * to this one. Mirrors the same plain ingredients the vanilla crafting-bench bait recipes use, so
 * either crafting method produces the same result.
 */
public final class BaitPath {

    private record Recipe(Item ingredientA, Item ingredientB, int countB, Item result, int resultCount) {
    }

    private static final Recipe[] RECIPES = {
            new Recipe(Items.STRING, Items.WHEAT_SEEDS, 1, ModItems.BASIC_BAIT.get(), 4),
            new Recipe(Items.STRING, Items.GOLD_NUGGET, 1, ModItems.SHINY_LURE.get(), 4),
            new Recipe(Items.STRING, Items.REDSTONE, 1, ModItems.FRENZY_BAIT.get(), 4),
            new Recipe(Items.STRING, Items.GLOWSTONE_DUST, 2, ModItems.GHOST_BAIT.get(), 2),
    };

    private BaitPath() {
    }

    /** The bait stack this pair of inputs would produce, or {@link ItemStack#EMPTY}. */
    public static ItemStack preview(ItemStack ingredientA, ItemStack ingredientB) {
        Recipe recipe = find(ingredientA, ingredientB);
        return recipe == null ? ItemStack.EMPTY : new ItemStack(recipe.result(), recipe.resultCount());
    }

    /** How much of ingredient B slot a successful bait craft consumes (0 if no match). */
    public static int ingredientBCost(ItemStack ingredientA, ItemStack ingredientB) {
        Recipe recipe = find(ingredientA, ingredientB);
        return recipe == null ? 0 : recipe.countB();
    }

    /** Status line when the left slot is string, or null when this is not a bait craft. */
    public static Component hint(ItemStack ingredientA, ItemStack ingredientB) {
        if (ingredientA.isEmpty() || !ingredientA.is(Items.STRING)) {
            return null;
        }
        if (!preview(ingredientA, ingredientB).isEmpty()) {
            return Component.translatable("gui.anglersedge.upgrade_ready", preview(ingredientA, ingredientB).getHoverName());
        }
        if (ingredientB.isEmpty()) {
            return Component.translatable("gui.anglersedge.bait_need");
        }
        for (Recipe recipe : RECIPES) {
            if (ingredientB.is(recipe.ingredientB()) && ingredientB.getCount() < recipe.countB()) {
                return Component.translatable("gui.anglersedge.upgrade_short",
                        recipe.countB(),
                        Component.translatable(recipe.ingredientB().getDescriptionId()),
                        ingredientB.getCount());
            }
        }
        return Component.translatable("gui.anglersedge.bait_wrong");
    }

    private static Recipe find(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) {
            return null;
        }
        for (Recipe recipe : RECIPES) {
            if (a.is(recipe.ingredientA()) && b.is(recipe.ingredientB()) && b.getCount() >= recipe.countB()) {
                return recipe;
            }
        }
        return null;
    }
}
