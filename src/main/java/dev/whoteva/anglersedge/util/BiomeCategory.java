package dev.whoteva.anglersedge.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * Coarse biome buckets for fishing loot. Specific biomes are checked before the broad ocean
 * and river tags so a frozen river is cold and a deep ocean is deep.
 */
public final class BiomeCategory {

    private BiomeCategory() {
    }

    public static String at(Level level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        if (biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP)) {
            return "swamp";
        }
        if (biome.is(Biomes.LUSH_CAVES) || biome.is(Biomes.DRIPSTONE_CAVES)) {
            return "cave";
        }
        if (biome.is(Biomes.FROZEN_OCEAN) || biome.is(Biomes.DEEP_FROZEN_OCEAN) || biome.is(Biomes.COLD_OCEAN)
                || biome.is(Biomes.FROZEN_RIVER) || biome.is(Biomes.SNOWY_BEACH)) {
            return "cold";
        }
        if (biome.is(Biomes.WARM_OCEAN) || biome.is(Biomes.LUKEWARM_OCEAN) || biome.is(BiomeTags.IS_BEACH)) {
            return "warm";
        }
        if (biome.is(Biomes.DEEP_OCEAN) || biome.is(Biomes.DEEP_COLD_OCEAN) || biome.is(Biomes.DEEP_LUKEWARM_OCEAN)) {
            return "deep";
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
