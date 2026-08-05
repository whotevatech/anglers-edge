package dev.whoteva.anglersedge.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The upgrade station's output slot: never accepts placed items, and taking the result consumes
 * the rod and the material cost from the two input slots - the same "ResultSlot" pattern vanilla
 * crafting uses.
 */
public class UpgradeResultSlot extends Slot {

    private final Container inputs;

    public UpgradeResultSlot(Container inputs, int slotIndex, int x, int y) {
        super(inputs, slotIndex, x, y);
        this.inputs = inputs;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        ItemStack slotA = inputs.getItem(0);
        ItemStack slotB = inputs.getItem(1);

        int rodCost = UpgradePath.materialCost(slotA, slotB);
        if (rodCost > 0) {
            slotA.shrink(1);
            slotB.shrink(rodCost);
            inputs.setChanged();
        } else {
            int baitCost = BaitPath.ingredientBCost(slotA, slotB);
            if (baitCost > 0) {
                slotA.shrink(1);
                slotB.shrink(baitCost);
                inputs.setChanged();
            }
        }

        super.onTake(player, stack);
    }
}
