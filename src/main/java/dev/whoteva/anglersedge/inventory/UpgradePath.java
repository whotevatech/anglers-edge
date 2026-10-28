package dev.whoteva.anglersedge.inventory;

import dev.whoteva.anglersedge.item.ModItems;
import net.minecraft.network.chat.Component;
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

    /**
     * A short status line when the left slot is one of the known rods, or null when this pair
     * is not a rod upgrade (so bait recipes can take a turn).
     */
    public static Component hint(ItemStack rod, ItemStack material) {
        if (rod.isEmpty() || !isKnownRod(rod)) {
            return null;
        }
        if (rod.getCount() != 1) {
            return Component.translatable("gui.anglersedge.upgrade_single");
        }
        Step step = findByRod(rod);
        if (step == null) {
            return Component.translatable("gui.anglersedge.upgrade_top");
        }
        if (!preview(rod, material).isEmpty()) {
            return Component.translatable("gui.anglersedge.upgrade_ready", preview(rod, material).getHoverName());
        }
        Component materialName = Component.translatable(step.material().getDescriptionId());
        Component resultName = Component.translatable(step.toRod().getDescriptionId());
        if (material.isEmpty()) {
            return Component.translatable("gui.anglersedge.upgrade_need", step.materialCount(), materialName, resultName);
        }
        if (material.is(step.material()) && material.getCount() < step.materialCount()) {
            return Component.translatable("gui.anglersedge.upgrade_short", step.materialCount(), materialName, material.getCount());
        }
        return Component.translatable("gui.anglersedge.upgrade_wrong", materialName, step.materialCount(), resultName);
    }

    private static boolean isKnownRod(ItemStack rod) {
        return rod.is(ModItems.STURDY_ROD.get()) || rod.is(ModItems.ANGLERS_ROD.get()) || rod.is(ModItems.LEVIATHAN_ROD.get());
    }

    private static Step findByRod(ItemStack rod) {
        for (Step step : STEPS) {
            if (rod.is(step.fromRod())) {
                return step;
            }
        }
        return null;
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
