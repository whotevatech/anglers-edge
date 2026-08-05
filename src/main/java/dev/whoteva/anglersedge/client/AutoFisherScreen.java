package dev.whoteva.anglersedge.client;

import dev.whoteva.anglersedge.inventory.AutoFisherMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Drawn entirely with {@link GuiGraphics#fill}, not a borrowed texture. The slot squares below
 * are computed from the exact same coordinates {@link AutoFisherMenu} uses to place its
 * {@code Slot}s, so the background art and the interactive slots can never drift out of sync
 * with each other - the failure mode a borrowed vanilla texture (dispenser.png, in an earlier
 * version of this file) turned out to have.
 */
public class AutoFisherScreen extends AbstractContainerScreen<AutoFisherMenu> {

    private static final int PANEL_BG = 0xFFC6C6C6;
    private static final int PANEL_BORDER = 0xFF373737;
    private static final int SLOT_BORDER = 0xFF373737;
    private static final int SLOT_BG = 0xFF8B8B8B;

    public AutoFisherScreen(AutoFisherMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.fill(x, y, x + imageWidth, y + imageHeight, PANEL_BG);
        graphics.fill(x, y, x + imageWidth, y + 1, PANEL_BORDER);
        graphics.fill(x, y + imageHeight - 1, x + imageWidth, y + imageHeight, PANEL_BORDER);
        graphics.fill(x, y, x + 1, y + imageHeight, PANEL_BORDER);
        graphics.fill(x + imageWidth - 1, y, x + imageWidth, y + imageHeight, PANEL_BORDER);

        // The auto-fisher's own 3x3 catch grid - same (62 + col*18, 17 + row*18) origin as
        // AutoFisherMenu's addSlot calls.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                drawSlot(graphics, x + 62 + col * 18, y + 17 + row * 18);
            }
        }

        // Filter (26, 17) and reel upgrade (26, 44) slots - same coordinates as AutoFisherMenu.
        drawSlot(graphics, x + 26, y + 17);
        drawSlot(graphics, x + 26, y + 44);

        // Player inventory - same (8 + col*18, 84 + row*18) / (8 + col*18, 142) origin as
        // AutoFisherMenu's player-inventory addSlot calls.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(graphics, x + 8 + col * 18, y + 84 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlot(graphics, x + 8 + col * 18, y + 142);
        }
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, SLOT_BORDER);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT_BG);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
