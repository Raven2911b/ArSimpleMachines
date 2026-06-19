package com.raven.arsimplemachines;

import ARLib.holoProjector.itemHoloProjector;
import ARLib.ARLibRegistry;
import com.mojang.logging.LogUtils;
import com.raven.arsimplemachines.registry.*;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            // Projector pattern – MUST be rectangular, no nulls, no empty layers
            Object[][][] latheProjectorPattern = new Object[][][]{
                    { { 'C', 'M',null, 'O' } },   // top layer (y = 0)
                    { { 'E', 'S', 'S', 'I' } }    // bottom layer   (y = 1)
            };
            Object[][][] rollingProjectorPattern = new Object[][][]{
                    // Y = 0 (top)
                    {
                            { 'C', null, null ,null},   // Z = 0
                            { 'I', 'S', 'S', null},   // Z = 1
                            { 'E', 'S', 'S', null}    // Z = 2
                    },
                    // Y = 1 (bottom)
                    {
                            { 'F', 'R', 'R',null },   // Z = 0
                            { null, 'S', 'S','O' },   // Z = 1
                            { null, 'S', 'S','O' }    // Z = 2
                    }
            };

            // Projector mapping using ARLib blocks
            Map<Character, List<net.minecraft.world.level.block.Block>> latheProjectorMapping = Map.of(
                    'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
                    'S', List.of(ARLibRegistry.BLOCK_STRUCTURE.get()),
                    'I', List.of(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get()),
                    'O', List.of(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get()),
                    'M', List.of(ARLibRegistry.BLOCK_MOTOR.get()),
                    'C', List.of(ModBlocks.LATHE_CONTROLLER.get())
            );
            Map<Character, List<net.minecraft.world.level.block.Block>> rollingProjectorMapping = Map.of(
                    'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
                    'F', List.of(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get()),
                    'S', List.of(ARLibRegistry.BLOCK_STRUCTURE.get()),
                    'I', List.of(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get()),
                    'O', List.of(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get()),
                    'R', List.of(ARLibRegistry.BLOCK_MOTOR.get()),
                    'C', List.of(ModBlocks.ROLLING_CONTROLLER.get())

            );

            itemHoloProjector.registerMultiblock(
                    "Lathe",
                    latheProjectorPattern,              // no flipY
                    new HashMap<>(latheProjectorMapping)
            );
            itemHoloProjector.registerMultiblock(
                    "Rolling Machine",
                    rollingProjectorPattern,
                    new HashMap<>(rollingProjectorMapping)
            );

        });
    }
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModItems.GAS_CHARGE_PAD);
            event.accept(ModItems.LATHE_CONTROLLER);
            event.accept(ModItems.ROLLING_CONTROLLER);
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.TITANIUM_INGOT);
            event.accept(ModItems.TITANIUM_ROD);
            event.accept(ModItems.TITANIUM_PLATE);
        }
    }
}
