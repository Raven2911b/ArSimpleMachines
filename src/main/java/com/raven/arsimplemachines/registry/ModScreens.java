package com.raven.arsimplemachines.registry;

import com.raven.arsimplemachines.screen.GasChargePadScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = "arsimplemachines", value = Dist.CLIENT)
public class ModScreens {

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.GAS_CHARGE_PAD_MENU.get(), GasChargePadScreen::new);
    }
}
