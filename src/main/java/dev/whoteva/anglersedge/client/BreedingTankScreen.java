package dev.whoteva.anglersedge.client;

import dev.whoteva.anglersedge.inventory.BreedingTankMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Drawn with {@link GuiGraphics#fill}, same approach as {@link AutoFisherScreen} (see its
 * javadoc for why): the slot squares are computed from the same coordinates
 * {@link BreedingTankMenu} places its {@code Slot}s at, so background art and interactive slots
 * can't drift apart.
 */
public class BreedingTankScreen extends AbstractContainerScreen<BreedingTankMenu> {

    private static final int PANEL_BG = 0xFFC6C6C6;
    private static final int PANEL_BORDER = 0xFF373737;
    private static final int SLOT_BORDER = 0xFF373737;
    private static final int SLOT_BG = 0xFF8B8B8B;
    private static final int PARENT_SLOT_BG = 0xFF6B8B6B;

    public BreedingTankScreen(BreedingTankMenu menu, Inventory playerInventory, Component title) {
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

        // Breeding-pair slots (tinted green to mark them as special) - same (62, 17) / (98, 17)
        // origin as BreedingTankMenu's addSlot calls.
        drawSlot(graphics, x + 62, y + 17, PARENT_SLOT_BG);
        drawSlot(graphics, x + 98, y + 17, PARENT_SLOT_BG);

        // Output grid - same (62 + col*18, 44 + row*18) origin as BreedingTankMenu.
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                drawSlot(graphics, x + 62 + col * 18, y + 44 + row * 18, SLOT_BG);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(graphics, x + 8 + col * 18, y + 84 + row * 18, SLOT_BG);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlot(graphics, x + 8 + col * 18, y + 142, SLOT_BG);
        }
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y, int background) {
        graphics.fill(x, y, x + 18, y + 18, SLOT_BORDER);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, background);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
