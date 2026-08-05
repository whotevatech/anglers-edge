package dev.whoteva.anglersedge.inventory;

import dev.whoteva.anglersedge.block.ModBlocks;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class UpgradeStationMenu extends AbstractContainerMenu {

    private static final int ROD_SLOT = 0;
    private static final int MATERIAL_SLOT = 1;
    private static final int RESULT_SLOT = 2;
    private static final int PLAYER_INV_START = 3;

    private final Container container;
    private final ContainerLevelAccess access;

    /** Client-side: opened over the network with no extra data, so it gets its own scratch container. */
    public UpgradeStationMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(3), ContainerLevelAccess.NULL);
    }

    /** Server-side: backed by the block entity's real container. */
    public UpgradeStationMenu(int containerId, Inventory playerInventory, Container container, ContainerLevelAccess access) {
        super(ModMenus.UPGRADE_STATION.get(), containerId);
        checkContainerSize(container, 3);
        this.container = container;
        this.access = access;
        container.startOpen(playerInventory.player);

        addSlot(new Slot(container, ROD_SLOT, 44, 35));
        addSlot(new Slot(container, MATERIAL_SLOT, 62, 35));
        addSlot(new UpgradeResultSlot(container, RESULT_SLOT, 116, 35));

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
    public void broadcastChanges() {
        ItemStack slotA = container.getItem(ROD_SLOT);
        ItemStack slotB = container.getItem(MATERIAL_SLOT);
        ItemStack expected = UpgradePath.preview(slotA, slotB);
        if (expected.isEmpty()) {
            // Same two slots double as bait-crafting inputs - see BaitPath's javadoc for why
            // the two recipe sets never collide.
            expected = BaitPath.preview(slotA, slotB);
        }
        if (!ItemStack.matches(expected, container.getItem(RESULT_SLOT))) {
            container.setItem(RESULT_SLOT, expected);
        }
        super.broadcastChanges();
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
            if (!moveItemStackTo(original, ROD_SLOT, RESULT_SLOT, false)) {
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
        return stillValid(access, player, ModBlocks.UPGRADE_STATION.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
