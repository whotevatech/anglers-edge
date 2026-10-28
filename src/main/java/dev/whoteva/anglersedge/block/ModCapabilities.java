package dev.whoteva.anglersedge.block;

import dev.whoteva.anglersedge.block.entity.BreedingTankBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

/**
 * Exposes both blocks' inventories as {@code IItemHandler}s, so pipes/hoppers from other mods
 * (not just vanilla hoppers, which read {@code Container} directly) can pull from them too.
 */
public final class ModCapabilities {

    private ModCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.AUTO_FISHER.get(),
                (blockEntity, side) -> new InvWrapper(blockEntity));

        // Inputs only. The result slot is a preview; extracting it through a pipe would skip the craft cost.
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.UPGRADE_STATION.get(),
                (blockEntity, side) -> new RangedWrapper(new InvWrapper(blockEntity), 0, 2));

        // Parents stay in the tank. Pipes can pull offspring only.
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.BREEDING_TANK.get(),
                (blockEntity, side) -> new RangedWrapper(new InvWrapper(blockEntity), 2, BreedingTankBlockEntity.SLOTS));
    }
}
