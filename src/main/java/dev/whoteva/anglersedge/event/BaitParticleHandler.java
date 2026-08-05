package dev.whoteva.anglersedge.event;

import dev.whoteva.anglersedge.item.BaitItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Purely cosmetic: while a player's bobber is out and they're holding one of this mod's baits
 * in the off-hand, drift the matching {@link dev.whoteva.anglersedge.item.BaitEffect#particle()}
 * up from the bobber every few ticks. Gives a visual read on active bait without opening the
 * inventory. No gameplay effect - the actual catch-time logic still lives in
 * {@link FishingEventHandler}.
 */
public final class BaitParticleHandler {

    private static final int PARTICLE_INTERVAL = 8;

    private BaitParticleHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        FishingHook hook = player.fishing;
        if (hook == null || hook.isRemoved()) {
            return;
        }

        if (serverLevel.getGameTime() % PARTICLE_INTERVAL != 0) {
            return;
        }

        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!(offhand.getItem() instanceof BaitItem bait)) {
            return;
        }

        serverLevel.sendParticles(
                bait.effect().particle(),
                hook.getX(), hook.getY() + 0.2, hook.getZ(),
                2, 0.05, 0.05, 0.05, 0.01);
    }
}
