package com.raven.arsimplemachines.block;

import ARLib.multiblockCore.BlockMultiblockMaster;
import com.raven.arsimplemachines.blockentity.ElectricArcFurnaceControllerBlockEntity;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.MenuProvider;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class ElectricArcFurnaceControllerBlock extends BlockMultiblockMaster implements EntityBlock {

    public static final BooleanProperty STATE_MULTIBLOCK_FORMED = BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED;
    public static final BooleanProperty RUNNING = BooleanProperty.create("running");


    public ElectricArcFurnaceControllerBlock(Properties props) {
        super(props);
        // Register default state so placed block has correct property values
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(HORIZONTAL_FACING, Direction.NORTH)
                        .setValue(STATE_MULTIBLOCK_FORMED, false)
                        .setValue(RUNNING, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE_MULTIBLOCK_FORMED, HORIZONTAL_FACING, RUNNING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getHorizontalDirection().getOpposite();
        return defaultBlockState()
                .setValue(HORIZONTAL_FACING, facing)
                .setValue(STATE_MULTIBLOCK_FORMED, false)
                .setValue(RUNNING, false);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricArcFurnaceControllerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {

        if (type != ModBlockEntities.ELECTRIC_ARC_FURNACE_CONTROLLER.get()) return null;

        if (level.isClientSide) {
            return (lvl, pos, st, be) -> {
                if (be instanceof ElectricArcFurnaceControllerBlockEntity e) e.clientTick();
            };
        }

        return (lvl, pos, st, be) -> {
            if (be instanceof ElectricArcFurnaceControllerBlockEntity e) e.tick();
        };
    }
    //@Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        return useWithoutItem(state, level, pos, player, hit);
    }
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                            Player player, BlockHitResult hit) {
        if (!state.getValue(STATE_MULTIBLOCK_FORMED)) {

            // RUN DEBUG CHECK HERE
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ElectricArcFurnaceControllerBlockEntity e) {
                e.debugStructureCheck();
            }

            // THEN LET ARLIB SCAN
            return super.useWithoutItem(state, level, pos, player, hit);
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof MenuProvider provider)) {
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide) {
            player.openMenu(provider, buf -> buf.writeBlockPos(pos));
        }

        return InteractionResult.SUCCESS;
    }

}
