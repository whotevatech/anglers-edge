package dev.whoteva.anglersedge.block.entity;

import dev.whoteva.anglersedge.block.ModBlockEntities;
import dev.whoteva.anglersedge.config.AnglersConfig;
import dev.whoteva.anglersedge.inventory.AutoFisherMenu;
import dev.whoteva.anglersedge.item.BaitItem;
import dev.whoteva.anglersedge.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/**
 * Sits next to water and periodically rolls the vanilla fishing loot table into its own
 * inventory - no bobber, no bite timer, just "is there water nearby, and has enough time passed".
 *
 * <p>Passive by default. Any redstone signal speeds it up. Intervals live in
 * {@link dev.whoteva.anglersedge.config.AnglersConfig}. Output sits in a 9-slot inventory that a
 * hopper (or this mod's {@code IItemHandler} capability) can pull from directly. A bait in the
 * third upgrade slot is consumed once per cycle that actually stores a catch.
 */
public class AutoFisherBlockEntity extends BlockEntity implements Container, Clearable, MenuProvider {

    public static final int FLAG_WATER = 1;
    public static final int FLAG_POWERED = 1 << 1;
    public static final int FLAG_STALLED = 1 << 2;
    public static final int FLAG_FILTER = 1 << 3;
    public static final int FLAG_REEL = 1 << 4;
    public static final int FLAG_BAIT = 1 << 5;

    private static final int SLOTS = 9;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private int cooldown = 200;
    private int interval = 200;
    private boolean hasWater;
    private boolean powered;
    private boolean stalled;

    /** Slot 0: filter. Slot 1: reel upgrade. Slot 2: bait, consumed once per successful cycle. */
    private final SimpleContainer upgrades = new SimpleContainer(3);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cooldown;
                case 1 -> interval;
                case 2 -> statusFlags();
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

    public ContainerData data() {
        return data;
    }

    public AutoFisherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AUTO_FISHER.get(), pos, state);
        upgrades.addListener(c -> setChanged());
    }

    public Container upgrades() {
        return upgrades;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AutoFisherBlockEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        entity.hasWater = entity.hasAdjacentWater(level, pos);
        entity.powered = level.hasNeighborSignal(pos);
        if (!entity.hasWater) {
            entity.stalled = false;
            entity.interval = AnglersConfig.PASSIVE_INTERVAL.get();
            return;
        }
        if (entity.outputFull()) {
            entity.stalled = true;
            return;
        }

        entity.cooldown--;
        if (entity.cooldown > 0) {
            return;
        }

        int next = entity.powered ? AnglersConfig.POWERED_INTERVAL.get() : AnglersConfig.PASSIVE_INTERVAL.get();
        if (AnglersConfig.WEATHER_BONUSES.get() && (level.isRaining() || level.isNight())) {
            next = Math.max(20, next * AnglersConfig.WEATHER_INTERVAL_PERCENT.get() / 100);
        }
        entity.interval = next;
        entity.cooldown = next;

        LootTable table = serverLevel.getServer().reloadableRegistries().getLootTable(lootTableFor(level, pos));
        LootParams params = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD))
                .create(LootContextParamSets.FISHING);

        ItemStack filter = entity.upgrades.getItem(0);
        boolean hasReelUpgrade = entity.upgrades.getItem(1).is(ModItems.REEL_UPGRADE.get());
        int rolls = hasReelUpgrade ? 2 : 1;

        NonNullList<ItemStack> caught = NonNullList.create();
        for (int roll = 0; roll < rolls; roll++) {
            for (ItemStack stack : table.getRandomItems(params, serverLevel.getRandom())) {
                if (!filter.isEmpty() && !stack.is(filter.getItem())) {
                    continue;
                }
                caught.add(stack);
            }
        }
        if (caught.isEmpty()) {
            entity.stalled = false;
            return;
        }

        ItemStack bait = entity.upgrades.getItem(2);
        if (bait.getItem() instanceof BaitItem baitItem) {
            baitItem.effect().apply(caught, serverLevel.getRandom());
        }

        boolean inserted = false;
        for (ItemStack stack : caught) {
            if (stack.isEmpty()) {
                continue;
            }
            dev.whoteva.anglersedge.util.FishWeight.apply(stack, serverLevel.getRandom());
            int before = stack.getCount();
            entity.insert(stack);
            if (stack.getCount() != before) {
                inserted = true;
            }
        }

        if (!inserted) {
            entity.stalled = true;
            entity.cooldown = 20;
            return;
        }
        entity.stalled = false;
        if (bait.getItem() instanceof BaitItem) {
            bait.shrink(1);
        }
        serverLevel.playSound(null, pos, SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.BLOCKS, 0.35F, 1.0F);
    }

    private int statusFlags() {
        int flags = 0;
        if (hasWater) flags |= FLAG_WATER;
        if (powered) flags |= FLAG_POWERED;
        if (stalled) flags |= FLAG_STALLED;
        if (!upgrades.getItem(0).isEmpty()) flags |= FLAG_FILTER;
        if (upgrades.getItem(1).is(ModItems.REEL_UPGRADE.get())) flags |= FLAG_REEL;
        if (upgrades.getItem(2).getItem() instanceof BaitItem) flags |= FLAG_BAIT;
        return flags;
    }

    private boolean outputFull() {
        for (ItemStack stack : items) {
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Picks one of this mod's biome-flavored fishing tables ({@code data/anglersedge/loot_table/fishing/}),
     * falling back to vanilla's own fishing table for biomes that don't fall into a recognized
     * bucket. See {@link dev.whoteva.anglersedge.util.BiomeCategory}.
     */
    private static net.minecraft.resources.ResourceKey<LootTable> lootTableFor(Level level, BlockPos pos) {
        String category = dev.whoteva.anglersedge.util.BiomeCategory.at(level, pos);
        if (category.equals("generic")) {
            return BuiltInLootTables.FISHING;
        }
        return net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.LOOT_TABLE,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                        dev.whoteva.anglersedge.AnglersEdge.MOD_ID, "fishing/" + category));
    }

    private boolean hasAdjacentWater(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getFluidState(pos.relative(direction)).is(net.minecraft.world.level.material.Fluids.WATER)) {
                return true;
            }
        }
        return false;
    }

    private void insert(ItemStack stack) {
        for (int i = 0; i < items.size() && !stack.isEmpty(); i++) {
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
        return Component.translatable("container.anglersedge.auto_fisher");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AutoFisherMenu(containerId, playerInventory, this, upgrades,
                ContainerLevelAccess.create(level, worldPosition), data);
    }

    // --- persistence ---------------------------------------------------------------------

    @Override
    protected void loadAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        cooldown = tag.getInt("Cooldown");
        if (tag.contains("Upgrades", net.minecraft.nbt.Tag.TAG_LIST)) {
            upgrades.fromTag(tag.getList("Upgrades", net.minecraft.nbt.Tag.TAG_COMPOUND), registries);
        }
    }

    @Override
    protected void saveAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Cooldown", cooldown);
        tag.put("Upgrades", upgrades.createTag(registries));
    }
}
