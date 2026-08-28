package com.raven.arsimplemachines.client;

import net.minecraft.client.color.item.ItemColors;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.registry.ModItems;

@EventBusSubscriber(modid = ArSimpleMachines.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ItemColorHandler {

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        ItemColors itemColors = event.getItemColors();

        // Register tinted wrench
        itemColors.register(
                (stack, tintIndex) -> {
                    if (tintIndex == 0) {
                        return 0xFF6B00; // Orange base
                    } else if (tintIndex == 1) {
                        return 0xFF0000; // Red overlay
                    }
                    return -1;
                },
                ModItems.TINTED_WRENCH.get()
        );
    }
}