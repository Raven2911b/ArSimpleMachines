package com.raven.arsimplemachines.registry;

import com.raven.arsimplemachines.ArSimpleMachines;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ArSimpleMachines.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> AR_SIMPLE_MACHINES_TAB =
            TABS.register("arsimplemachines_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.arsimplemachines"))
                            .icon(() -> new ItemStack(ModBlocks.LATHE_CONTROLLER.get()))
                            .displayItems((params, output) -> {

                                // ---------------------------------------------------------
                                // MACHINES
                                // ---------------------------------------------------------
                                output.accept(ModBlocks.LATHE_CONTROLLER.get());
                                output.accept(ModBlocks.ROLLING_CONTROLLER.get());
                                output.accept(ModBlocks.GAS_CHARGE_PAD.get());

                                // Various multiblock parts
                                output.accept(ModBlocks.CHEMICAL_REACTOR_CONTROLLER.get());
                                output.accept(ModBlocks.ELECTROLYZER_CONTROLLER.get());
                                output.accept(ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get());
                                output.accept(ModBlocks.CRYSTALLIZER_CONTROLLER.get());
                                output.accept(ModBlocks.CUTTING_MACHINE_CONTROLLER.get());
                                output.accept(ModBlocks.PRECISION_ASSEMBLER_CONTROLLER.get());

                                // ---------------------------------------------------------
                                // MATERIALS
                                // ---------------------------------------------------------
                                output.accept(ModItems.TITANIUM_INGOT.get());
                                output.accept(ModItems.TITANIUM_PLATE.get());
                                output.accept(ModItems.TITANIUM_SHEET.get());
                                output.accept(ModItems.TITANIUM_GEAR.get());
                                output.accept(ModItems.TITANIUM_STICK.get());
                                output.accept(ModItems.TITANIUM_DUST.get());
                                output.accept(ModItems.TITANIUM_NUGGET.get());
                                output.accept(ModItems.BLAST_BRICK.get());
                                output.accept(ModItems.RUTILE_ORE.get());
                                output.accept(ModItems.SILICON_INGOT.get());
                                output.accept(ModItems.SILICON_NUGGET.get());
                                output.accept(ModItems.SILICON_BOULE.get());
                                output.accept(ModItems.SAW_BLADE.get());
                                output.accept(ModItems.SILICON_WAFER.get());
                                output.accept(ModItems.SAW_BLADE_ASSEMBLY.get());
                                output.accept(ModItems.USER_INTERFACE.get());
                                output.accept(ModItems.LIQUID_IO_BOARD.get());
                                output.accept(ModItems.IO_CIRCUIT_BOARD.get());
                                output.accept(ModItems.CONTROL_CIRCUIT_BOARD.get());
                                output.accept(ModItems.IRON_PLATE.get());
                                output.accept(ModItems.IRON_STICK.get());
                                output.accept(ModItems.IRON_SHEET.get());
                                output.accept(ModItems.COPPER_PLATE.get());
                                output.accept(ModItems.COPPER_STICK.get());
                                output.accept(ModItems.GOLD_PLATE.get());
                                output.accept(ModItems.GOLD_STICK.get());
                                output.accept(ModItems.STEEL_GEAR.get());
                                output.accept(ModItems.FAN.get());
                                output.accept(ModItems.BASIC_CIRCUIT_PLATE.get());
                                output.accept(ModItems.BASIC_CIRCUIT.get());
                                output.accept(ModItems.ADVANCED_CIRCUIT_PLATE.get());
                                output.accept(ModItems.STEEL_INGOT.get());
                                output.accept(ModItems.STEEL_STICK.get());
                                output.accept(ModItems.STEEL_PLATE.get());
                                output.accept(ModItems.STEEL_SHEET.get());
                                output.accept(ModItems.TINTED_WRENCH.get());
                                output.accept(ModItems.ALUMINUM_GEAR.get());
                                output.accept(ModItems.ALUMINUM_INGOT.get());
                                output.accept(ModItems.ALUMINUM_NUGGET.get());
                                output.accept(ModItems.ALUMINUM_PLATE.get());
                                output.accept(ModItems.ALUMINUM_SHEET.get());
                                output.accept(ModItems.ALUMINUM_STICK.get());
                                output.accept(ModItems.ALUMINUM_DUST.get());
                            })
                            .build()
            );
}
