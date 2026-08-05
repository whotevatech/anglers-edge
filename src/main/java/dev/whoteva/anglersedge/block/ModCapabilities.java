package dev.whoteva.anglersedge.block;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

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

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.UPGRADE_STATION.get(),
                (blockEntity, side) -> new InvWrapper(blockEntity));
    }
}
