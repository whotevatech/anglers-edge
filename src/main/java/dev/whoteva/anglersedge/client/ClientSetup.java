package dev.whoteva.anglersedge.client;

import dev.whoteva.anglersedge.block.ModBlockEntities;
import dev.whoteva.anglersedge.inventory.ModMenus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class ClientSetup {

    private ClientSetup() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.UPGRADE_STATION.get(), UpgradeStationScreen::new);
        event.register(ModMenus.AUTO_FISHER.get(), AutoFisherScreen::new);
        event.register(ModMenus.BREEDING_TANK.get(), BreedingTankScreen::new);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.FISH_TROPHY.get(), FishTrophyRenderer::new);
    }
}
