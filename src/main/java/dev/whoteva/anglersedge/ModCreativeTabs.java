package dev.whoteva.anglersedge;

import dev.whoteva.anglersedge.block.ModBlocks;
import dev.whoteva.anglersedge.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static dev.whoteva.anglersedge.AnglersEdge.MOD_ID;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ANGLERS_EDGE = TABS.register(
            "anglers_edge",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.anglersedge"))
                    .icon(() -> new ItemStack(ModItems.LEVIATHAN_ROD.get()))
                    .build());

    private ModCreativeTabs() {
    }

    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ANGLERS_EDGE.getKey()) {
            event.accept(ModItems.STURDY_ROD.get());
            event.accept(ModItems.ANGLERS_ROD.get());
            event.accept(ModItems.LEVIATHAN_ROD.get());
            event.accept(ModItems.BASIC_BAIT.get());
            event.accept(ModItems.SHINY_LURE.get());
            event.accept(ModItems.FRENZY_BAIT.get());
            event.accept(ModItems.GHOST_BAIT.get());
            event.accept(ModItems.GLIMMERFIN.get());
            event.accept(ModItems.SWAMP_EEL.get());
            event.accept(ModItems.ABYSSAL_RAY.get());
            event.accept(ModItems.LEVIATHAN_SCALE.get());
            event.accept(ModItems.REEF_CRAB.get());
            event.accept(ModItems.ANGLERFISH.get());
            event.accept(ModItems.SHARK_TOOTH.get());
            event.accept(ModItems.FISHING_JOURNAL.get());
            event.accept(ModItems.REEL_UPGRADE.get());
            event.accept(ModItems.NET.get());
            event.accept(ModBlocks.UPGRADE_STATION.get());
            event.accept(ModBlocks.AUTO_FISHER.get());
            event.accept(ModBlocks.FISH_TROPHY.get());
            event.accept(ModBlocks.BREEDING_TANK.get());
        }
    }
}
