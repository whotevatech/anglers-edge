package dev.whoteva.anglersedge.client;

import dev.whoteva.anglersedge.AnglersEdge;
import dev.whoteva.anglersedge.advancement.AnglersCriteria;
import dev.whoteva.anglersedge.item.FishingJournalItem;
import dev.whoteva.anglersedge.util.FishCatalog;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Full species checklist. Uncaught rows stay visible so the journal is a goal, not a blank book,
 * and the four milestones along the bottom match the advancements the catch handler grants.
 */
public class FishingJournalScreen extends Screen {

    public static final int WIDTH = 280;
    public static final int HEIGHT = 210;

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AnglersEdge.MOD_ID, "textures/gui/journal.png");

    private static final String[] MILESTONES = {
            AnglersCriteria.FIRST_CATCH,
            AnglersCriteria.SPECIES_5,
            AnglersCriteria.SPECIES_10,
            AnglersCriteria.JOURNAL_COMPLETE
    };

    private final ItemStack journal;

    public FishingJournalScreen(ItemStack journal) {
        super(Component.translatable("item.anglersedge.fishing_journal"));
        this.journal = journal;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;

        graphics.drawString(font, title, x + 8, y + 8, TackleGui.LABEL, false);

        Map<String, Integer> counts = FishingJournalItem.counts(journal);
        int discovered = FishingJournalItem.discovered(journal);
        int totalCatches = 0;
        for (FishCatalog.Species species : FishCatalog.all()) {
            totalCatches += counts.getOrDefault(species.id(), 0);
        }
        Component summary = Component.translatable("gui.anglersedge.journal_summary", discovered, FishCatalog.size(), totalCatches);
        graphics.drawString(font, summary, x + 8, y + 20, TackleGui.HINT, false);

        List<FishCatalog.Species> species = FishCatalog.all();
        int rows = (species.size() + 1) / 2;
        Component hoveredTip = null;
        for (int i = 0; i < species.size(); i++) {
            FishCatalog.Species entry = species.get(i);
            int col = i / rows;
            int row = i % rows;
            int rowX = x + 8 + col * 136;
            int rowY = y + 36 + row * 18;
            ItemStack icon = new ItemStack(entry.item());
            int count = counts.getOrDefault(entry.id(), 0);
            graphics.renderItem(icon, rowX, rowY);
            if (count <= 0) {
                graphics.fill(rowX, rowY, rowX + 16, rowY + 16, 0x88666A68);
            }
            Component name = Component.translatable(entry.item().getDescriptionId());
            String trimmed = font.plainSubstrByWidth(name.getString(), 78);
            int nameColor = count > 0 ? TackleGui.LABEL : 0xFF7A7A7A;
            graphics.drawString(font, trimmed, rowX + 18, rowY + 4, nameColor, false);
            String countText = count > 0 ? "x" + count : "—";
            graphics.drawString(font, countText, rowX + 100, rowY + 4, count > 0 ? entry.rarity().color().getColor() : 0xFF8A8A8A, false);

            if (mouseX >= rowX && mouseX < rowX + 128 && mouseY >= rowY && mouseY < rowY + 16) {
                hoveredTip = Component.translatable("tooltip.anglersedge.journal_row",
                        name, entry.rarity().tag(), count);
            }
        }

        int boxY = y + 168;
        for (int i = 0; i < MILESTONES.length; i++) {
            int boxX = x + 8 + i * 68;
            boolean done = milestoneMet(MILESTONES[i], discovered);
            int border = done ? 0xFFC4A15A : 0xFF6A6A6A;
            int fill = done ? 0xFF2C4A44 : 0xFFB7B7B7;
            graphics.fill(boxX, boxY, boxX + 64, boxY + 28, border);
            graphics.fill(boxX + 1, boxY + 1, boxX + 63, boxY + 27, fill);
            Component label = Component.translatable("gui.anglersedge.milestone." + MILESTONES[i]);
            int textWidth = font.width(label);
            int textColor = done ? 0xFFF2E2B8 : TackleGui.LABEL;
            graphics.drawString(font, label, boxX + (64 - textWidth) / 2, boxY + 10, textColor, false);
            if (mouseX >= boxX && mouseX < boxX + 64 && mouseY >= boxY && mouseY < boxY + 28) {
                hoveredTip = Component.translatable("gui.anglersedge.milestone." + MILESTONES[i] + ".tip");
            }
        }

        if (hoveredTip != null) {
            graphics.renderTooltip(font, hoveredTip, mouseX, mouseY);
        }
    }

    private static boolean milestoneMet(String milestone, int discovered) {
        return switch (milestone) {
            case AnglersCriteria.FIRST_CATCH -> discovered >= 1;
            case AnglersCriteria.SPECIES_5 -> discovered >= 5;
            case AnglersCriteria.SPECIES_10 -> discovered >= 10;
            case AnglersCriteria.JOURNAL_COMPLETE -> discovered >= FishCatalog.size();
            default -> false;
        };
    }
}
