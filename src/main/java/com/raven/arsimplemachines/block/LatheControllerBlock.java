package com.raven.arsimplemachines.block;

import ARLib.multiblockCore.BlockMultiblockMaster;
import com.raven.arsimplemachines.blockentity.LatheControllerBlockEntity;
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
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class LatheControllerBlock extends BlockMultiblockMaster implements EntityBlock {
    public static final BooleanProperty STATE_MULTIBLOCK_FORMED = BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED;

    public LatheControllerBlock(Properties props) {
        super(props);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE_MULTIBLOCK_FORMED, HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState()
                .setValue(HORIZONTAL_FACING, facing)
                .setValue(STATE_MULTIBLOCK_FORMED, false);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // ARLib handles hiding via placeholders
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LatheControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {

        if (type != ModBlockEntities.LATHE_CONTROLLER.get()) {
            return null;
        }

        // CLIENT TICKER (animation)
        if (level.isClientSide) {
            return (lvl, pos, st, be) -> {
                if (be instanceof LatheControllerBlockEntity lathe) {
                    lathe.clientTick();
                }
            };
        }

        // SERVER TICKER (recipe logic)
        return (lvl, pos, st, be) -> {
            if (be instanceof LatheControllerBlockEntity lathe) {
                lathe.tick();
            }
        };
    }

}
