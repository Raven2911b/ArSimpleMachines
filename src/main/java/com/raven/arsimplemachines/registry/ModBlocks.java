package com.raven.arsimplemachines.registry;

import com.raven.arsimplemachines.ArSimpleMachines;

import com.raven.arsimplemachines.block.*;

import ARLib.multiblockCore.BlockMultiblockPart;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
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
                                    .requiresCorrectToolForDrops()
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
                                    .requiresCorrectToolForDrops()
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
                                    .requiresCorrectToolForDrops()
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
                                    .requiresCorrectToolForDrops()
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
                                    .requiresCorrectToolForDrops()
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
                                    .requiresCorrectToolForDrops()
                    ));

    // ---------------------------------------------------------
    // CRYSTALLIZER CONTROLLER
    // ---------------------------------------------------------
    public static final DeferredBlock<Block> CRYSTALLIZER_CONTROLLER =
            BLOCKS.register("crystallizer_controller",
                    () -> new CrystallizerControllerBlock(
                            Block.Properties.of()
                                    .strength(3.0f)
                                    .noOcclusion()
                    ));

    // ---------------------------------------------------------
    // CUTTING MACHINE CONTROLLER
    // ---------------------------------------------------------
    public static final DeferredBlock<Block> CUTTING_MACHINE_CONTROLLER =
            BLOCKS.register("cutting_machine_controller",
                    () -> new CuttingMachineControllerBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(3.0f)
                                    .noOcclusion()
                                    .requiresCorrectToolForDrops()
                    ));

    public static final DeferredBlock<Block> BLAST_BRICK =
            BLOCKS.register("blast_brick",
                    () -> new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .instrument(NoteBlockInstrument.BASEDRUM)
                            .strength(3.0f)
                            .sound(SoundType.STONE)
                            .strength(3.5F)
                            .requiresCorrectToolForDrops()
                            .forceSolidOn()));

    public static final DeferredBlock<Block> RUTILE_ORE =
            BLOCKS.register("rutile_ore",
                    () -> new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .instrument(NoteBlockInstrument.BASEDRUM)
                            .strength(3.0f)
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
                    ));
    public static final DeferredBlock<Block> SAW_BLADE_ASSEMBLY =
            BLOCKS.register("saw_blade_assembly",
                    () -> new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .instrument(NoteBlockInstrument.BASEDRUM)
                            .strength(3.0f)
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
                    ));
}
