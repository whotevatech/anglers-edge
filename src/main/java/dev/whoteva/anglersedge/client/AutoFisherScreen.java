package dev.whoteva.anglersedge.client;

import dev.whoteva.anglersedge.AnglersEdge;
import dev.whoteva.anglersedge.inventory.AutoFisherMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Catch grid, filter, reel, and bait. Status text is driven by the menu's synced data slots,
 * which the block entity updates as it ticks, so the countdown is real and not a guess.
 */
public class AutoFisherScreen extends AbstractContainerScreen<AutoFisherMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AnglersEdge.MOD_ID, "textures/gui/auto_fisher.png");

    private static final int WATER = 1;
    private static final int POWERED = 1 << 1;
    private static final int STALLED = 1 << 2;
    private static final int FILTER = 1 << 3;
    private static final int REEL = 1 << 4;
    private static final int BAIT = 1 << 5;

    public AutoFisherScreen(AutoFisherMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = AutoFisherMenu.IMAGE_WIDTH;
        this.imageHeight = AutoFisherMenu.IMAGE_HEIGHT;
        this.inventoryLabelY = AutoFisherMenu.INV_Y - 12;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        TackleGui.drawSlot(graphics, x + AutoFisherMenu.FILTER_X, y + AutoFisherMenu.FILTER_Y);
        TackleGui.drawSlot(graphics, x + AutoFisherMenu.REEL_X, y + AutoFisherMenu.REEL_Y);
        TackleGui.drawSlot(graphics, x + AutoFisherMenu.BAIT_X, y + AutoFisherMenu.BAIT_Y);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                TackleGui.drawSlot(graphics, x + AutoFisherMenu.CATCH_X + col * 18, y + AutoFisherMenu.CATCH_Y + row * 18);
            }
        }
        TackleGui.drawPlayerSlots(graphics, x, y + AutoFisherMenu.INV_Y, y + AutoFisherMenu.HOTBAR_Y);

        if (!menu.getSlot(AutoFisherMenu.SLOT_FILTER).hasItem()) {
            TackleGui.drawGhostFilter(graphics, x + AutoFisherMenu.FILTER_X, y + AutoFisherMenu.FILTER_Y);
        }
        if (!menu.getSlot(AutoFisherMenu.SLOT_REEL).hasItem()) {
            TackleGui.drawGhostReel(graphics, x + AutoFisherMenu.REEL_X, y + AutoFisherMenu.REEL_Y);
        }
        if (!menu.getSlot(AutoFisherMenu.SLOT_BAIT).hasItem()) {
            TackleGui.drawGhostBait(graphics, x + AutoFisherMenu.BAIT_X, y + AutoFisherMenu.BAIT_Y);
        }

        int flags = menu.synced(2);
        int cooldown = menu.synced(0);
        int interval = Math.max(1, menu.synced(1));
        boolean water = (flags & WATER) != 0;
        boolean stalled = (flags & STALLED) != 0;
        float progress = (!water || stalled) ? 0f : 1f - (cooldown / (float) interval);
        int fill = (flags & POWERED) != 0 ? 0xFFC4A15A : 0xFF1F6F78;
        TackleGui.drawBar(graphics, x + AutoFisherMenu.CATCH_X, y + AutoFisherMenu.CATCH_Y + 56, 54, 6, progress, fill);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, Component.translatable("gui.anglersedge.slot_filter"), 28, AutoFisherMenu.FILTER_Y + 5, TackleGui.LABEL, false);
        graphics.drawString(font, Component.translatable("gui.anglersedge.slot_reel"), 28, AutoFisherMenu.REEL_Y + 5, TackleGui.LABEL, false);
        graphics.drawString(font, Component.translatable("gui.anglersedge.slot_bait"), 28, AutoFisherMenu.BAIT_Y + 5, TackleGui.LABEL, false);
        graphics.drawString(font, Component.translatable("gui.anglersedge.catch"), AutoFisherMenu.CATCH_X, 6, TackleGui.HINT, false);

        int flags = menu.synced(2);
        Component status = statusText(flags);
        int statusColor = ((flags & WATER) == 0 || (flags & STALLED) != 0) ? TackleGui.WARN : TackleGui.GOOD;
        graphics.drawString(font, status, 8, AutoFisherMenu.STATUS_Y, statusColor, false);
        graphics.drawString(font, flagsText(flags), 8, AutoFisherMenu.FLAGS_Y, TackleGui.HINT, false);
    }

    private Component statusText(int flags) {
        if ((flags & WATER) == 0) {
            return Component.translatable("gui.anglersedge.needs_water");
        }
        if ((flags & STALLED) != 0) {
            return Component.translatable("gui.anglersedge.bin_full");
        }
        int seconds = Math.max(0, (menu.synced(0) + 19) / 20);
        String key = (flags & POWERED) != 0 ? "gui.anglersedge.next_catch_powered" : "gui.anglersedge.next_catch";
        return Component.translatable(key, seconds);
    }

    private Component flagsText(int flags) {
        boolean filter = (flags & FILTER) != 0;
        boolean reel = (flags & REEL) != 0;
        boolean bait = (flags & BAIT) != 0;
        if (!filter && !reel && !bait) {
            return Component.translatable("gui.anglersedge.no_upgrades");
        }
        MutableComponent line = Component.empty();
        appendFlag(line, filter, "gui.anglersedge.flag_filter");
        appendFlag(line, reel, "gui.anglersedge.flag_reel");
        appendFlag(line, bait, "gui.anglersedge.flag_bait");
        return line;
    }

    private static void appendFlag(MutableComponent line, boolean on, String key) {
        if (!on) {
            return;
        }
        if (!line.getString().isEmpty()) {
            line.append(Component.literal(" · "));
        }
        line.append(Component.translatable(key));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot != null && !hoveredSlot.hasItem()) {
            Component tip = switch (hoveredSlot.index) {
                case AutoFisherMenu.SLOT_FILTER -> Component.translatable("tooltip.anglersedge.slot_filter");
                case AutoFisherMenu.SLOT_REEL -> Component.translatable("tooltip.anglersedge.slot_reel");
                case AutoFisherMenu.SLOT_BAIT -> Component.translatable("tooltip.anglersedge.slot_bait");
                default -> null;
            };
            if (tip != null) {
                graphics.renderTooltip(font, tip, mouseX, mouseY);
            }
        }
    }
}
