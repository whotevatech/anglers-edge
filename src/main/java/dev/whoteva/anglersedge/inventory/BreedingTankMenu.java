package dev.whoteva.anglersedge.inventory;

import dev.whoteva.anglersedge.block.ModBlocks;
import dev.whoteva.anglersedge.block.entity.BreedingTankBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Two breeding-pair slots up top (restricted to fish species - see
 * {@link BreedingTankBlockEntity#isBreedable}) plus a 3x2 output grid below for whatever the tank
 * has produced.
 */
public class BreedingTankMenu extends AbstractContainerMenu {

    private static final int PARENT_A = BreedingTankBlockEntity.PARENT_A;
    private static final int PARENT_B = BreedingTankBlockEntity.PARENT_B;
    private static final int CONTAINER_SIZE = BreedingTankBlockEntity.SLOTS;
    private static final int PLAYER_INV_START = CONTAINER_SIZE;

    private final Container container;
    private final ContainerLevelAccess access;

    /** Client-side: opened over the network with no extra data. */
    public BreedingTankMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(CONTAINER_SIZE), ContainerLevelAccess.NULL);
    }

    /** Server-side: backed by the block entity's real container. */
    public BreedingTankMenu(int containerId, Inventory playerInventory, Container container, ContainerLevelAccess access) {
        super(ModMenus.BREEDING_TANK.get(), containerId);
        checkContainerSize(container, CONTAINER_SIZE);
        this.container = container;
        this.access = access;
        container.startOpen(playerInventory.player);

        addSlot(new Slot(container, PARENT_A, 62, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BreedingTankBlockEntity.isBreedable(stack.getItem());
            }
        });
        addSlot(new Slot(container, PARENT_B, 98, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BreedingTankBlockEntity.isBreedable(stack.getItem());
            }
        });

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(container, 2 + col + row * 3, 62 + col * 18, 44 + row * 18));
            }
        }

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
        } else if (BreedingTankBlockEntity.isBreedable(original.getItem())
                && (!slots.get(PARENT_A).hasItem() || !slots.get(PARENT_B).hasItem())) {
            // Prefer filling an empty breeding-pair slot when shift-clicking fish in from the
            // player's own inventory; otherwise fall back to the output grid.
            if (!moveItemStackTo(original, PARENT_A, PARENT_B + 1, false)) {
                moveItemStackTo(original, PARENT_B + 1, CONTAINER_SIZE, false);
            }
        } else {
            if (!moveItemStackTo(original, PARENT_B + 1, CONTAINER_SIZE, false)) {
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
        return stillValid(access, player, ModBlocks.BREEDING_TANK.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
