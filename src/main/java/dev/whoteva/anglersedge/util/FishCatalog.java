package dev.whoteva.anglersedge.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Locale;

/**
 * Every species the journal, trophy, breeding tank, and weight roller agree on.
 * Item lookups are lazy so this class can load before mod items are registered.
 */
public final class FishCatalog {

    public enum Habitat {
        ANY, OCEAN, WARM, COLD, DEEP, RIVER, SWAMP, CAVE, ENCOUNTER;

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final class Species {
        private final String id;
        private final FishRarity.Tier rarity;
        private final Habitat habitat;
        private final float minWeight;
        private final float maxWeight;
        private final boolean sizeable;
        private final boolean breedable;
        private Item cached;

        private Species(String id, FishRarity.Tier rarity, Habitat habitat,
                        float minWeight, float maxWeight, boolean sizeable, boolean breedable) {
            this.id = id;
            this.rarity = rarity;
            this.habitat = habitat;
            this.minWeight = minWeight;
            this.maxWeight = maxWeight;
            this.sizeable = sizeable;
            this.breedable = breedable;
        }

        public String id() {
            return id;
        }

        public FishRarity.Tier rarity() {
            return rarity;
        }

        public Habitat habitat() {
            return habitat;
        }

        public float minWeight() {
            return minWeight;
        }

        public float maxWeight() {
            return maxWeight;
        }

        public boolean sizeable() {
            return sizeable;
        }

        public boolean breedable() {
            return breedable;
        }

        public Item item() {
            if (cached != null && cached != Items.AIR) {
                return cached;
            }
            Item resolved = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
            if (resolved != Items.AIR) {
                cached = resolved;
            }
            return resolved;
        }
    }

    public static final Species COD = species("minecraft:cod", FishRarity.Tier.COMMON, Habitat.ANY, 1f, 8f, true, true);
    public static final Species SALMON = species("minecraft:salmon", FishRarity.Tier.COMMON, Habitat.ANY, 2f, 15f, true, true);
    public static final Species PUFFERFISH = species("minecraft:pufferfish", FishRarity.Tier.COMMON, Habitat.ANY, 0.5f, 3f, true, true);
    public static final Species TROPICAL_FISH = species("minecraft:tropical_fish", FishRarity.Tier.COMMON, Habitat.WARM, 0.2f, 2f, true, true);
    public static final Species REEF_CRAB = species("anglersedge:reef_crab", FishRarity.Tier.COMMON, Habitat.OCEAN, 0.5f, 4f, true, true);
    public static final Species GLIMMERFIN = species("anglersedge:glimmerfin", FishRarity.Tier.UNCOMMON, Habitat.OCEAN, 1f, 6f, true, true);
    public static final Species SUNSCALE = species("anglersedge:sunscale", FishRarity.Tier.UNCOMMON, Habitat.WARM, 1f, 7f, true, true);
    public static final Species FROST_CHAR = species("anglersedge:frost_char", FishRarity.Tier.UNCOMMON, Habitat.COLD, 2f, 12f, true, true);
    public static final Species SWAMP_EEL = species("anglersedge:swamp_eel", FishRarity.Tier.UNCOMMON, Habitat.SWAMP, 2f, 10f, true, true);
    public static final Species CAVE_LOACH = species("anglersedge:cave_loach", FishRarity.Tier.RARE, Habitat.CAVE, 0.4f, 2.5f, true, true);
    public static final Species ANGLERFISH = species("anglersedge:anglerfish", FishRarity.Tier.RARE, Habitat.DEEP, 3f, 20f, true, true);
    public static final Species SHARK_TOOTH = species("anglersedge:shark_tooth", FishRarity.Tier.RARE, Habitat.ENCOUNTER, 0f, 0f, false, false);
    public static final Species ABYSSAL_RAY = species("anglersedge:abyssal_ray", FishRarity.Tier.EPIC, Habitat.DEEP, 10f, 60f, true, true);
    public static final Species LEVIATHAN_SCALE = species("anglersedge:leviathan_scale", FishRarity.Tier.BOSS, Habitat.ENCOUNTER, 0f, 0f, false, false);

    private static final List<Species> ALL = List.of(
            COD, SALMON, PUFFERFISH, TROPICAL_FISH,
            REEF_CRAB, GLIMMERFIN, SUNSCALE, FROST_CHAR, SWAMP_EEL,
            CAVE_LOACH, ANGLERFISH, SHARK_TOOTH, ABYSSAL_RAY, LEVIATHAN_SCALE);

    private FishCatalog() {
    }

    private static Species species(String id, FishRarity.Tier rarity, Habitat habitat,
                                   float minWeight, float maxWeight, boolean sizeable, boolean breedable) {
        return new Species(id, rarity, habitat, minWeight, maxWeight, sizeable, breedable);
    }

    public static List<Species> all() {
        return ALL;
    }

    public static int size() {
        return ALL.size();
    }

    public static Species byItem(Item item) {
        for (Species species : ALL) {
            if (species.item() == item) {
                return species;
            }
        }
        return null;
    }

    public static boolean isBreedable(Item item) {
        Species species = byItem(item);
        return species != null && species.breedable();
    }

    public static boolean isDisplayable(Item item) {
        return byItem(item) != null;
    }

    /**
     * One signature fish for a handheld cast in this biome bucket, or null when the bucket
     * has no modded flavor (the generic fallback keeps the vanilla drop).
     */
    public static Item flavorCatch(String category, RandomSource random) {
        Species[] pool = switch (category) {
            case "ocean" -> new Species[]{GLIMMERFIN, GLIMMERFIN, REEF_CRAB, ANGLERFISH};
            case "swamp" -> new Species[]{SWAMP_EEL};
            case "river" -> new Species[]{GLIMMERFIN, SWAMP_EEL, REEF_CRAB};
            case "cold" -> new Species[]{FROST_CHAR, FROST_CHAR, GLIMMERFIN};
            case "warm" -> new Species[]{SUNSCALE, SUNSCALE, REEF_CRAB, TROPICAL_FISH};
            case "deep" -> new Species[]{ABYSSAL_RAY, ANGLERFISH, GLIMMERFIN};
            case "cave" -> new Species[]{CAVE_LOACH};
            default -> null;
        };
        if (pool == null || pool.length == 0) {
            return null;
        }
        return pool[random.nextInt(pool.length)].item();
    }
}
