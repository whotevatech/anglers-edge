package dev.whoteva.anglersedge.block.entity;

import dev.whoteva.anglersedge.block.ModBlockEntities;
import dev.whoteva.anglersedge.inventory.BreedingTankMenu;
import dev.whoteva.anglersedge.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.Set;

/**
 * A slower, stockable alternative to the auto-fisher: stock two of the same fish species in the
 * breeding-pair slots, and - as long as the tank sits next to water - it periodically produces
 * one more of that species into the output slots. The pair itself is never consumed, so this is
 * a standing "farm" rather than a one-shot craft, which felt like the right shape for something
 * called a breeding tank.
 */
public class BreedingTankBlockEntity extends BlockEntity implements Container, Clearable, MenuProvider {

    public static final int PARENT_A = 0;
    public static final int PARENT_B = 1;
    private static final int OUTPUT_START = 2;
    private static final int OUTPUT_SLOTS = 6;
    public static final int SLOTS = OUTPUT_START + OUTPUT_SLOTS;

    private static final int BREED_TIME = 600;

    // Lazily built, not an eagerly-evaluated static field - see FishTrophyBlock's displayable()
    // for why referencing other DeferredHolders too early throws "trying to access unbound
    // value". BlockEntity classes are only loaded when a player actually places one, which is
    // safely after registration, but the lazy pattern costs nothing and stays consistent.
    private static Set<Item> breedable;

    private static Set<Item> breedable() {
        if (breedable == null) {
            breedable = Set.of(
                    Items.COD, Items.SALMON, Items.PUFFERFISH, Items.TROPICAL_FISH,
                    ModItems.GLIMMERFIN.get(), ModItems.SWAMP_EEL.get(), ModItems.ABYSSAL_RAY.get(),
                    ModItems.REEF_CRAB.get(), ModItems.ANGLERFISH.get());
        }
        return breedable;
    }

    public static boolean isBreedable(Item item) {
        return breedable().contains(item);
    }

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private int progress;

    public BreedingTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BREEDING_TANK.get(), pos, state);
    }

    public int progress() {
        return progress;
    }

    public static int breedTime() {
        return BREED_TIME;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BreedingTankBlockEntity entity) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        if (!entity.hasAdjacentWater(level, pos)) {
            entity.progress = 0;
            return;
        }

        ItemStack a = entity.items.get(PARENT_A);
        ItemStack b = entity.items.get(PARENT_B);
        if (a.isEmpty() || b.isEmpty() || !ItemStack.isSameItem(a, b) || !isBreedable(a.getItem())) {
            entity.progress = 0;
            return;
        }

        entity.progress++;
        if (entity.progress < BREED_TIME) {
            entity.setChanged();
            return;
        }
        entity.progress = 0;
        entity.insertOutput(new ItemStack(a.getItem(), 1));
    }

    private boolean hasAdjacentWater(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getFluidState(pos.relative(direction)).is(Fluids.WATER)) {
                return true;
            }
        }
        return false;
    }

    private void insertOutput(ItemStack stack) {
        for (int i = OUTPUT_START; i < SLOTS && !stack.isEmpty(); i++) {
            ItemStack slot = items.get(i);
            if (slot.isEmpty()) {
                items.set(i, stack);
                stack = ItemStack.EMPTY;
            } else if (ItemStack.isSameItemSameComponents(slot, stack)) {
                int space = slot.getMaxStackSize() - slot.getCount();
                int moved = Math.min(space, stack.getCount());
                slot.grow(moved);
                stack.shrink(moved);
            }
        }
        setChanged();
    }

    // --- Container ---------------------------------------------------------------------

    @Override
    public int getContainerSize() {
        return SLOTS;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
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

    // --- menu ---------------------------------------------------------------------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.anglersedge.breeding_tank");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BreedingTankMenu(containerId, playerInventory, this, ContainerLevelAccess.create(level, worldPosition));
    }

    // --- persistence ---------------------------------------------------------------------

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        progress = tag.getInt("Progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Progress", progress);
    }
}
