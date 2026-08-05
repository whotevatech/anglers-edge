package dev.whoteva.anglersedge.block.entity;

import dev.whoteva.anglersedge.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds exactly one mounted fish. A plain single-slot {@link Container} (rather than a bespoke
 * field with hand-written persistence) so it gets vanilla's existing "drop contents when the
 * block is broken" behaviour for free via {@code Containers.dropContentsOnDestroy} in
 * {@link dev.whoteva.anglersedge.block.FishTrophyBlock#onRemove}.
 */
public class FishTrophyBlockEntity extends BlockEntity implements Container, Clearable {

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    public FishTrophyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISH_TROPHY.get(), pos, state);
    }

    public ItemStack displayed() {
        return items.get(0);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(0);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, 0, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, 0);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(0, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.set(0, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    // --- client sync: the mounted fish needs to reach the client for the renderer to draw it ---

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
