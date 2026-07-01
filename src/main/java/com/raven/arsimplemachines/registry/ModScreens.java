package com.raven.arsimplemachines.registry;

import com.raven.arsimplemachines.screen.GasChargePadScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.raven.arsimplemachines.screen.RollingScreen;
import com.raven.arsimplemachines.screen.LatheScreen;
import com.raven.arsimplemachines.screen.ChemicalReactorScreen;

@EventBusSubscriber(modid = "arsimplemachines", value = Dist.CLIENT)
public class ModScreens {

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.GAS_CHARGE_PAD_MENU.get(), GasChargePadScreen::new);
        event.register(ModMenuTypes.ROLLING_MENU.get(), RollingScreen::new);
        event.register(ModMenuTypes.LATHE_MENU.get(), LatheScreen::new);
        event.register(ModMenuTypes.CHEMICAL_REACTOR_MENU.get(), ChemicalReactorScreen::new);

    }
}
