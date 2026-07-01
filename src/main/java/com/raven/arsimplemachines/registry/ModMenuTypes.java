package com.raven.arsimplemachines.registry;

import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.menu.GasChargePadMenu;
import com.raven.arsimplemachines.menu.LatheMenu;
import com.raven.arsimplemachines.menu.RollingMenu;
import com.raven.arsimplemachines.menu.ChemicalReactorMenu;

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

    public static final DeferredHolder<MenuType<?>, MenuType<LatheMenu>> LATHE_MENU =
            MENUS.register("lathe_menu",
                    () -> new MenuType<>(
                            (IContainerFactory<LatheMenu>) (windowId, inv, buf) ->
                                    new LatheMenu(windowId, inv, buf),
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );

    public static final DeferredHolder<MenuType<?>, MenuType<RollingMenu>> ROLLING_MENU =
            MENUS.register("rolling_menu",
                    () -> new MenuType<>(
                            (IContainerFactory<RollingMenu>) (windowId, inv, buf) ->
                                    new RollingMenu(windowId, inv, buf),
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );

    // ---------------------------------------------------------
    // CHEMICAL REACTOR MENU
    // ---------------------------------------------------------
    public static final DeferredHolder<MenuType<?>, MenuType<ChemicalReactorMenu>> CHEMICAL_REACTOR_MENU =
            MENUS.register("chemical_reactor_menu",
                    () -> new MenuType<>(
                            (IContainerFactory<ChemicalReactorMenu>) (windowId, inv, buf) ->
                                    new ChemicalReactorMenu(windowId, inv, buf),
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );
}
