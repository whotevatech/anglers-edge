package dev.whoteva.anglersedge.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * One trigger for every mod milestone. Advancements pass {@code "milestone": "journal_complete"}
 * (and friends) so each toast can have its own icon and parent without a class per goal.
 */
public class MilestoneTrigger extends SimpleCriterionTrigger<MilestoneTrigger.Instance> {

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, String milestone) {
        super.trigger(player, instance -> instance.milestone.equals(milestone));
    }

    public record Instance(String milestone) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("milestone").forGetter(Instance::milestone)
        ).apply(instance, Instance::new));

        @Override
        public Optional<ContextAwarePredicate> player() {
            return Optional.empty();
        }
    }
}
