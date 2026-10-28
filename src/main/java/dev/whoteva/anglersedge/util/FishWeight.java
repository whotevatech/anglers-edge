package dev.whoteva.anglersedge.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

/**
 * Rolls a weight and a size title onto a caught fish. Vanilla species are included, so the
 * numbers live in {@link FishCatalog} and are written as name, lore, and a small custom-data
 * tag the trophy renderer can read back.
 */
public final class FishWeight {

    private static final String TAG_ROOT = "AnglersEdge";
    private static final String TAG_QUALITY = "Quality";
    private static final String TAG_WEIGHT = "Weight";

    private FishWeight() {
    }

    public static boolean isSizeable(Item item) {
        FishCatalog.Species species = FishCatalog.byItem(item);
        return species != null && species.sizeable();
    }

    /** -1 when this stack was never weighed. */
    public static float qualityOf(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return -1f;
        }
        CompoundTag root = data.copyTag();
        if (!root.contains(TAG_ROOT)) {
            return -1f;
        }
        return root.getCompound(TAG_ROOT).getFloat(TAG_QUALITY);
    }

    /** Rolls and applies a weight + size title. No-op for items that are not sizeable. */
    public static void apply(ItemStack stack, RandomSource random) {
        FishCatalog.Species species = FishCatalog.byItem(stack.getItem());
        if (species == null || !species.sizeable()) {
            return;
        }
        float t = random.nextFloat();
        float weight = species.minWeight() + t * (species.maxWeight() - species.minWeight());
        String titleKey = titleKey(t);
        ChatFormatting color = colorFor(t);

        Component baseName = Component.translatable(stack.getItem().getDescriptionId());
        Component displayName = titleKey == null
                ? baseName.copy().withStyle(color)
                : Component.translatable("fish.anglersedge.sized", Component.translatable(titleKey), baseName).withStyle(color);
        stack.set(DataComponents.CUSTOM_NAME, displayName);

        Component weightLine = weight < 1f
                ? Component.translatable("tooltip.anglersedge.weight_oz", String.format("%.1f", weight * 16f))
                : Component.translatable("tooltip.anglersedge.weight_lb", String.format("%.1f", weight));
        weightLine = weightLine.copy().withStyle(ChatFormatting.GRAY);

        Component rarityTag = FishRarity.tagFor(stack);
        List<Component> lore = rarityTag == null ? List.of(weightLine) : List.of(rarityTag, weightLine);
        stack.set(DataComponents.LORE, new ItemLore(lore));

        CustomData existing = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag root = existing.copyTag();
        CompoundTag fish = new CompoundTag();
        fish.putFloat(TAG_QUALITY, t);
        fish.putFloat(TAG_WEIGHT, weight);
        root.put(TAG_ROOT, fish);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, root);
    }

    /** Null means an average fish, which keeps its plain name. */
    private static String titleKey(float t) {
        if (t < 0.15F) return "size.anglersedge.tiny";
        if (t < 0.35F) return "size.anglersedge.small";
        if (t < 0.65F) return null;
        if (t < 0.85F) return "size.anglersedge.large";
        if (t < 0.97F) return "size.anglersedge.massive";
        return "size.anglersedge.record";
    }

    private static ChatFormatting colorFor(float t) {
        if (t < 0.15F) return ChatFormatting.GRAY;
        if (t < 0.65F) return ChatFormatting.WHITE;
        if (t < 0.85F) return ChatFormatting.GREEN;
        if (t < 0.97F) return ChatFormatting.GOLD;
        return ChatFormatting.LIGHT_PURPLE;
    }
}
