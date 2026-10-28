package dev.whoteva.anglersedge.event;

import dev.whoteva.anglersedge.advancement.AnglersCriteria;
import dev.whoteva.anglersedge.config.AnglersConfig;
import dev.whoteva.anglersedge.util.BiomeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerPlayer;
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

    private WhaleSightingHandler() {
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        Player player = event.getEntity();
        if (player.getRandom().nextFloat() >= AnglersConfig.WHALE_CHANCE.get()) {
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        FishingHook hook = event.getHookEntity();
        String category = BiomeCategory.at(serverLevel, hook.blockPosition());
        if (!category.equals("ocean") && !category.equals("warm") && !category.equals("cold") && !category.equals("deep")) {
            return;
        }
        if (!EncounterGate.tryClaim(hook)) {
            return;
        }

        RandomSource random = serverLevel.getRandom();
        double angle = random.nextDouble() * Math.PI * 2;
        double distance = 20 + random.nextDouble() * 25;
        double x = hook.getX() + Math.cos(angle) * distance;
        double z = hook.getZ() + Math.sin(angle) * distance;
        double y = serverLevel.getSeaLevel();

        WaterTelegraph.burst(serverLevel, x, y + 0.2, z, true);
        WaterTelegraph.burst(serverLevel, hook.getX(), hook.getY(), hook.getZ(), false);
        serverLevel.playSound(null, BlockPos.containing(x, y, z), SoundEvents.ELDER_GUARDIAN_AMBIENT,
                SoundSource.AMBIENT, 2.0F, 0.5F);
        serverLevel.playSound(null, hook.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.AMBIENT, 0.6F, 0.5F);

        Component message = Component.translatable("message.anglersedge.whale")
                .withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
        player.displayClientMessage(message, true);
        player.sendSystemMessage(message);
        if (player instanceof ServerPlayer serverPlayer) {
            AnglersCriteria.trigger(serverPlayer, AnglersCriteria.WHALE_SIGHTED);
        }
    }
}
