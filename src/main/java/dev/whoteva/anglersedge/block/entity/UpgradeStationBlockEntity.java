package dev.whoteva.anglersedge.block.entity;

import dev.whoteva.anglersedge.inventory.UpgradeStationMenu;
import dev.whoteva.anglersedge.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class UpgradeStationBlockEntity extends BaseContainerBlockEntity {

    private NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

    public UpgradeStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UPGRADE_STATION.get(), pos, state);
    }

    @Override
    public int getContainerSize() {
        return 3;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.anglersedge.upgrade_station");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new UpgradeStationMenu(containerId, playerInventory, this, ContainerLevelAccess.create(level, worldPosition));
    }
}
