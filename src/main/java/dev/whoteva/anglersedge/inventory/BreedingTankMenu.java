package dev.whoteva.anglersedge.inventory;

import dev.whoteva.anglersedge.advancement.AnglersCriteria;
import dev.whoteva.anglersedge.block.ModBlocks;
import dev.whoteva.anglersedge.block.entity.BreedingTankBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Two breeding-pair slots and a take-only output grid. Output is not a dump slot: shift-click
 * only fills an empty parent slot, and taking an offspring grants the breeding advancement.
 */
public class BreedingTankMenu extends AbstractContainerMenu {

    public static final int IMAGE_WIDTH = 176;
    public static final int IMAGE_HEIGHT = 208;
    public static final int PARENT_A_X = 34;
    public static final int PARENT_A_Y = 18;
    public static final int PARENT_B_X = 124;
    public static final int PARENT_B_Y = 18;
    public static final int OUTPUT_X = 61;
    public static final int OUTPUT_Y = 44;
    public static final int INV_Y = 126;
    public static final int HOTBAR_Y = 184;
    public static final int STATUS_Y = 88;
    public static final int BAR_Y = 100;

    private static final int PARENT_A = BreedingTankBlockEntity.PARENT_A;
    private static final int PARENT_B = BreedingTankBlockEntity.PARENT_B;
    private static final int CONTAINER_SIZE = BreedingTankBlockEntity.SLOTS;
    private static final int PLAYER_INV_START = CONTAINER_SIZE;

    private final Container container;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    /** Client-side: opened over the network with no extra data. */
    public BreedingTankMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(CONTAINER_SIZE), ContainerLevelAccess.NULL,
                new SimpleContainerData(3));
    }

    /** Server-side: backed by the block entity's real container. */
    public BreedingTankMenu(int containerId, Inventory playerInventory, Container container,
                             ContainerLevelAccess access, ContainerData data) {
        super(ModMenus.BREEDING_TANK.get(), containerId);
        checkContainerSize(container, CONTAINER_SIZE);
        this.container = container;
        this.access = access;
        this.data = data;
        container.startOpen(playerInventory.player);

        addSlot(parentSlot(container, PARENT_A, PARENT_A_X, PARENT_A_Y));
        addSlot(parentSlot(container, PARENT_B, PARENT_B_X, PARENT_B_Y));

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int index = 2 + col + row * 3;
                addSlot(new Slot(container, index, OUTPUT_X + col * 18, OUTPUT_Y + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public void onTake(Player player, ItemStack stack) {
                        super.onTake(player, stack);
                        if (player instanceof ServerPlayer serverPlayer) {
                            AnglersCriteria.trigger(serverPlayer, AnglersCriteria.FISH_BRED);
                        }
                    }
                });
            }
        }

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

    private static Slot parentSlot(Container container, int index, int x, int y) {
        return new Slot(container, index, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BreedingTankBlockEntity.isBreedable(stack.getItem());
            }
        };
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
        } else if (BreedingTankBlockEntity.isBreedable(original.getItem())) {
            if (!moveItemStackTo(original, PARENT_A, PARENT_B + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
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
        return stillValid(access, player, ModBlocks.BREEDING_TANK.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
