package dev.whoteva.anglersedge.event;

import dev.whoteva.anglersedge.util.BiomeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;

/**
 * Pure atmosphere, no entity involved: vanilla has no large sea-mammal model to reuse the way
 * {@link LeviathanEncounterHandler} reuses the Elder Guardian, so rather than build (and get
 * subtly wrong) a custom whale model blind, this is just a distant splash, a low ambient call,
 * and a chat line - a "you just missed it" moment rather than something you fight or catch.
 */
public final class WhaleSightingHandler {

    private static final float SIGHTING_CHANCE = 0.05F;

    private WhaleSightingHandler() {
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        Player player = event.getEntity();
        if (player.getRandom().nextFloat() >= SIGHTING_CHANCE) {
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        FishingHook hook = event.getHookEntity();
        if (!BiomeCategory.at(serverLevel, hook.blockPosition()).equals("ocean")) {
            return;
        }

        RandomSource random = serverLevel.getRandom();
        double angle = random.nextDouble() * Math.PI * 2;
        double distance = 20 + random.nextDouble() * 25;
        double x = hook.getX() + Math.cos(angle) * distance;
        double z = hook.getZ() + Math.sin(angle) * distance;
        double y = serverLevel.getSeaLevel();

        serverLevel.sendParticles(ParticleTypes.SPLASH, x, y, z, 40, 1.8, 0.4, 1.8, 0.1);
        serverLevel.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, x, y, z, 20, 1.2, 0.6, 1.2, 0.05);
        serverLevel.playSound((Player) null, BlockPos.containing(x, y, z), SoundEvents.ELDER_GUARDIAN_AMBIENT,
                SoundSource.AMBIENT, 2.0F, 0.5F);

        player.sendSystemMessage(Component.literal("A massive shape breaches the surface in the distance...")
                .withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
    }
}
