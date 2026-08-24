package com.raven.arsimplemachines.registry;

import com.raven.arsimplemachines.ArSimpleMachines;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ArSimpleMachines.MODID);

    // -------------------------
    // GAS CHARGE PAD
    // -------------------------
    public static final DeferredItem<BlockItem> GAS_CHARGE_PAD =
            ITEMS.registerSimpleBlockItem("gas_charge_pad", ModBlocks.GAS_CHARGE_PAD);

    // -------------------------
    // LATHE CONTROLLER
    // -------------------------
    public static final DeferredItem<BlockItem> LATHE_CONTROLLER =
            ITEMS.registerSimpleBlockItem("lathe_controller", ModBlocks.LATHE_CONTROLLER);

    // -------------------------
    // ROLLING CONTROLLER
    // -------------------------
    public static final DeferredItem<BlockItem> ROLLING_CONTROLLER =
            ITEMS.registerSimpleBlockItem("rolling_controller", ModBlocks.ROLLING_CONTROLLER);

    // -------------------------
    // CHEMICAL REACTOR CONTROLLER
    // -------------------------
    public static final DeferredItem<BlockItem> CHEMICAL_REACTOR_CONTROLLER =
            ITEMS.registerSimpleBlockItem("chemical_reactor_controller", ModBlocks.CHEMICAL_REACTOR_CONTROLLER);

    // -------------------------
    // ELECTROLYZER CONTROLLER
    // -------------------------
    public static final DeferredItem<BlockItem> ELECTROLYZER_CONTROLLER =
            ITEMS.registerSimpleBlockItem("electrolyzer_controller", ModBlocks.ELECTROLYZER_CONTROLLER);

    // -------------------------
    // ELECTRIC ARC FURNACE CONTROLLER
    // -------------------------
    public static final DeferredItem<BlockItem> ELECTRIC_ARC_FURNACE_CONTROLLER =
            ITEMS.registerSimpleBlockItem("electric_arc_furnace_controller", ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER);

    // -------------------------
    // CRYSTALLIZER CONTROLLER
    // -------------------------
    public static final DeferredItem<BlockItem> CRYSTALLIZER_CONTROLLER =
            ITEMS.registerSimpleBlockItem("crystallizer_controller", ModBlocks.CRYSTALLIZER_CONTROLLER);

    // -------------------------
    // CUTTING MACHINE CONTROLLER
    // -------------------------
    public static final DeferredItem<BlockItem> CUTTING_MACHINE_CONTROLLER =
            ITEMS.registerSimpleBlockItem("cutting_machine_controller", ModBlocks.CUTTING_MACHINE_CONTROLLER);

    // -------------------------
    // PRECISION ASSEMBLER CONTROLLER
    // -------------------------
    public static final DeferredItem<BlockItem> PRECISION_ASSEMBLER_CONTROLLER =
            ITEMS.registerSimpleBlockItem("precision_assembler_controller", ModBlocks.PRECISION_ASSEMBLER_CONTROLLER);


    // -------------------------
    // MATERIALS
    // -------------------------
    public static final DeferredItem<Item> TITANIUM_INGOT =
            ITEMS.register("titanium_ingot", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> TITANIUM_ROD =
            ITEMS.register("titanium_rod", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> TITANIUM_PLATE =
            ITEMS.register("titanium_plate",
                    () -> new Item(new Item.Properties()));

    public static final DeferredItem<BlockItem> BLAST_BRICK =
            ITEMS.registerSimpleBlockItem("blast_brick", ModBlocks.BLAST_BRICK);

    public static final DeferredItem<BlockItem> RUTILE_ORE =
            ITEMS.register("rutile_ore",
                    () -> new BlockItem(ModBlocks.RUTILE_ORE.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> SAW_BLADE_ASSEMBLY =
            ITEMS.registerSimpleBlockItem("saw_blade_assembly", ModBlocks.SAW_BLADE_ASSEMBLY);

    public static final DeferredItem<Item> SILICON_INGOT =
            ITEMS.register("silicon_ingot", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> SILICON_NUGGET =
            ITEMS.register("silicon_nugget", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> SILICON_BOULE =
            ITEMS.register("silicon_boule", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> SAW_BLADE =
            ITEMS.register("saw_blade", () -> new Item(new Item.Properties()));;

    public static final DeferredItem<Item> SILICON_WAFER =
            ITEMS.register("silicon_wafer", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> USER_INTERFACE =
            ITEMS.register("user_interface", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> LIQUID_IO_BOARD =
            ITEMS.register("liquid_io_board", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> IO_CIRCUIT_BOARD =
            ITEMS.register("io_circuit_board", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> CONTROL_CIRCUIT_BOARD =
            ITEMS.register("control_circuit_board", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> IRON_PLATE =
            ITEMS.register("iron_plate", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> IRON_STICK =
            ITEMS.register("iron_stick", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> COPPER_PLATE =
            ITEMS.register("copper_plate", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> COPPER_STICK =
            ITEMS.register("copper_stick", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> GOLD_PLATE =
            ITEMS.register("gold_plate", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> GOLD_STICK =
            ITEMS.register("gold_stick", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> STEEL_GEAR =
            ITEMS.register("steel_gear", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> FAN =
            ITEMS.register("fan", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> STEEL_PLATE =
            ITEMS.register("steel_plate", () -> new Item(new Item.Properties()));;

    public static final DeferredItem<Item> STEEL_INGOT =
            ITEMS.register("steel_ingot", () -> new Item(new Item.Properties()));;

    public static final DeferredItem<Item> STEEL_STICK =
            ITEMS.register("steel_stick", () -> new Item(new Item.Properties()));;



    public static final DeferredItem<Item> BASIC_CIRCUIT_PLATE =
            ITEMS.register("basic_circuit_plate", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> BASIC_CIRCUIT =
            ITEMS.register("basic_circuit", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> ADVANCED_CIRCUIT_PLATE =
            ITEMS.register("advanced_circuit_plate", () -> new Item(new Item.Properties()));;
}
