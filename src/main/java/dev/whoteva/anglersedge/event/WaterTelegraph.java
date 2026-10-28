package dev.whoteva.anglersedge.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

/** Shared splash so the shark, leviathan, and whale read as the same water, not three different effects. */
public final class WaterTelegraph {

    private WaterTelegraph() {
    }

    public static void burst(ServerLevel level, double x, double y, double z, boolean heavy) {
        level.sendParticles(ParticleTypes.SPLASH, x, y, z, heavy ? 48 : 22, 0.7, 0.15, 0.7, 0.08);
        level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, x, y - 0.4, z, heavy ? 36 : 14, 0.4, 0.55, 0.4, 0.02);
        level.sendParticles(ParticleTypes.FISHING, x, y, z, heavy ? 16 : 6, 0.45, 0.1, 0.45, 0.01);
        if (heavy) {
            level.sendParticles(ParticleTypes.END_ROD, x, y + 0.4, z, 14, 0.55, 0.35, 0.55, 0.02);
            level.sendParticles(ParticleTypes.SQUID_INK, x, y, z, 8, 0.35, 0.15, 0.35, 0.01);
        }
    }
}
