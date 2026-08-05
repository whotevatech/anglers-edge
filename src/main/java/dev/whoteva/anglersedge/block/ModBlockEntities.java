package dev.whoteva.anglersedge.block;

import dev.whoteva.anglersedge.block.entity.AutoFisherBlockEntity;
import dev.whoteva.anglersedge.block.entity.BreedingTankBlockEntity;
import dev.whoteva.anglersedge.block.entity.FishTrophyBlockEntity;
import dev.whoteva.anglersedge.block.entity.UpgradeStationBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static dev.whoteva.anglersedge.AnglersEdge.MOD_ID;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UpgradeStationBlockEntity>> UPGRADE_STATION =
            BLOCK_ENTITIES.register("upgrade_station", () -> BlockEntityType.Builder.of(
                    UpgradeStationBlockEntity::new, ModBlocks.UPGRADE_STATION.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoFisherBlockEntity>> AUTO_FISHER =
            BLOCK_ENTITIES.register("auto_fisher", () -> BlockEntityType.Builder.of(
                    AutoFisherBlockEntity::new, ModBlocks.AUTO_FISHER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FishTrophyBlockEntity>> FISH_TROPHY =
            BLOCK_ENTITIES.register("fish_trophy", () -> BlockEntityType.Builder.of(
                    FishTrophyBlockEntity::new, ModBlocks.FISH_TROPHY.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BreedingTankBlockEntity>> BREEDING_TANK =
            BLOCK_ENTITIES.register("breeding_tank", () -> BlockEntityType.Builder.of(
                    BreedingTankBlockEntity::new, ModBlocks.BREEDING_TANK.get()).build(null));

    private ModBlockEntities() {
    }
}
