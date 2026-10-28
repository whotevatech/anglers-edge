package dev.whoteva.anglersedge.item;

import dev.whoteva.anglersedge.client.JournalClient;
import dev.whoteva.anglersedge.util.FishCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A book that logs catalog species caught while it sits in the player's inventory.
 * Right-click opens the checklist. Counts live on the stack via {@link CustomData}.
 */
public class FishingJournalItem extends Item {

    private static final String TAG_CAUGHT = "CaughtSpecies";

    public FishingJournalItem(Properties properties) {
        super(properties);
    }

    /** Increments the recorded count for {@code itemId} (for example {@code "minecraft:cod"}) by one. */
    public static void record(ItemStack journalStack, String itemId) {
        CustomData existing = journalStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = existing.copyTag();
        CompoundTag counts = tag.getCompound(TAG_CAUGHT);
        counts.putInt(itemId, counts.getInt(itemId) + 1);
        tag.put(TAG_CAUGHT, counts);
        CustomData.set(DataComponents.CUSTOM_DATA, journalStack, tag);
    }

    /** Species id to times caught. Older journals stored plain ints; that format is still read. */
    public static Map<String, Integer> counts(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag counts = data.copyTag().getCompound(TAG_CAUGHT);
        Map<String, Integer> result = new TreeMap<>();
        for (String key : counts.getAllKeys()) {
            result.put(key, counts.getInt(key));
        }
        return result;
    }

    /** How many catalog species have a count of at least one. */
    public static int discovered(ItemStack stack) {
        Map<String, Integer> counts = counts(stack);
        int discovered = 0;
        for (FishCatalog.Species species : FishCatalog.all()) {
            if (counts.getOrDefault(species.id(), 0) > 0) {
                discovered++;
            }
        }
        return discovered;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            JournalClient.open(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Map<String, Integer> counts = counts(stack);
        int discovered = discovered(stack);
        int total = 0;
        for (FishCatalog.Species species : FishCatalog.all()) {
            total += counts.getOrDefault(species.id(), 0);
        }

        tooltip.add(Component.translatable("tooltip.anglersedge.journal_species", discovered, FishCatalog.size())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.anglersedge.journal_total", total)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.anglersedge.journal_open").withStyle(ChatFormatting.DARK_GRAY));

        int shown = 0;
        for (FishCatalog.Species species : FishCatalog.all()) {
            int count = counts.getOrDefault(species.id(), 0);
            if (count <= 0) {
                continue;
            }
            if (shown >= 6) {
                tooltip.add(Component.translatable("tooltip.anglersedge.journal_more", discovered - shown)
                        .withStyle(ChatFormatting.DARK_GRAY));
                break;
            }
            tooltip.add(Component.translatable("tooltip.anglersedge.journal_line",
                            Component.translatable(species.item().getDescriptionId()), count)
                    .withStyle(species.rarity().color()));
            shown++;
        }
    }

    /** Registry id for a caught stack, or null when it is not a catalog species. */
    public static String catalogId(ItemStack drop) {
        if (drop.isEmpty() || FishCatalog.byItem(drop.getItem()) == null) {
            return null;
        }
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(drop.getItem());
        return key.toString();
    }
}
