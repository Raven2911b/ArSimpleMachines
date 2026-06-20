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

                                // Machines
                                output.accept(ModBlocks.LATHE_CONTROLLER.get());
                                output.accept(ModBlocks.ROLLING_CONTROLLER.get());
                                output.accept(ModBlocks.GAS_CHARGE_PAD.get());

                                // Materials
                                output.accept(ModItems.TITANIUM_INGOT.get());
                                output.accept(ModItems.TITANIUM_PLATE.get());
                                output.accept(ModItems.TITANIUM_ROD.get());
                            })
                            .build()
            );
}
