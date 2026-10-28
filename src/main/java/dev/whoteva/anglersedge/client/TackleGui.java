package dev.whoteva.anglersedge.client;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Shared slot, arrow, and bar drawing for every Angler's Edge screen. Slot squares use the
 * same 18×18 footprint as {@link net.minecraft.world.inventory.Slot}, with vanilla-style
 * light and dark rims so they read as slots instead of flat gray boxes.
 */
public final class TackleGui {

    public static final int LABEL = 0xFF404040;
    public static final int HINT = 0xFF5C5C5C;
    public static final int WARN = 0xFF8E3B2E;
    public static final int GOOD = 0xFF215C45;
    public static final int GHOST = 0xFF6A7A78;

    private static final int SLOT_INNER = 0xFF8B8B8B;
    private static final int PARENT_INNER = 0xFF7E9A86;
    private static final int RESULT_INNER = 0xFF8B8B8B;

    private TackleGui() {
    }

    public static void drawSlot(GuiGraphics graphics, int x, int y) {
        drawSlot(graphics, x, y, SLOT_INNER);
    }

    public static void drawParentSlot(GuiGraphics graphics, int x, int y) {
        drawSlot(graphics, x, y, PARENT_INNER);
    }

    public static void drawResultSlot(GuiGraphics graphics, int x, int y) {
        drawSlot(graphics, x, y, RESULT_INNER);
    }

    public static void drawSlot(GuiGraphics graphics, int x, int y, int inner) {
        graphics.fill(x, y, x + 18, y + 18, 0xFFFFFFFF);
        graphics.fill(x, y, x + 17, y + 17, 0xFF373737);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, inner);
        graphics.fill(x + 1, y + 1, x + 16, y + 2, 0xFF5A5A5A);
        graphics.fill(x + 1, y + 1, x + 2, y + 16, 0xFF5A5A5A);
        graphics.fill(x + 2, y + 16, x + 17, y + 17, 0xFFB4B4B4);
        graphics.fill(x + 16, y + 2, x + 17, y + 17, 0xFFB4B4B4);
    }

    public static void drawPlayerSlots(GuiGraphics graphics, int left, int invY, int hotbarY) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(graphics, left + 8 + col * 18, invY + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlot(graphics, left + 8 + col * 18, hotbarY);
        }
    }

    public static void drawBar(GuiGraphics graphics, int x, int y, int width, int height, float progress, int fill) {
        graphics.fill(x, y, x + width, y + height, 0xFF373737);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF5A5A5A);
        int inner = Math.max(0, width - 2);
        int filled = Math.round(inner * Math.max(0f, Math.min(1f, progress)));
        if (filled > 0) {
            graphics.fill(x + 1, y + 1, x + 1 + filled, y + height - 1, fill);
        }
    }

    /** Furnace-style arrow. {@code progress} of 0 draws only the empty track; 1 fills it. */
    public static void drawArrow(GuiGraphics graphics, int x, int y, float progress) {
        drawArrowShape(graphics, x, y, 0xFF7A7A7A);
        float clamped = Math.max(0f, Math.min(1f, progress));
        if (clamped <= 0f) {
            return;
        }
        int filled = Math.max(1, Math.round(22 * clamped));
        graphics.enableScissor(x, y, x + filled, y + 16);
        drawArrowShape(graphics, x, y, 0xFF1F6F78);
        graphics.disableScissor();
    }

    private static void drawArrowShape(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x, y + 5, x + 14, y + 10, color);
        graphics.fill(x + 14, y + 3, x + 16, y + 12, color);
        graphics.fill(x + 16, y + 4, x + 18, y + 11, color);
        graphics.fill(x + 18, y + 5, x + 20, y + 10, color);
        graphics.fill(x + 20, y + 6, x + 22, y + 9, color);
    }

    public static void drawGhostRod(GuiGraphics graphics, int x, int y) {
        int color = GHOST;
        for (int i = 0; i < 8; i++) {
            graphics.fill(x + 4 + i, y + 12 - i, x + 6 + i, y + 14 - i, color);
        }
        graphics.fill(x + 5, y + 12, x + 9, y + 15, color);
    }

    public static void drawGhostPlus(GuiGraphics graphics, int x, int y) {
        int color = GHOST;
        graphics.fill(x + 8, y + 5, x + 10, y + 13, color);
        graphics.fill(x + 5, y + 8, x + 13, y + 10, color);
    }

    public static void drawGhostFish(GuiGraphics graphics, int x, int y) {
        int color = GHOST;
        graphics.fill(x + 5, y + 7, x + 12, y + 11, color);
        graphics.fill(x + 11, y + 6, x + 14, y + 8, color);
        graphics.fill(x + 11, y + 10, x + 14, y + 12, color);
        graphics.fill(x + 6, y + 8, x + 7, y + 9, 0xFF8FA09E);
    }

    public static void drawGhostFilter(GuiGraphics graphics, int x, int y) {
        int color = GHOST;
        graphics.fill(x + 4, y + 4, x + 14, y + 6, color);
        graphics.fill(x + 6, y + 6, x + 12, y + 9, color);
        graphics.fill(x + 8, y + 9, x + 10, y + 14, color);
    }

    public static void drawGhostReel(GuiGraphics graphics, int x, int y) {
        int color = GHOST;
        graphics.fill(x + 5, y + 5, x + 13, y + 7, color);
        graphics.fill(x + 5, y + 11, x + 13, y + 13, color);
        graphics.fill(x + 5, y + 5, x + 7, y + 13, color);
        graphics.fill(x + 11, y + 5, x + 13, y + 13, color);
        graphics.fill(x + 8, y + 8, x + 10, y + 10, color);
    }

    public static void drawGhostBait(GuiGraphics graphics, int x, int y) {
        int color = GHOST;
        graphics.fill(x + 8, y + 3, x + 10, y + 8, color);
        graphics.fill(x + 6, y + 8, x + 12, y + 13, color);
    }
}
