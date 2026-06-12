package com.raven.arsimplemachines.block;

import ARLib.multiblockCore.BlockMultiblockMaster;
import com.raven.arsimplemachines.blockentity.RollingControllerBlockEntity;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class RollingControllerBlock extends BlockMultiblockMaster implements EntityBlock {

    public static final BooleanProperty STATE_MULTIBLOCK_FORMED = BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED;

    public RollingControllerBlock(Properties props) {
        super(props);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE_MULTIBLOCK_FORMED, HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getHorizontalDirection().getOpposite();
        return defaultBlockState()
                .setValue(HORIZONTAL_FACING, facing)
                .setValue(STATE_MULTIBLOCK_FORMED, false);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RollingControllerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {

        if (type != ModBlockEntities.ROLLING_CONTROLLER.get()) return null;

        if (level.isClientSide) {
            return (lvl, pos, st, be) -> {
                if (be instanceof RollingControllerBlockEntity r) r.clientTick();
            };
        }

        return (lvl, pos, st, be) -> {
            if (be instanceof RollingControllerBlockEntity r) r.tick();
        };
    }
}
