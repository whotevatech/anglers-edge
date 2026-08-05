package dev.whoteva.anglersedge.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;

/**
 * A fishing rod whose tier is baked in as flat luck/lure bonuses, rather than read from
 * enchantments on the stack.
 *
 * <p>Vanilla's {@link FishingHook} takes its luck and lure-speed bonus as constructor arguments
 * ({@code luck}, {@code lureSpeed} - both plain {@code int}s, confirmed against the real 1.21.1
 * class), so a higher rod tier is just a bigger pair of numbers passed to a normal hook. Nothing
 * about the hook, the loot table, or the bite mechanics needs touching.
 *
 * <p>Reeling in ({@code player.fishing != null}) is left to vanilla via {@code super.use(...)} -
 * the interesting behaviour here is only in casting a new line.
 */
public class TieredFishingRodItem extends FishingRodItem {

    private final int luckBonus;
    private final int lureBonus;
    private final float bonusRollChance;

    public TieredFishingRodItem(int luckBonus, int lureBonus, Properties properties) {
        this(luckBonus, lureBonus, 0f, properties);
    }

    /**
     * @param bonusRollChance chance, on every successful catch, that a duplicate of one of the
     *                        rolled drops is added on top - a per-rod-tier special ability rather
     *                        than a bait effect, so it stacks with whatever bait is in the
     *                        off-hand. 0 for the lower tiers; only the Leviathan Rod uses this.
     */
    public TieredFishingRodItem(int luckBonus, int lureBonus, float bonusRollChance, Properties properties) {
        super(properties);
        this.luckBonus = luckBonus;
        this.lureBonus = lureBonus;
        this.bonusRollChance = bonusRollChance;
    }

    public int luckBonus() {
        return luckBonus;
    }

    public int lureBonus() {
        return lureBonus;
    }

    public float bonusRollChance() {
        return bonusRollChance;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.fishing != null) {
            // Reeling in an existing line. Vanilla's own logic (retrieve(), damage, sound,
            // stats) is tier-agnostic and correct as-is.
            return super.use(level, player, hand);
        }

        if (!level.isClientSide) {
            // Rain nudges the odds of a treasure roll (luck), night nudges the bite speed
            // (lure) - both are just +1 to the constructor args vanilla already exposes, so
            // this needed no hook/mixin work at all.
            int effectiveLuck = luckBonus + (level.isRaining() ? 1 : 0);
            int effectiveLure = lureBonus + (level.isNight() ? 1 : 0);
            FishingHook hook = new FishingHook(player, level, effectiveLuck, effectiveLure);
            level.addFreshEntity(hook);
        }

        level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.FISHING_BOBBER_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        player.gameEvent(GameEvent.ITEM_INTERACT_START);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.anglersedge.rod_stats", luckBonus, lureBonus)
                .withStyle(ChatFormatting.GRAY));
        if (bonusRollChance > 0f) {
            tooltip.add(Component.translatable("tooltip.anglersedge.rod_bonus_roll", Math.round(bonusRollChance * 100))
                    .withStyle(ChatFormatting.DARK_AQUA));
        }
    }
}
