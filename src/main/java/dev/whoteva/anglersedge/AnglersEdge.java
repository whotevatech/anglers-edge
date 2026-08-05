package dev.whoteva.anglersedge;

import dev.whoteva.anglersedge.block.ModBlockEntities;
import dev.whoteva.anglersedge.block.ModBlocks;
import dev.whoteva.anglersedge.block.ModCapabilities;
import dev.whoteva.anglersedge.client.ClientSetup;
import dev.whoteva.anglersedge.event.BaitParticleHandler;
import dev.whoteva.anglersedge.event.FishingEventHandler;
import dev.whoteva.anglersedge.event.LeviathanEncounterHandler;
import dev.whoteva.anglersedge.event.ReefSharkHandler;
import dev.whoteva.anglersedge.event.WhaleSightingHandler;
import dev.whoteva.anglersedge.inventory.ModMenus;
import dev.whoteva.anglersedge.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Angler's Edge: rod tiers, bait, an upgrade station and an auto-fisher.
 *
 * <p>Registration follows the standard NeoForge pattern - every {@link DeferredRegister} lives
 * in its own class and is wired to the mod event bus here, in the constructor, before any
 * registry actually freezes.
 */
@Mod(AnglersEdge.MOD_ID)
public class AnglersEdge {

    public static final String MOD_ID = "anglersedge";

    public AnglersEdge(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);

        modEventBus.addListener(ModCreativeTabs::buildContents);
        modEventBus.addListener(ModCapabilities::register);
        modEventBus.addListener(ClientSetup::registerScreens);
        modEventBus.addListener(ClientSetup::registerRenderers);

        // Game-logic (non-registration) events go on the main NeoForge bus, not the mod bus.
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(FishingEventHandler.class);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(BaitParticleHandler.class);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(LeviathanEncounterHandler.class);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(ReefSharkHandler.class);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(WhaleSightingHandler.class);
    }
}
