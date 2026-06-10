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
    // -------------------------
    // MATERIALS
    // -------------------------
    public static final DeferredItem<Item> TITANIUM_INGOT =
            ITEMS.register("titanium_ingot", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> TITANIUM_ROD =
            ITEMS.register("titanium_rod", () -> new Item(new Item.Properties()));
}
