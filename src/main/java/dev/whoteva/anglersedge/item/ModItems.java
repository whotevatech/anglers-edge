package dev.whoteva.anglersedge.item;

import dev.whoteva.anglersedge.block.ModBlocks;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static dev.whoteva.anglersedge.AnglersEdge.MOD_ID;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    // --- Rod tiers -----------------------------------------------------------------------
    // Luck/lure numbers are the same units vanilla's Luck of the Sea / Lure enchantments use
    // (each point trims a few seconds off the average bite and skews the loot roll).

    public static final DeferredHolder<Item, TieredFishingRodItem> STURDY_ROD = ITEMS.register(
            "sturdy_rod",
            () -> new TieredFishingRodItem(0, 1, new Item.Properties().durability(96)));

    public static final DeferredHolder<Item, TieredFishingRodItem> ANGLERS_ROD = ITEMS.register(
            "anglers_rod",
            () -> new TieredFishingRodItem(1, 2, new Item.Properties().durability(192)));

    public static final DeferredHolder<Item, TieredFishingRodItem> LEVIATHAN_ROD = ITEMS.register(
            "leviathan_rod",
            // Leviathan's Bounty: 25% chance per catch of a bonus duplicate drop - see
            // TieredFishingRodItem's bonusRollChance javadoc.
            () -> new TieredFishingRodItem(2, 3, 0.25f, new Item.Properties().durability(384)));

    // --- Bait ------------------------------------------------------------------------------

    public static final DeferredHolder<Item, BaitItem> BASIC_BAIT = ITEMS.register(
            "basic_bait",
            () -> new BaitItem(BaitEffect.UPGRADE_JUNK, new Item.Properties()));

    public static final DeferredHolder<Item, BaitItem> SHINY_LURE = ITEMS.register(
            "shiny_lure",
            () -> new BaitItem(BaitEffect.BONUS_TREASURE_CHANCE, new Item.Properties()));

    public static final DeferredHolder<Item, BaitItem> FRENZY_BAIT = ITEMS.register(
            "frenzy_bait",
            () -> new BaitItem(BaitEffect.DOUBLE_CATCH, new Item.Properties()));

    public static final DeferredHolder<Item, BaitItem> GHOST_BAIT = ITEMS.register(
            "ghost_bait",
            () -> new BaitItem(BaitEffect.GUARANTEE_TREASURE, new Item.Properties()));

    // --- Fish species ----------------------------------------------------------------------
    // Biome-flavored catches: which loot table these come from is decided in
    // data/anglersedge/loot_table/fishing/*.json and dev.whoteva.anglersedge.util.BiomeCategory,
    // not here - this is just the item definitions.

    public static final DeferredHolder<Item, Item> GLIMMERFIN = ITEMS.register(
            "glimmerfin",
            () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(3).saturationModifier(0.15F).build())));

    public static final DeferredHolder<Item, Item> SWAMP_EEL = ITEMS.register(
            "swamp_eel",
            () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build())));

    public static final DeferredHolder<Item, Item> ABYSSAL_RAY = ITEMS.register(
            "abyssal_ray",
            () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(6).saturationModifier(0.4F).build())));

    /** Drops only from the Ancient Leviathan - see dev.whoteva.anglersedge.event.LeviathanEncounterHandler. */
    public static final DeferredHolder<Item, Item> LEVIATHAN_SCALE = ITEMS.register(
            "leviathan_scale",
            () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> REEF_CRAB = ITEMS.register(
            "reef_crab",
            () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(2).saturationModifier(0.2F).build())));

    public static final DeferredHolder<Item, Item> ANGLERFISH = ITEMS.register(
            "anglerfish",
            () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(5).saturationModifier(0.3F).build())));

    /** Drops only from the Reef Shark - see dev.whoteva.anglersedge.event.ReefSharkHandler. */
    public static final DeferredHolder<Item, Item> SHARK_TOOTH = ITEMS.register(
            "shark_tooth",
            () -> new Item(new Item.Properties()));

    // --- Auto-fisher upgrades -----------------------------------------------------------

    public static final DeferredHolder<Item, Item> REEL_UPGRADE = ITEMS.register(
            "reel_upgrade",
            () -> new Item(new Item.Properties()));

    /** Crafting ingredient - woven string net, used to build the auto-fisher (see its recipe). */
    public static final DeferredHolder<Item, Item> NET = ITEMS.register(
            "net",
            () -> new Item(new Item.Properties()));

    // --- Journal -----------------------------------------------------------------------

    public static final DeferredHolder<Item, FishingJournalItem> FISHING_JOURNAL = ITEMS.register(
            "fishing_journal",
            () -> new FishingJournalItem(new Item.Properties().stacksTo(1)));

    // --- Block items ------------------------------------------------------------------
    // Registering a block does NOT register a corresponding item - without these, the blocks
    // can be placed via /setblock but never held, crafted, dropped, or shown in a creative tab.

    public static final DeferredHolder<Item, BlockItem> UPGRADE_STATION = ITEMS.registerSimpleBlockItem(
            "upgrade_station", ModBlocks.UPGRADE_STATION);

    public static final DeferredHolder<Item, BlockItem> AUTO_FISHER = ITEMS.registerSimpleBlockItem(
            "auto_fisher", ModBlocks.AUTO_FISHER);

    public static final DeferredHolder<Item, BlockItem> FISH_TROPHY = ITEMS.registerSimpleBlockItem(
            "fish_trophy", ModBlocks.FISH_TROPHY);

    public static final DeferredHolder<Item, BlockItem> BREEDING_TANK = ITEMS.registerSimpleBlockItem(
            "breeding_tank", ModBlocks.BREEDING_TANK);

    private ModItems() {
    }
}
