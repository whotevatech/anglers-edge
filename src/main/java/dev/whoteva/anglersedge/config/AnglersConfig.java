package dev.whoteva.anglersedge.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Gameplay rates that used to be private constants. Defaults match the 1.0.0 feel, except the
 * handheld biome-flavor chance, which is a bit higher so the new cold, warm, deep, and cave
 * fish actually show up on a rod. Auto-fisher loot tables are unaffected by that knob.
 */
public final class AnglersConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue LEVIATHAN_CHANCE;
    public static final ModConfigSpec.DoubleValue SHARK_CHANCE;
    public static final ModConfigSpec.DoubleValue WHALE_CHANCE;

    public static final ModConfigSpec.DoubleValue BIOME_FLAVOR_CHANCE;
    public static final ModConfigSpec.DoubleValue SHINY_LURE_CHANCE;
    public static final ModConfigSpec.DoubleValue LEVIATHAN_BONUS_CHANCE;
    public static final ModConfigSpec.BooleanValue WEATHER_BONUSES;

    public static final ModConfigSpec.IntValue PASSIVE_INTERVAL;
    public static final ModConfigSpec.IntValue POWERED_INTERVAL;
    public static final ModConfigSpec.IntValue WEATHER_INTERVAL_PERCENT;

    public static final ModConfigSpec.IntValue BREED_TIME;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("encounters");
        LEVIATHAN_CHANCE = builder
                .comment("Chance per successful catch that an Ancient Leviathan rises. 0 disables it.")
                .defineInRange("leviathanChance", 0.03D, 0.0D, 1.0D);
        SHARK_CHANCE = builder
                .comment("Chance per successful catch that a Reef Shark shows up in ocean or river water.")
                .defineInRange("sharkChance", 0.06D, 0.0D, 1.0D);
        WHALE_CHANCE = builder
                .comment("Chance per successful ocean catch of a distant whale sighting.")
                .defineInRange("whaleChance", 0.05D, 0.0D, 1.0D);
        builder.pop();

        builder.push("fishing");
        BIOME_FLAVOR_CHANCE = builder
                .comment("Handheld rods only: chance to swap one vanilla drop for a biome fish. Auto-fishers roll loot tables instead.")
                .defineInRange("biomeFlavorChance", 0.25D, 0.0D, 1.0D);
        SHINY_LURE_CHANCE = builder
                .comment("Chance that Shiny Lure adds a bonus treasure item.")
                .defineInRange("shinyLureChance", 0.20D, 0.0D, 1.0D);
        LEVIATHAN_BONUS_CHANCE = builder
                .comment("Chance that the Leviathan Rod duplicates one caught stack.")
                .defineInRange("leviathanBonusChance", 0.25D, 0.0D, 1.0D);
        WEATHER_BONUSES = builder
                .comment("Rain adds +1 luck and night adds +1 lure on this mod's rods, and both shorten the auto-fisher wait.")
                .define("weatherBonuses", true);
        builder.pop();

        builder.push("autoFisher");
        PASSIVE_INTERVAL = builder
                .comment("Ticks between auto-fisher catches with no redstone signal. 20 ticks is one second.")
                .defineInRange("passiveInterval", 200, 20, 20 * 60 * 10);
        POWERED_INTERVAL = builder
                .comment("Ticks between catches while the block is powered.")
                .defineInRange("poweredInterval", 60, 20, 20 * 60 * 10);
        WEATHER_INTERVAL_PERCENT = builder
                .comment("During rain or night, the wait is multiplied by this percent. 67 is about two thirds.")
                .defineInRange("weatherIntervalPercent", 67, 20, 100);
        builder.pop();

        builder.push("breeding");
        BREED_TIME = builder
                .comment("Ticks to produce one offspring once a matching pair is stocked next to water.")
                .defineInRange("breedTime", 600, 20, 20 * 60 * 30);
        builder.pop();

        SPEC = builder.build();
    }

    private AnglersConfig() {
    }
}
