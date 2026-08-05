package dev.whoteva.anglersedge.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import static dev.whoteva.anglersedge.AnglersEdge.MOD_ID;

public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);

    public static final DeferredBlock<UpgradeStationBlock> UPGRADE_STATION = BLOCKS.register(
            "upgrade_station",
            () -> new UpgradeStationBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(3.5F)
                    .sound(SoundType.COPPER)
                    .noOcclusion()));

    public static final DeferredBlock<AutoFisherBlock> AUTO_FISHER = BLOCKS.register(
            "auto_fisher",
            () -> new AutoFisherBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredBlock<FishTrophyBlock> FISH_TROPHY = BLOCKS.register(
            "fish_trophy",
            () -> new FishTrophyBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredBlock<BreedingTankBlock> BREEDING_TANK = BLOCKS.register(
            "breeding_tank",
            () -> new BreedingTankBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(2.5F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()));

    private ModBlocks() {
    }
}
