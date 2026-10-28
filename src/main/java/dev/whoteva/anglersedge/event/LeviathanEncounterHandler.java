package dev.whoteva.anglersedge.event;

import dev.whoteva.anglersedge.advancement.AnglersCriteria;
import dev.whoteva.anglersedge.config.AnglersConfig;
import dev.whoteva.anglersedge.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.ElderGuardian;
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
 * The "boss fish": a rare, tougher-than-usual encounter rather than a from-scratch modeled and
 * animated mob. Reusing a vanilla {@link ElderGuardian} (renamed, tagged, given its own drop) is
 * a deliberate scoping call - hand-writing a custom entity model/renderer blind, with no way to
 * see how it actually looks in-game, is exactly the kind of thing that goes subtly wrong. This
 * still delivers the real feature (a legendary catch you have to fight) without that risk.
 *
 * <p>Which entities are "ours" is tracked by UUID in memory rather than via NeoForge's usual
 * {@code Entity#getPersistentData()} - that method is added by NeoForge's own classloading
 * transformer and isn't present on the plain deobfuscated client jar this project compiles
 * against for verification, so a real NBT tag can't be checked at build time. A live JVM set is
 * simpler anyway and the trade-off (the tag doesn't survive a server restart) is fine for a mob
 * meant to be fought shortly after it spawns.
 */
public final class LeviathanEncounterHandler {

    public static final String TAG = "anglersedge_leviathan";
    private static final java.util.Set<java.util.UUID> LEVIATHAN_IDS = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private LeviathanEncounterHandler() {
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        EncounterGate.reset();
        Player player = event.getEntity();
        if (player.getRandom().nextFloat() >= AnglersConfig.LEVIATHAN_CHANCE.get()) {
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        FishingHook hook = event.getHookEntity();
        if (!EncounterGate.tryClaim(hook)) {
            return;
        }

        ElderGuardian leviathan = EntityType.ELDER_GUARDIAN.create(serverLevel);
        if (leviathan == null) {
            return;
        }
        leviathan.moveTo(hook.getX(), hook.getY() - 1, hook.getZ(), 0F, 0F);
        leviathan.setCustomName(Component.translatable("entity.anglersedge.ancient_leviathan")
                .withStyle(ChatFormatting.DARK_AQUA));
        leviathan.setCustomNameVisible(true);
        leviathan.addTag(TAG);
        leviathan.setPersistenceRequired();
        leviathan.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 20, 0));
        LEVIATHAN_IDS.add(leviathan.getUUID());
        serverLevel.addFreshEntity(leviathan);

        WaterTelegraph.burst(serverLevel, hook.getX(), hook.getY(), hook.getZ(), true);
        Component message = Component.translatable("message.anglersedge.leviathan").withStyle(ChatFormatting.DARK_AQUA);
        player.displayClientMessage(message, true);
        player.sendSystemMessage(message);
        serverLevel.playSound(null, hook.blockPosition(), SoundEvents.ELDER_GUARDIAN_AMBIENT_LAND, SoundSource.HOSTILE, 1.0F, 0.7F);
        serverLevel.playSound(null, hook.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.NEUTRAL, 1.0F, 0.6F);
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!LEVIATHAN_IDS.remove(entity.getUUID()) && !entity.getTags().contains(TAG)) {
            return;
        }
        Vec3 pos = entity.position();
        int count = 1 + entity.level().getRandom().nextInt(3);
        ItemStack scales = new ItemStack(ModItems.LEVIATHAN_SCALE.get(), count);
        dev.whoteva.anglersedge.util.FishRarity.applyTagOnly(scales);
        event.getDrops().add(new ItemEntity(entity.level(), pos.x, pos.y, pos.z, scales));
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            AnglersCriteria.trigger(player, AnglersCriteria.LEVIATHAN_SLAIN);
            FishingEventHandler.recordCatch(player, scales);
        }
    }
}
