package dev.whoteva.anglersedge.block;

import com.mojang.serialization.MapCodec;
import dev.whoteva.anglersedge.block.entity.FishTrophyBlockEntity;
import dev.whoteva.anglersedge.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Set;

/**
 * A decorative case that mounts exactly one fish. Right-click with a fish item in hand to mount
 * it; right-click empty-handed to take it back out. Any vanilla or modded fish drop works, not
 * just this mod's own species - a trophy case that could only hold three specific items felt
 * needlessly restrictive.
 */
public class FishTrophyBlock extends BaseEntityBlock {

    public static final MapCodec<FishTrophyBlock> CODEC = simpleCodec(FishTrophyBlock::new);

    // Built lazily, NOT as an eagerly-evaluated static field: NeoForge fires the blocks
    // RegisterEvent before the items one (BlockItems need their block to already exist), so
    // constructing this block - which loads this class - happens before ModItems.GLIMMERFIN and
    // friends are bound. Calling DeferredHolder#get() that early throws "trying to access
    // unbound value". Deferring construction to first actual use (a player right-clicking the
    // block, necessarily after the whole mod has finished loading) sidesteps the ordering issue
    // entirely.
    private static Set<Item> displayable;

    private static Set<Item> displayable() {
        if (displayable == null) {
            displayable = Set.of(
                    Items.COD, Items.SALMON, Items.PUFFERFISH, Items.TROPICAL_FISH,
                    ModItems.GLIMMERFIN.get(), ModItems.SWAMP_EEL.get(), ModItems.ABYSSAL_RAY.get(),
                    ModItems.LEVIATHAN_SCALE.get(), ModItems.REEF_CRAB.get(), ModItems.ANGLERFISH.get(),
                    ModItems.SHARK_TOOTH.get());
        }
        return displayable;
    }

    public FishTrophyBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FishTrophyBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FishTrophyBlockEntity trophy) || !displayable().contains(stack.getItem())
                || !trophy.displayed().isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            ItemStack mounted = stack.copyWithCount(1);
            stack.shrink(1);
            trophy.setItem(0, mounted);
            sync(level, pos, state);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FishTrophyBlockEntity trophy) || trophy.displayed().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            ItemStack taken = trophy.removeItem(0, 1);
            player.getInventory().placeItemBackInInventory(taken);
            sync(level, pos, state);
        }
        return InteractionResult.SUCCESS;
    }

    private static void sync(Level level, BlockPos pos, BlockState state) {
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            Containers.dropContentsOnDestroy(state, newState, level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
