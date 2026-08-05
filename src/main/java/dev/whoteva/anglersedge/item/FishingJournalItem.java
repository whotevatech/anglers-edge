package dev.whoteva.anglersedge.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A book that logs every catch this mod's fishing has made while it was somewhere in the
 * player's inventory - both which species have been seen and how many of each. Deliberately
 * simple: the log is stored directly on the item stack via the vanilla {@link CustomData}
 * component (a plain {@code CompoundTag} of {@code registry id -> count}, the same mechanism
 * vanilla itself uses for arbitrary per-stack data), so there's no new registry, no capability,
 * and nothing to keep in sync - the journal stack itself is the save file. Picking up a second
 * journal starts a second, independent log.
 */
public class FishingJournalItem extends Item {

    private static final String TAG_CAUGHT = "CaughtSpecies";

    public FishingJournalItem(Properties properties) {
        super(properties);
    }

    /** Increments the recorded count for {@code itemId} (e.g. {@code "minecraft:cod"}) by one. */
    public static void record(ItemStack journalStack, String itemId) {
        CustomData existing = journalStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = existing.copyTag();
        CompoundTag counts = tag.getCompound(TAG_CAUGHT);
        counts.putInt(itemId, counts.getInt(itemId) + 1);
        tag.put(TAG_CAUGHT, counts);
        CustomData.set(DataComponents.CUSTOM_DATA, journalStack, tag);
    }

    /** Species id -> times caught, sorted alphabetically for a stable tooltip order. */
    private static Map<String, Integer> caught(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag counts = data.copyTag().getCompound(TAG_CAUGHT);
        Map<String, Integer> result = new TreeMap<>();
        for (String key : counts.getAllKeys()) {
            result.put(key, counts.getInt(key));
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Map<String, Integer> species = caught(stack);
        int total = species.values().stream().mapToInt(Integer::intValue).sum();

        tooltip.add(Component.translatable("tooltip.anglersedge.journal_species", species.size())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.anglersedge.journal_total", total)
                .withStyle(ChatFormatting.GRAY));

        int shown = 0;
        for (Map.Entry<String, Integer> entry : species.entrySet()) {
            if (shown >= 8) {
                tooltip.add(Component.literal("...").withStyle(ChatFormatting.DARK_GRAY));
                break;
            }
            String id = entry.getKey();
            String name = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
            tooltip.add(Component.literal("- " + name + " x" + entry.getValue())
                    .withStyle(ChatFormatting.DARK_AQUA));
            shown++;
        }
    }
}
