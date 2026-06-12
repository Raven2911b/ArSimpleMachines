package com.raven.arsimplemachines.registry;

import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.block.LatheControllerBlock;
import com.raven.arsimplemachines.block.GasChargePadBlock;

import ARLib.multiblockCore.BlockMultiblockPart;

import com.raven.arsimplemachines.block.RollingControllerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ArSimpleMachines.MODID);

    // -------------------------
    // GAS CHARGE PAD
    // -------------------------
    public static final DeferredBlock<Block> GAS_CHARGE_PAD =
            BLOCKS.register("gas_charge_pad",
                    () -> new GasChargePadBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(2.0f)
                                    .noOcclusion()
                    ));

    // -------------------------
    // LATHE CONTROLLER (MASTER)
    // -------------------------
    public static final DeferredBlock<Block> LATHE_CONTROLLER =
            BLOCKS.register("lathe_controller",
                    () -> new LatheControllerBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(3.0f)
                                    .noOcclusion()
                    ));
    // -------------------------
    // LATHE CONTROLLER (MASTER)
    // -------------------------
    public static final DeferredBlock<Block> ROLLING_CONTROLLER =
            BLOCKS.register("rolling_controller",
                    () -> new RollingControllerBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(3.0f)
                                    .noOcclusion()
                    ));
}
