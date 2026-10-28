package dev.whoteva.anglersedge.inventory;

import dev.whoteva.anglersedge.block.ModBlocks;
import dev.whoteva.anglersedge.item.BaitItem;
import dev.whoteva.anglersedge.item.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

/**
 * 3×3 catch grid plus filter, reel, and bait slots. Shift-click sends reels and bait to their
 * own slots; the filter stays click-to-place so a sample isn't swallowed by a shift-click.
 */
public class AutoFisherMenu extends AbstractContainerMenu {

    public static final int IMAGE_WIDTH = 176;
    public static final int IMAGE_HEIGHT = 204;
    public static final int FILTER_X = 8;
    public static final int FILTER_Y = 18;
    public static final int REEL_X = 8;
    public static final int REEL_Y = 40;
    public static final int BAIT_X = 8;
    public static final int BAIT_Y = 62;
    public static final int CATCH_X = 80;
    public static final int CATCH_Y = 18;
    public static final int INV_Y = 122;
    public static final int HOTBAR_Y = 180;
    public static final int STATUS_Y = 84;
    public static final int FLAGS_Y = 96;

    public static final int CATCH_SIZE = 9;
    public static final int SLOT_FILTER = 9;
    public static final int SLOT_REEL = 10;
    public static final int SLOT_BAIT = 11;
    private static final int PLAYER_INV_START = 12;

    private final Container container;
    private final Container upgrades;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    /** Client-side: opened over the network with no extra data. */
    public AutoFisherMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(CATCH_SIZE), new SimpleContainer(3),
                ContainerLevelAccess.NULL, new SimpleContainerData(3));
    }

    /** Server-side: backed by the block entity's real containers. */
    public AutoFisherMenu(int containerId, Inventory playerInventory, Container container, Container upgrades,
                           ContainerLevelAccess access, ContainerData data) {
        super(ModMenus.AUTO_FISHER.get(), containerId);
        checkContainerSize(container, CATCH_SIZE);
        checkContainerSize(upgrades, 3);
        this.container = container;
        this.upgrades = upgrades;
        this.access = access;
        this.data = data;
        container.startOpen(playerInventory.player);
        upgrades.startOpen(playerInventory.player);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(container, col + row * 3, CATCH_X + col * 18, CATCH_Y + row * 18));
            }
        }

        addSlot(new Slot(upgrades, 0, FILTER_X, FILTER_Y));
        addSlot(new Slot(upgrades, 1, REEL_X, REEL_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.REEL_UPGRADE.get());
            }
        });
        addSlot(new Slot(upgrades, 2, BAIT_X, BAIT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof BaitItem;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, HOTBAR_Y));
        }

        addDataSlots(data);
    }

    public int synced(int index) {
        return data.get(index);
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

        if (index < PLAYER_INV_START) {
            if (!moveItemStackTo(original, PLAYER_INV_START, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (original.is(ModItems.REEL_UPGRADE.get())) {
            if (!moveItemStackTo(original, SLOT_REEL, SLOT_REEL + 1, false)
                    && !moveItemStackTo(original, 0, CATCH_SIZE, false)) {
                return ItemStack.EMPTY;
            }
        } else if (original.getItem() instanceof BaitItem) {
            if (!moveItemStackTo(original, SLOT_BAIT, SLOT_BAIT + 1, false)
                    && !moveItemStackTo(original, 0, CATCH_SIZE, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(original, 0, CATCH_SIZE, false)) {
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

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.AUTO_FISHER.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
        upgrades.stopOpen(player);
    }
}
