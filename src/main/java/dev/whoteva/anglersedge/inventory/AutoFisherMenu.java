package dev.whoteva.anglersedge.inventory;

import dev.whoteva.anglersedge.block.ModBlocks;
import dev.whoteva.anglersedge.item.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 3x3 view onto the auto-fisher's catch inventory (same slot layout vanilla's dispenser uses),
 * plus two small upgrade slots to the left: a filter (any item - only matching catches are kept)
 * and a reel upgrade (currently just {@link ModItems#REEL_UPGRADE}, doubles the loot roll per
 * cycle). No crafting logic here, it's a read/take inventory plus two special-purpose slots.
 */
public class AutoFisherMenu extends AbstractContainerMenu {

    private static final int CATCH_SIZE = 9;
    private static final int UPGRADE_SIZE = 2;
    private static final int PLAYER_INV_START = CATCH_SIZE + UPGRADE_SIZE;

    private final Container container;
    private final Container upgrades;
    private final ContainerLevelAccess access;

    /** Client-side: opened over the network with no extra data. */
    public AutoFisherMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(CATCH_SIZE), new SimpleContainer(UPGRADE_SIZE),
                ContainerLevelAccess.NULL);
    }

    /** Server-side: backed by the block entity's real containers. */
    public AutoFisherMenu(int containerId, Inventory playerInventory, Container container, Container upgrades,
                           ContainerLevelAccess access) {
        super(ModMenus.AUTO_FISHER.get(), containerId);
        checkContainerSize(container, CATCH_SIZE);
        checkContainerSize(upgrades, UPGRADE_SIZE);
        this.container = container;
        this.upgrades = upgrades;
        this.access = access;
        container.startOpen(playerInventory.player);
        upgrades.startOpen(playerInventory.player);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(container, col + row * 3, 62 + col * 18, 17 + row * 18));
            }
        }

        // Filter slot: drop any item in here and only matching catches are kept from then on.
        addSlot(new Slot(upgrades, 0, 26, 17));
        // Reel upgrade slot: only accepts the Reel Upgrade item.
        addSlot(new Slot(upgrades, 1, 26, 44) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.REEL_UPGRADE.get());
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack original = slot.getItem();
        ItemStack moving = original.copy();

        if (index < PLAYER_INV_START) {
            if (!moveItemStackTo(original, PLAYER_INV_START, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(original, 0, CATCH_SIZE, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (original.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return moving;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.AUTO_FISHER.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
