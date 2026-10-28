package dev.whoteva.anglersedge.event;

import net.minecraft.world.entity.projectile.FishingHook;

/**
 * One encounter per catch. Leviathan is registered first, then the shark, then the whale,
 * and each calls {@link #tryClaim} only when it actually fires. {@link #reset()} runs at the
 * start of the leviathan handler so a previous catch cannot block the next one.
 */
public final class EncounterGate {

    private static final ThreadLocal<Boolean> CLAIMED = ThreadLocal.withInitial(() -> false);

    private EncounterGate() {
    }

    public static void reset() {
        CLAIMED.set(false);
    }

    public static boolean tryClaim(FishingHook hook) {
        if (hook == null || CLAIMED.get()) {
            return false;
        }
        CLAIMED.set(true);
        return true;
    }
}
