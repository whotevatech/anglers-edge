package dev.whoteva.anglersedge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/** Client-only opener so the journal item can stay in common code. */
public final class JournalClient {

    private JournalClient() {
    }

    public static void open(ItemStack journal) {
        Minecraft.getInstance().setScreen(new FishingJournalScreen(journal));
    }
}
