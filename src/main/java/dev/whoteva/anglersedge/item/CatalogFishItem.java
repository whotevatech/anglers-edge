package dev.whoteva.anglersedge.item;

import dev.whoteva.anglersedge.util.FishCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * A catalog species. Caught stacks already carry rarity and weight as lore, so the tooltip
 * only fills that in for a plain (uncaught / creative) stack.
 */
public class CatalogFishItem extends Item {

    public CatalogFishItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (stack.get(DataComponents.LORE) != null) {
            return;
        }
        FishCatalog.Species species = FishCatalog.byItem(this);
        if (species == null) {
            return;
        }
        tooltip.add(species.rarity().tag());
        tooltip.add(Component.translatable("tooltip.anglersedge.habitat." + species.habitat().id())
                .withStyle(ChatFormatting.GRAY));
    }
}
