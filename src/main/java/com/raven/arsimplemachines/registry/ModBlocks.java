package com.raven.arsimplemachines.registry;

import com.raven.arsimplemachines.ArSimpleMachines;

import com.raven.arsimplemachines.block.*;

import ARLib.multiblockCore.BlockMultiblockPart;

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
    // ROLLING CONTROLLER (MASTER)
    // -------------------------
    public static final DeferredBlock<Block> ROLLING_CONTROLLER =
            BLOCKS.register("rolling_controller",
                    () -> new RollingControllerBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(3.0f)
                                    .noOcclusion()
                    ));

    // -------------------------
    // CHEMICAL REACTOR CONTROLLER (MASTER)
    // -------------------------
    public static final DeferredBlock<Block> CHEMICAL_REACTOR_CONTROLLER =
            BLOCKS.register("chemical_reactor_controller",
                    () -> new ChemicalReactorControllerBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(3.5f)
                                    .noOcclusion()
                    ));

    // -------------------------
    // ELECTROLYZER CONTROLLER (MASTER)
    // -------------------------
    public static final DeferredBlock<Block> ELECTROLYZER_CONTROLLER =
            BLOCKS.register("electrolyzer_controller",
                    () -> new ElectrolyzerControllerBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(3.5f)
                                    .noOcclusion()
                    ));
    // -------------------------
    // ELECTRIC ARC FURNACE CONTROLLER (MASTER)
    // -------------------------
    public static final DeferredBlock<Block> ELECTRIC_ARC_FURNACE_CONTROLLER =
            BLOCKS.register("electric_arc_furnace_controller",
                    () -> new ElectricArcFurnaceControllerBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(4.0f)
                                    .noOcclusion()
                    ));

    public static final DeferredBlock<Block> BLAST_BRICK =
            BLOCKS.register("blast_brick",
                    () -> new Block(BlockBehaviour.Properties.of()
                            .strength(3.5F)
                            .requiresCorrectToolForDrops()));



}
