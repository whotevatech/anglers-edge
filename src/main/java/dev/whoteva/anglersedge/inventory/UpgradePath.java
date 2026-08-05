package dev.whoteva.anglersedge.inventory;

import dev.whoteva.anglersedge.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The upgrade station's recipe table: (rod tier, material) -> next rod tier.
 *
 * <p>A flat lookup rather than the vanilla recipe system - there are only two upgrades in this
 * first pass, and the station's own material-count check (a specific item at a specific
 * quantity) doesn't map cleanly onto {@code CraftingInput}/shapeless recipes anyway.
 */
public final class UpgradePath {

    private record Step(Item fromRod, Item material, int materialCount, Item toRod) {
    }

    private static final Step[] STEPS = {
            new Step(ModItems.STURDY_ROD.get(), Items.PRISMARINE_CRYSTALS, 4, ModItems.ANGLERS_ROD.get()),
            new Step(ModItems.ANGLERS_ROD.get(), Items.NAUTILUS_SHELL, 2, ModItems.LEVIATHAN_ROD.get()),
    };

    private UpgradePath() {
    }

    /** The rod the station would produce for this input pair, or {@link ItemStack#EMPTY}. */
    public static ItemStack preview(ItemStack rod, ItemStack material) {
        Step step = findStep(rod, material);
        return step == null ? ItemStack.EMPTY : new ItemStack(step.toRod());
    }

    /** How much of the material slot a successful upgrade consumes. */
    public static int materialCost(ItemStack rod, ItemStack material) {
        Step step = findStep(rod, material);
        return step == null ? 0 : step.materialCount();
    }

    private static Step findStep(ItemStack rod, ItemStack material) {
        if (rod.isEmpty() || rod.getCount() != 1 || material.isEmpty()) {
            return null;
        }
        for (Step step : STEPS) {
            if (rod.is(step.fromRod()) && material.is(step.material()) && material.getCount() >= step.materialCount()) {
                return step;
            }
        }
        return null;
    }
}
