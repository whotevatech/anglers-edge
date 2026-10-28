package dev.whoteva.anglersedge.item;

import dev.whoteva.anglersedge.config.AnglersConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * A consumable held in the off-hand while fishing, or loaded into an auto-fisher's bait slot.
 * {@link dev.whoteva.anglersedge.event.FishingEventHandler} checks the off-hand on every successful
 * catch; if it's one of these, the matching {@link BaitEffect} is applied and one bait is consumed.
 */
public class BaitItem extends Item {

    private final BaitEffect effect;

    public BaitItem(BaitEffect effect, Properties properties) {
        super(properties);
        this.effect = effect;
    }

    public BaitEffect effect() {
        return effect;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (effect == BaitEffect.BONUS_TREASURE_CHANCE) {
            int percent = Math.round(AnglersConfig.SHINY_LURE_CHANCE.get().floatValue() * 100f);
            tooltip.add(Component.translatable(effect.descriptionKey(), percent).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable(effect.descriptionKey()).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("tooltip.anglersedge.bait_hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
