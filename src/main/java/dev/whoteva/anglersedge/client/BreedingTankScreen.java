package dev.whoteva.anglersedge.client;

import dev.whoteva.anglersedge.AnglersEdge;
import dev.whoteva.anglersedge.block.entity.BreedingTankBlockEntity;
import dev.whoteva.anglersedge.inventory.BreedingTankMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Pair slots, a progress arrow, and a take-only output. The bar and status come from data
 * slots synced by the tank, including the "output full" stall.
 */
public class BreedingTankScreen extends AbstractContainerScreen<BreedingTankMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AnglersEdge.MOD_ID, "textures/gui/breeding_tank.png");

    public BreedingTankScreen(BreedingTankMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = BreedingTankMenu.IMAGE_WIDTH;
        this.imageHeight = BreedingTankMenu.IMAGE_HEIGHT;
        this.inventoryLabelY = BreedingTankMenu.INV_Y - 12;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        TackleGui.drawParentSlot(graphics, x + BreedingTankMenu.PARENT_A_X, y + BreedingTankMenu.PARENT_A_Y);
        TackleGui.drawParentSlot(graphics, x + BreedingTankMenu.PARENT_B_X, y + BreedingTankMenu.PARENT_B_Y);
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                TackleGui.drawSlot(graphics, x + BreedingTankMenu.OUTPUT_X + col * 18, y + BreedingTankMenu.OUTPUT_Y + row * 18);
            }
        }
        TackleGui.drawPlayerSlots(graphics, x, y + BreedingTankMenu.INV_Y, y + BreedingTankMenu.HOTBAR_Y);

        if (!menu.getSlot(0).hasItem()) {
            TackleGui.drawGhostFish(graphics, x + BreedingTankMenu.PARENT_A_X, y + BreedingTankMenu.PARENT_A_Y);
        }
        if (!menu.getSlot(1).hasItem()) {
            TackleGui.drawGhostFish(graphics, x + BreedingTankMenu.PARENT_B_X, y + BreedingTankMenu.PARENT_B_Y);
        }

        int progress = menu.synced(0);
        int required = Math.max(1, menu.synced(1));
        int status = menu.synced(2);
        float pct = status == BreedingTankBlockEntity.STATUS_BREEDING || status == BreedingTankBlockEntity.STATUS_FULL
                ? progress / (float) required
                : 0f;
        TackleGui.drawArrow(graphics, x + 77, y + BreedingTankMenu.PARENT_A_Y + 4, pct);
        int fill = status == BreedingTankBlockEntity.STATUS_FULL ? 0xFF8E3B2E : 0xFF1F6F78;
        TackleGui.drawBar(graphics, x + 8, y + BreedingTankMenu.BAR_Y, imageWidth - 16, 6, pct, fill);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        int status = menu.synced(2);
        Component text = switch (status) {
            case BreedingTankBlockEntity.STATUS_NO_WATER -> Component.translatable("gui.anglersedge.needs_water");
            case BreedingTankBlockEntity.STATUS_MISMATCH -> Component.translatable("gui.anglersedge.pair_mismatch");
            case BreedingTankBlockEntity.STATUS_BREEDING -> Component.translatable("gui.anglersedge.breeding");
            case BreedingTankBlockEntity.STATUS_FULL -> Component.translatable("gui.anglersedge.output_full");
            default -> Component.translatable("gui.anglersedge.pair_empty");
        };
        int color = (status == BreedingTankBlockEntity.STATUS_NO_WATER
                || status == BreedingTankBlockEntity.STATUS_MISMATCH
                || status == BreedingTankBlockEntity.STATUS_FULL) ? TackleGui.WARN : TackleGui.LABEL;
        if (status == BreedingTankBlockEntity.STATUS_BREEDING) {
            color = TackleGui.GOOD;
        }
        graphics.drawString(font, text, 8, BreedingTankMenu.STATUS_Y, color, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot != null && !hoveredSlot.hasItem() && hoveredSlot.index <= 1) {
            graphics.renderTooltip(font, Component.translatable("tooltip.anglersedge.slot_parent"), mouseX, mouseY);
        }
    }
}
