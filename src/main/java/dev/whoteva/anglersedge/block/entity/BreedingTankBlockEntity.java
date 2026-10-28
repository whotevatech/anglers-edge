package dev.whoteva.anglersedge.block.entity;

import dev.whoteva.anglersedge.block.ModBlockEntities;
import dev.whoteva.anglersedge.config.AnglersConfig;
import dev.whoteva.anglersedge.inventory.BreedingTankMenu;
import dev.whoteva.anglersedge.util.FishCatalog;
import dev.whoteva.anglersedge.util.FishWeight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * A slower, stockable alternative to the auto-fisher: stock two of the same fish species in the
 * breeding-pair slots, and — as long as the tank sits next to water — it periodically produces
 * one more of that species into the output slots. The pair itself is never consumed.
 * Which items count as fish comes from {@link FishCatalog}.
 */
public class BreedingTankBlockEntity extends BlockEntity implements Container, Clearable, MenuProvider {

    public static final int STATUS_NO_WATER = 0;
    public static final int STATUS_NEED_PAIR = 1;
    public static final int STATUS_MISMATCH = 2;
    public static final int STATUS_BREEDING = 3;
    public static final int STATUS_FULL = 4;

    public static final int PARENT_A = 0;
    public static final int PARENT_B = 1;
    private static final int OUTPUT_START = 2;
    private static final int OUTPUT_SLOTS = 6;
    public static final int SLOTS = OUTPUT_START + OUTPUT_SLOTS;

    public static boolean isBreedable(Item item) {
        return FishCatalog.isBreedable(item);
    }

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private int progress;
    private int status = STATUS_NEED_PAIR;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> AnglersConfig.BREED_TIME.get();
                case 2 -> status;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public BreedingTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BREEDING_TANK.get(), pos, state);
    }

    public ContainerData data() {
        return data;
    }

    public int progress() {
        return progress;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BreedingTankBlockEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!entity.hasAdjacentWater(level, pos)) {
            entity.progress = 0;
            entity.status = STATUS_NO_WATER;
            return;
        }

        ItemStack a = entity.items.get(PARENT_A);
        ItemStack b = entity.items.get(PARENT_B);
        if (a.isEmpty() || b.isEmpty()) {
            entity.progress = 0;
            entity.status = STATUS_NEED_PAIR;
            return;
        }
        if (!ItemStack.isSameItem(a, b) || !isBreedable(a.getItem())) {
            entity.progress = 0;
            entity.status = STATUS_MISMATCH;
            return;
        }
        if (!entity.hasOutputSpace()) {
            entity.status = STATUS_FULL;
            return;
        }

        entity.status = STATUS_BREEDING;
        entity.progress++;
        int breedTime = AnglersConfig.BREED_TIME.get();
        if (entity.progress % 20 == 0) {
            entity.setChanged();
        }
        if (entity.progress < breedTime) {
            return;
        }
        entity.progress = 0;
        ItemStack offspring = new ItemStack(a.getItem(), 1);
        FishWeight.apply(offspring, serverLevel.getRandom());
        entity.insertOutput(offspring);
        serverLevel.playSound(null, pos, SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.BLOCKS, 0.4F, 1.15F);
    }

    /** Offspring carry a unique name, so they need an empty output slot rather than a partial stack. */
    private boolean hasOutputSpace() {
        for (int i = OUTPUT_START; i < SLOTS; i++) {
            if (items.get(i).isEmpty()) {
                return true;
            }
        }
        return false;
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
        return new BreedingTankMenu(containerId, playerInventory, this, ContainerLevelAccess.create(level, worldPosition), data);
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
