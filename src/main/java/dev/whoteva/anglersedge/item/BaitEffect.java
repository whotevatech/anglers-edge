package dev.whoteva.anglersedge.item;

import dev.whoteva.anglersedge.config.AnglersConfig;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import net.minecraft.util.RandomSource;

/**
 * What a bait item does to the drops of a successful catch. Applied from
 * {@link dev.whoteva.anglersedge.event.FishingEventHandler} once, after vanilla has already
 * rolled the {@code minecraft:gameplay/fishing} loot table.
 *
 * <p>The JUNK / TREASURE / FISH item sets below mirror the vanilla fishing loot table's pools.
 * They're hand-maintained rather than read from the loot table itself - simpler, and the fishing
 * pools have been stable for a long time - but a future version of this mod could read them from
 * the datapack registry instead if that drifts.
 */
public enum BaitEffect {

    /** Basic Bait: any junk in the catch is swapped for a plain fish instead. */
    UPGRADE_JUNK {
        @Override
        public void apply(NonNullList<ItemStack> drops, RandomSource random) {
            for (int i = 0; i < drops.size(); i++) {
                if (isJunk(drops.get(i).getItem())) {
                    drops.set(i, new ItemStack(randomOf(FISH, random)));
                }
            }
        }
    },

    /** Shiny Lure: on top of whatever was caught, a decent chance of a bonus treasure item. */
    BONUS_TREASURE_CHANCE {
        @Override
        public void apply(NonNullList<ItemStack> drops, RandomSource random) {
            if (random.nextFloat() < AnglersConfig.SHINY_LURE_CHANCE.get()) {
                drops.add(new ItemStack(randomOf(TREASURE, random)));
            }
        }
    },

    /** Frenzy Bait: whatever came up, you get two of it. */
    DOUBLE_CATCH {
        @Override
        public void apply(NonNullList<ItemStack> drops, RandomSource random) {
            for (ItemStack stack : drops) {
                int doubled = Math.min(stack.getCount() * 2, stack.getMaxStackSize());
                stack.setCount(doubled);
            }
        }
    },

    /** Ghost Bait: the strongest and priciest bait - the catch is replaced with treasure outright. */
    GUARANTEE_TREASURE {
        @Override
        public void apply(NonNullList<ItemStack> drops, RandomSource random) {
            drops.clear();
            drops.add(new ItemStack(randomOf(TREASURE, random)));
        }
    };

    private static final List<Item> JUNK = List.of(
            Items.BOWL, Items.STRING, Items.FISHING_ROD, Items.LEATHER, Items.LEATHER_BOOTS,
            Items.ROTTEN_FLESH, Items.STICK, Items.BONE, Items.INK_SAC, Items.TRIPWIRE_HOOK,
            Items.POTION
    );
    private static final List<Item> TREASURE = List.of(
            Items.NAME_TAG, Items.SADDLE, Items.NAUTILUS_SHELL, Items.BOW, Items.ENCHANTED_BOOK
    );
    private static final List<Item> FISH = List.of(
            Items.COD, Items.SALMON, Items.PUFFERFISH, Items.TROPICAL_FISH
    );

    public abstract void apply(NonNullList<ItemStack> drops, RandomSource random);

    public String descriptionKey() {
        return "tooltip.anglersedge.bait." + name().toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Cosmetic-only: which particle drifts up from the bobber while this bait is active, so a
     * player watching the water gets a visual read on what's about to bite. Purely client-facing;
     * has no effect on {@link #apply}.
     */
    public SimpleParticleType particle() {
        return switch (this) {
            case UPGRADE_JUNK -> ParticleTypes.BUBBLE;
            case BONUS_TREASURE_CHANCE -> ParticleTypes.CRIT;
            case DOUBLE_CATCH -> ParticleTypes.FLAME;
            case GUARANTEE_TREASURE -> ParticleTypes.SOUL;
        };
    }

    private static boolean isJunk(Item item) {
        return JUNK.contains(item);
    }

    private static Item randomOf(List<Item> items, RandomSource random) {
        return items.get(random.nextInt(items.size()));
    }
}
