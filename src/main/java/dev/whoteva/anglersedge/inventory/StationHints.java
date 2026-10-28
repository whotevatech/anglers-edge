package dev.whoteva.anglersedge.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** One status line for the upgrade station, shared by the screen so the copy can't drift from the recipes. */
public final class StationHints {

    private StationHints() {
    }

    public static Component line(ItemStack left, ItemStack right) {
        if (left.isEmpty() && right.isEmpty()) {
            return Component.translatable("gui.anglersedge.upgrade_empty");
        }
        Component upgrade = UpgradePath.hint(left, right);
        if (upgrade != null) {
            return upgrade;
        }
        Component bait = BaitPath.hint(left, right);
        if (bait != null) {
            return bait;
        }
        return Component.translatable("gui.anglersedge.upgrade_nomatch");
    }

    public static int color(ItemStack left, ItemStack right) {
        if (!UpgradePath.preview(left, right).isEmpty() || !BaitPath.preview(left, right).isEmpty()) {
            return 0xFF215C45;
        }
        if (left.isEmpty() && right.isEmpty()) {
            return 0xFF5C5C5C;
        }
        return 0xFF6E4A32;
    }
}
