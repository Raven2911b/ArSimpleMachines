package com.raven.arsimplemachines.block;

import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.blockentity.LatheControllerBlockEntity;
import com.raven.arsimplemachines.multiblock.MultiblockControllerBlock;
import com.raven.arsimplemachines.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

public class LatheControllerBlock extends MultiblockControllerBlock implements EntityBlock {

    public LatheControllerBlock(Properties props) {
        super(props);
        System.out.println("LatheControllerBlock: constructor called");
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();

        ArSimpleMachines.LOGGER.info("[Lathe] Placed facing: {}", facing);

        return this.defaultBlockState().setValue(FACING, facing);
    }


    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos,
                        BlockState oldState, boolean isMoving) {
        System.out.println("LatheControllerBlock: onPlace fired at " + pos);
        super.onPlace(state, level, pos, oldState, isMoving);
    }

//    @Override
//    public RenderShape getRenderShape(BlockState state) {
//        // When formed, ARLib hides the controller block
//        return state.getValue(FORMED)
//                ? RenderShape.INVISIBLE
//                : RenderShape.MODEL;
//    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // Always render the model, even when formed
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {

        System.out.println("LatheControllerBlock: use() fired at " + pos);

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LatheControllerBlockEntity controller) {
                System.out.println("LatheControllerBlock: calling validateStructure()");
                controller.validateStructure();
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                            Player player, BlockHitResult hit) {
        System.out.println("LatheControllerBlock: useWithoutItem fired");
        return this.use(state, level, pos, player, InteractionHand.MAIN_HAND, hit);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        System.out.println("LatheControllerBlock: newBlockEntity called at " + pos);
        return new LatheControllerBlockEntity(pos, state);
    }

    // CLEAN TICKER — EMPTY FOR NOW (we fill this in Phase 3)
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {

        if (type != ModBlockEntities.LATHE_CONTROLLER.get()) {
            return null;
        }

        if (level.isClientSide) {
            return (lvl, pos, st, be) -> {
                if (be instanceof LatheControllerBlockEntity lathe) {
                    lathe.clientTick();
                }
            };
        } else {
            return (lvl, pos, st, be) -> {
                if (be instanceof LatheControllerBlockEntity lathe) {
                    lathe.serverTick();
                }
            };
        }
    }
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && state.hasProperty(FORMED) && state.getValue(FORMED)) {
            Direction facing = state.getValue(FACING);
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof LatheControllerBlockEntity controller) {
                System.out.println("LatheControllerBlock: playerWillDestroy - forcing unform with facing=" + facing);

                controller.applyPlaceholdersForPattern(false, facing);

                try {
                    controller.getPattern().applyFormedToWorld(level, pos, false);
                } catch (Exception e) {
                    System.out.println("LatheControllerBlock: failed to applyFormedToWorld in playerWillDestroy: " + e);
                }
            }
        }

        // IMPORTANT: return the state so vanilla can continue destroying the block
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getValue(FORMED)) {
            Direction facing = state.getValue(FACING);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LatheControllerBlockEntity controller) {
                System.out.println("LatheControllerBlock: onRemove - forcing unform with facing=" + facing);
                // Restore all placeholder blocks
                controller.applyPlaceholdersForPattern(false, facing);
                // Clear FORMED property on all multiblock structure blocks
                try {
                    controller.getPattern().applyFormedToWorld(level, pos, false);
                } catch (Exception e) {
                    System.out.println("LatheControllerBlock: failed to applyFormedToWorld: " + e);
                }
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }
}
