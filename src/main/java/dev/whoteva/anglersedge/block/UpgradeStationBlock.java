package dev.whoteva.anglersedge.block;

import com.mojang.serialization.MapCodec;
import dev.whoteva.anglersedge.block.entity.UpgradeStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class UpgradeStationBlock extends BaseEntityBlock {

    public static final MapCodec<UpgradeStationBlock> CODEC = simpleCodec(UpgradeStationBlock::new);

    public UpgradeStationBlock(Properties properties) {
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
        return new UpgradeStationBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof MenuProvider menuProvider) {
            player.openMenu(menuProvider);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        // The result slot is a live preview. Dropping it alongside the inputs would duplicate the craft.
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof UpgradeStationBlockEntity station) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), station.getItem(0));
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), station.getItem(1));
            station.setItem(0, ItemStack.EMPTY);
            station.setItem(1, ItemStack.EMPTY);
            station.setItem(2, ItemStack.EMPTY);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
