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

    public static final DeferredItem<Item> SILICON_INGOT =
            ITEMS.register("silicon_ingot", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> SILICON_NUGGET =
            ITEMS.register("silicon_nugget", () -> new Item(new Item.Properties()));;
    public static final DeferredItem<Item> SILICON_BOULE =
            ITEMS.register("silicon_boule", () -> new Item(new Item.Properties()));;
}
