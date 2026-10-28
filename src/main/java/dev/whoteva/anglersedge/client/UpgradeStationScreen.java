package dev.whoteva.anglersedge.client;

import dev.whoteva.anglersedge.AnglersEdge;
import dev.whoteva.anglersedge.inventory.StationHints;
import dev.whoteva.anglersedge.inventory.UpgradeStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Rod, material, and result over the player inventory. The hint under the slots is computed
 * from the same recipe tables the menu uses, so a short stack says what it still needs.
 */
public class UpgradeStationScreen extends AbstractContainerScreen<UpgradeStationMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AnglersEdge.MOD_ID, "textures/gui/upgrade_station.png");

    public UpgradeStationScreen(UpgradeStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = UpgradeStationMenu.IMAGE_WIDTH;
        this.imageHeight = UpgradeStationMenu.IMAGE_HEIGHT;
        this.inventoryLabelY = UpgradeStationMenu.INV_Y - 12;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        TackleGui.drawSlot(graphics, x + UpgradeStationMenu.ROD_X, y + UpgradeStationMenu.ROD_Y);
        TackleGui.drawSlot(graphics, x + UpgradeStationMenu.MAT_X, y + UpgradeStationMenu.MAT_Y);
        TackleGui.drawResultSlot(graphics, x + UpgradeStationMenu.RESULT_X, y + UpgradeStationMenu.RESULT_Y);
        TackleGui.drawPlayerSlots(graphics, x, y + UpgradeStationMenu.INV_Y, y + UpgradeStationMenu.HOTBAR_Y);

        ItemStack rod = menu.getSlot(0).getItem();
        ItemStack material = menu.getSlot(1).getItem();
        ItemStack result = menu.getSlot(2).getItem();
        if (rod.isEmpty()) {
            TackleGui.drawGhostRod(graphics, x + UpgradeStationMenu.ROD_X, y + UpgradeStationMenu.ROD_Y);
        }
        if (material.isEmpty()) {
            TackleGui.drawGhostPlus(graphics, x + UpgradeStationMenu.MAT_X, y + UpgradeStationMenu.MAT_Y);
        }
        float progress = result.isEmpty() ? 0f : 1f;
        TackleGui.drawArrow(graphics, x + 96, y + UpgradeStationMenu.ROD_Y + 4, progress);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, Component.translatable("gui.anglersedge.slot_rod"), UpgradeStationMenu.ROD_X, 16, TackleGui.LABEL, false);
        graphics.drawString(font, Component.translatable("gui.anglersedge.slot_material"), UpgradeStationMenu.MAT_X - 6, 16, TackleGui.LABEL, false);
        graphics.drawString(font, Component.translatable("gui.anglersedge.slot_result"), UpgradeStationMenu.RESULT_X - 2, 16, TackleGui.LABEL, false);

        ItemStack left = menu.getSlot(0).getItem();
        ItemStack right = menu.getSlot(1).getItem();
        Component hint = StationHints.line(left, right);
        int color = StationHints.color(left, right);
        List<FormattedCharSequence> lines = font.split(hint, imageWidth - 16);
        int y = UpgradeStationMenu.HINT_Y;
        int shown = 0;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, 8, y, color, false);
            y += 10;
            shown++;
            if (shown >= 2) {
                break;
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot != null && !hoveredSlot.hasItem()) {
            Component tip = switch (hoveredSlot.index) {
                case 0 -> Component.translatable("tooltip.anglersedge.slot_rod");
                case 1 -> Component.translatable("tooltip.anglersedge.slot_material");
                case 2 -> Component.translatable("tooltip.anglersedge.slot_result");
                default -> null;
            };
            if (tip != null) {
                graphics.renderTooltip(font, tip, mouseX, mouseY);
            }
        }
    }
}
