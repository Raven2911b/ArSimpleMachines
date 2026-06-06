package com.raven.arsimplemachines;

import ARLib.holoProjector.itemHoloProjector;
import com.mojang.logging.LogUtils;
import com.raven.arsimplemachines.registry.*;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import ARLib.holoProjector.itemHoloProjector;
import com.raven.arsimplemachines.multiblock.LathePattern;

import java.util.HashMap;

@Mod(ArSimpleMachines.MODID)
public class ArSimpleMachines {

    public static final String MODID = "arsimplemachines";
    public static final Logger LOGGER = LogUtils.getLogger();
    public ArSimpleMachines(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModDataComponents.COMPONENTS.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(ModCapabilities::register);
        modEventBus.addListener(this::commonSetup);
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            itemHoloProjector.registerMultiblock(
                    "lathe",
                    LathePattern.flipY(LathePattern.PATTERN),   // ← FLIPPED ONLY FOR PROJECTOR
                    new HashMap<>(LathePattern.MAPPING)
            );
        });
    }



    private void addCreative(BuildCreativeModeTabContentsEvent event) {

        // Building Blocks tab
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModItems.GAS_CHARGE_PAD);
            event.accept(ModItems.LATHE_CONTROLLER);
        }

        // Ingredients tab (ingots, rods, dusts, etc.)
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.TITANIUM_INGOT);
            event.accept(ModItems.TITANIUM_ROD);
        }
    }

}
