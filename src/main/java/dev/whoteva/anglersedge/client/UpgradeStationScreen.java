package dev.whoteva.anglersedge.client;

import dev.whoteva.anglersedge.AnglersEdge;
import dev.whoteva.anglersedge.inventory.UpgradeStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * A plain re-skinned inventory background - three extra slots up top (rod, material, result)
 * over the usual player inventory grid. No custom widgets; the "craft" step happens implicitly
 * whenever both input slots are filled (see {@link UpgradeStationMenu#broadcastChanges()}).
 */
public class UpgradeStationScreen extends AbstractContainerScreen<UpgradeStationMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AnglersEdge.MOD_ID, "textures/gui/upgrade_station.png");

    public UpgradeStationScreen(UpgradeStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 176, 166);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
