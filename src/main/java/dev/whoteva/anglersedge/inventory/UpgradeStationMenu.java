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

    public static final int IMAGE_WIDTH = 176;
    public static final int IMAGE_HEIGHT = 176;
    public static final int ROD_X = 27;
    public static final int ROD_Y = 27;
    public static final int MAT_X = 63;
    public static final int MAT_Y = 27;
    public static final int RESULT_X = 124;
    public static final int RESULT_Y = 27;
    public static final int INV_Y = 88;
    public static final int HOTBAR_Y = 146;
    public static final int HINT_Y = 50;

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

        addSlot(new Slot(container, ROD_SLOT, ROD_X, ROD_Y));
        addSlot(new Slot(container, MATERIAL_SLOT, MAT_X, MAT_Y));
        addSlot(new UpgradeResultSlot(container, RESULT_SLOT, RESULT_X, RESULT_Y));

        addPlayerSlots(playerInventory);
    }

    private void addPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, HOTBAR_Y));
        }
    }

    @Override
    public void broadcastChanges() {
        ItemStack slotA = container.getItem(ROD_SLOT);
        ItemStack slotB = container.getItem(MATERIAL_SLOT);
        ItemStack expected = UpgradePath.preview(slotA, slotB);
        if (expected.isEmpty()) {
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
        int before = original.getCount();

        if (index == RESULT_SLOT) {
            // Taking a partial stack would consume the inputs and leave a free remainder.
            if (!canFullyFit(original, PLAYER_INV_START, slots.size())) {
                return ItemStack.EMPTY;
            }
            if (!moveItemStackTo(original, PLAYER_INV_START, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            if (original.isEmpty()) {
                slot.onTake(player, moving);
                slot.set(ItemStack.EMPTY);
            }
        } else if (index < PLAYER_INV_START) {
            if (!moveItemStackTo(original, PLAYER_INV_START, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(original, ROD_SLOT, RESULT_SLOT, false)) {
            return ItemStack.EMPTY;
        }

        if (original.getCount() == before) {
            return ItemStack.EMPTY;
        }
        if (original.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return moving;
    }

    private boolean canFullyFit(ItemStack stack, int start, int end) {
        int remaining = stack.getCount();
        for (int i = start; i < end && remaining > 0; i++) {
            ItemStack existing = slots.get(i).getItem();
            if (existing.isEmpty()) {
                remaining -= Math.min(remaining, stack.getMaxStackSize());
            } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                remaining -= Math.min(remaining, existing.getMaxStackSize() - existing.getCount());
            }
        }
        return remaining <= 0;
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
