package dev.whoteva.anglersedge.item;

import net.minecraft.world.item.Item;

/**
 * A consumable held in the off-hand while fishing. {@link dev.whoteva.anglersedge.event.FishingEventHandler}
 * checks the player's off-hand on every successful catch; if it's one of these, the matching
 * {@link BaitEffect} is applied and one bait is consumed.
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
}
