package com.raven.arsimplemachines.registry;

import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.menu.GasChargePadMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ArSimpleMachines.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GasChargePadMenu>> GAS_CHARGE_PAD_MENU =
            MENUS.register("gas_charge_pad_menu",
                    () -> new MenuType<>(
                            (IContainerFactory<GasChargePadMenu>) (windowId, inv, buf) ->
                                    new GasChargePadMenu(windowId, inv, buf.readBlockPos()),
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );
}
