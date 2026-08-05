package dev.whoteva.anglersedge.inventory;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static dev.whoteva.anglersedge.AnglersEdge.MOD_ID;

public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<UpgradeStationMenu>> UPGRADE_STATION = MENUS.register(
            "upgrade_station",
            () -> IMenuTypeExtension.create((windowId, inv, buf) -> new UpgradeStationMenu(windowId, inv)));

    public static final DeferredHolder<MenuType<?>, MenuType<AutoFisherMenu>> AUTO_FISHER = MENUS.register(
            "auto_fisher",
            () -> IMenuTypeExtension.create((windowId, inv, buf) -> new AutoFisherMenu(windowId, inv)));

    public static final DeferredHolder<MenuType<?>, MenuType<BreedingTankMenu>> BREEDING_TANK = MENUS.register(
            "breeding_tank",
            () -> IMenuTypeExtension.create((windowId, inv, buf) -> new BreedingTankMenu(windowId, inv)));

    private ModMenus() {
    }
}
