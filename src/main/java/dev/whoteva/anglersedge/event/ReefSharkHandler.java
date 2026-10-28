package dev.whoteva.anglersedge.event;

import dev.whoteva.anglersedge.advancement.AnglersCriteria;
import dev.whoteva.anglersedge.config.AnglersConfig;
import dev.whoteva.anglersedge.item.ModItems;
import dev.whoteva.anglersedge.util.BiomeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;

/**
 * A lighter-weight hazard than the Leviathan: fishing in ocean or river water has a moderate
 * chance of a Reef Shark (a renamed, regular {@link Guardian} rather than the elder variant)
 * showing up to contest the catch. Same reuse-a-vanilla-mob reasoning as
 * {@link LeviathanEncounterHandler} - see its javadoc - and the same in-memory UUID tracking for
 * the same reason (no {@code getPersistentData()} on the plain compile-time jar).
 */
public final class ReefSharkHandler {

    public static final String TAG = "anglersedge_reef_shark";
    private static final java.util.Set<java.util.UUID> SHARK_IDS = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private ReefSharkHandler() {
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        Player player = event.getEntity();
        if (player.getRandom().nextFloat() >= AnglersConfig.SHARK_CHANCE.get()) {
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        FishingHook hook = event.getHookEntity();
        String category = BiomeCategory.at(serverLevel, hook.blockPosition());
        if (!category.equals("ocean") && !category.equals("river") && !category.equals("warm")
                && !category.equals("cold") && !category.equals("deep")) {
            return;
        }
        if (!EncounterGate.tryClaim(hook)) {
            return;
        }

        Guardian shark = EntityType.GUARDIAN.create(serverLevel);
        if (shark == null) {
            return;
        }
        shark.moveTo(hook.getX(), hook.getY() - 1, hook.getZ(), 0F, 0F);
        shark.setCustomName(Component.translatable("entity.anglersedge.reef_shark").withStyle(ChatFormatting.GRAY));
        shark.setCustomNameVisible(true);
        shark.addTag(TAG);
        shark.setPersistenceRequired();
        shark.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 12, 0));
        SHARK_IDS.add(shark.getUUID());
        serverLevel.addFreshEntity(shark);

        WaterTelegraph.burst(serverLevel, hook.getX(), hook.getY(), hook.getZ(), false);
        player.displayClientMessage(Component.translatable("message.anglersedge.shark").withStyle(ChatFormatting.GRAY), true);
        serverLevel.playSound(null, hook.blockPosition(), SoundEvents.GUARDIAN_AMBIENT, SoundSource.HOSTILE, 1.0F, 1.4F);
        serverLevel.playSound(null, hook.blockPosition(), SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.NEUTRAL, 0.8F, 1.2F);
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!SHARK_IDS.remove(entity.getUUID()) && !entity.getTags().contains(TAG)) {
            return;
        }
        Vec3 pos = entity.position();
        int count = 1 + entity.level().getRandom().nextInt(2);
        ItemStack teeth = new ItemStack(ModItems.SHARK_TOOTH.get(), count);
        dev.whoteva.anglersedge.util.FishRarity.applyTagOnly(teeth);
        event.getDrops().add(new ItemEntity(entity.level(), pos.x, pos.y, pos.z, teeth));
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            AnglersCriteria.trigger(player, AnglersCriteria.SHARK_SLAIN);
            FishingEventHandler.recordCatch(player, teeth);
        }
    }
}
