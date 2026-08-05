package dev.whoteva.anglersedge.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * Coarse biome bucketing used to pick a fishing loot table. Deliberately simple - three buckets
 * (swamp, river, ocean) plus a generic fallback - rather than trying to mirror vanilla's full
 * biome list, since the loot tables in {@code data/anglersedge/loot_table/fishing/} only need to
 * be different enough to make "where you're fishing" matter.
 */
public final class BiomeCategory {

    private BiomeCategory() {
    }

    public static String at(Level level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        if (biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP)) {
            return "swamp";
        }
        if (biome.is(BiomeTags.IS_RIVER)) {
            return "river";
        }
        if (biome.is(BiomeTags.IS_OCEAN)) {
            return "ocean";
        }
        return "generic";
    }
}
