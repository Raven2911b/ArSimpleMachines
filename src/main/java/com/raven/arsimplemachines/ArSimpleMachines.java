package com.raven.arsimplemachines;

import ARLib.holoProjector.itemHoloProjector;
import ARLib.ARLibRegistry;
import com.mojang.logging.LogUtils;
import com.raven.arsimplemachines.registry.*;
import com.raven.arsimplemachines.util.CategoryRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
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
        ModRecipeTypes.RECIPE_TYPES.register(modEventBus);
        ModRecipeTypes.SERIALIZERS.register(modEventBus);

        modEventBus.addListener(ModCapabilities::register);
        modEventBus.addListener(this::commonSetup);
        ModCreativeTabs.TABS.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {

            // ⭐ CATEGORY REGISTRATION ⭐
            CategoryRegistry.add("ingot/iron", Items.IRON_INGOT);
            CategoryRegistry.add("ingot/titanium", ModItems.TITANIUM_INGOT.get());

            // Helper: safely add items from other mods
            java.util.function.BiConsumer<String, ResourceLocation> safeAdd = (category, rl) -> {
                var item = BuiltInRegistries.ITEM.get(rl);

                // Skip null items
                if (item == null) {
                    ArSimpleMachines.LOGGER.warn("Skipping NULL item for category {}: {}", category, rl);
                    return;
                }

                // Skip AIR (dummy item returned when mod is missing)
                if (item == Items.AIR) {
                    ArSimpleMachines.LOGGER.warn("Skipping AIR item for category {}: {}", category, rl);
                    return;
                }

                // Skip dummy items with no registry key
                var key = BuiltInRegistries.ITEM.getKey(item);
                if (key == null) {
                    ArSimpleMachines.LOGGER.warn("Skipping DUMMY item for category {}: {}", category, rl);
                    return;
                }

                CategoryRegistry.add(category, item);
            };


            // Electrum (Immersive Engineering)
            safeAdd.accept("ingot/electrum",
                    ResourceLocation.fromNamespaceAndPath("immersiveengineering", "ingot_electrum"));

            // Electrum (Thermal)
            safeAdd.accept("ingot/electrum",
                    ResourceLocation.fromNamespaceAndPath("thermal", "electrum_ingot"));

            // Electrum (More Ores & Gems)
            safeAdd.accept("ingot/electrum",
                    ResourceLocation.fromNamespaceAndPath("more_ores_more_gems", "electrum_ingot"));

            // Aluminum (Immersive Engineering)
            safeAdd.accept("ingot/aluminum",
                    ResourceLocation.fromNamespaceAndPath("immersiveengineering", "ingot_aluminum"));

            // Aluminum (Thermal)
            safeAdd.accept("ingot/aluminum",
                    ResourceLocation.fromNamespaceAndPath("thermal", "aluminum_ingot"));
            // Add more categories as needed
            ArSimpleMachines.LOGGER.warn("ALUMINUM CATEGORY CONTENTS:");
            for (var item : CategoryRegistry.getItems("ingot/aluminum")) {
                var id = BuiltInRegistries.ITEM.getKey(item);
                ArSimpleMachines.LOGGER.warn(" - {} -> {}", item, id);
            }

            // -------------------------
            // EXISTING MULTIBLOCKS
            // -------------------------
            Object[][][] latheProjectorPattern = new Object[][][]{
                    { { 'C', 'M', null, 'O' } },
                    { { 'E', 'S', 'S', 'I' } }
            };

            Object[][][] rollingProjectorPattern = new Object[][][]{
                    {
                            { 'C', null, null, null },
                            { 'I', 'S', 'S', null },
                            { 'E', 'S', 'S', null }
                    },
                    {
                            { 'F', 'R', 'R', null },
                            { null, 'X', 'X', 'S' },
                            { null, 'S', 'S', 'O' }
                    }
            };

            Object[][][] chemicalReactorProjectorPattern = new Object[][][]{
                    {
                            { null, 'C', null },
                            { 'O', 'S', 'H' }
                    },
                    {
                            { 'E', 'M', 'E' },
                            { 'S', 'X', 'S' }
                    }
            };

            // -------------------------
            // NEW ELECTROLYZER MULTIBLOCK
            // -------------------------
            Object[][][] electrolyzerProjectorPattern = new Object[][][]{
                    {
                            { null, null, null },
                            { 'O', 'M', 'O' }
                    },
                    {
                            { 'E', 'C', 'E' },
                            { 'S', 'I', 'S' }
                    }
            };

            // -------------------------
            // ELECTRIC ARC FURNACE MULTIBLOCK
            // -------------------------
            Object[][][] electricArcFurnaceProjectorPattern = new Object[][][]{
                    // Layer 4
                    {
                            {null,null,null,null,null},
                            {null,'E','B','E',null},
                            {null,'B','B','B',null},
                            {null,'B','E','B',null},
                            {null,null,null,null,null}
                    },
                    // Layer 3
                    {
                            {null,'B','B','B',null},
                            {'B', 'X', null, 'X', 'B'},
                            {'B', null,  null,  null, 'B'},
                            {'B', null, 'X', null, 'B'},
                            {null,'B','B','B',null}
                    },


                    // Layer 2
                    {
                            {'B', 'B',  'B',  'B', 'B'},
                            {'B', null, null, null, 'B'},
                            {'B', null, null, null, 'B'},
                            {'B', null, null, null, 'B'},
                            {'B', 'B',  'B',  'B', 'B'}
                    },
                    // Layer 1
                    {
                            {'B', 'B',  'C',  'B', 'B'},
                            {'I', null, null, null, 'O'},
                            {'I', null, null, null, 'O'},
                            {'I', null, null, null, 'O'},
                            {'B', 'B',  'B',  'B', 'B'}
                    },

                    // Layer 0
                    {
                            {'B','B','B','B','B'},
                            {'B','B','B','B','B'},
                            {'B','B','B','B','B'},
                            {'B','B','B','B','B'},
                            {'B','B','B','B','B'}
                    }
            };

            Object[][][] crystallizerProjectorPattern = new Object[][][]{
                    {
                            {'S', 'S', 'S'},
                            {'S', 'S', 'S'}
                    },
                    {
                            {'O', 'C', 'I'},
                            {'V', 'E', 'F'}
                    }
            };



            // -------------------------
            // PROJECTOR MAPPINGS
            // -------------------------
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
                    'X', List.of(ARLibRegistry.BLOCK_COIL_COPPER.get()),
                    'R', List.of(ARLibRegistry.BLOCK_MOTOR.get()),
                    'C', List.of(ModBlocks.ROLLING_CONTROLLER.get())
            );

            Map<Character, List<net.minecraft.world.level.block.Block>> chemicalReactorProjectorMapping = Map.of(
                    'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
                    'S', List.of(ARLibRegistry.BLOCK_STRUCTURE.get()),
                    'H', List.of(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get()),
                    'O', List.of(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get()),
                    'X', List.of(ARLibRegistry.BLOCK_FLUID_OUTPUT_BLOCK.get()),
                    'M', List.of(ARLibRegistry.BLOCK_MOTOR.get()),
                    'C', List.of(ModBlocks.CHEMICAL_REACTOR_CONTROLLER.get())
            );

            // -------------------------
            // ELECTROLYZER MAPPING
            // -------------------------
            Map<Character, List<net.minecraft.world.level.block.Block>> electrolyzerProjectorMapping = Map.of(
                    'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
                    'S', List.of(ARLibRegistry.BLOCK_STRUCTURE.get()),
                    'I', List.of(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get()),   // single input
                    'O', List.of(ARLibRegistry.BLOCK_FLUID_OUTPUT_BLOCK.get()),  // output A
                    'X', List.of(ARLibRegistry.BLOCK_FLUID_OUTPUT_BLOCK.get()),  // output B
                    'M', List.of(ARLibRegistry.BLOCK_MOTOR.get()),
                    'C', List.of(ModBlocks.ELECTROLYZER_CONTROLLER.get())
            );

            Map<Character, List<net.minecraft.world.level.block.Block>> electricArcFurnaceProjectorMapping = Map.of(
                    //'B', List.of(ARLibRegistry.BLOCK_STRUCTURE.get()),
                    'B', List.of(ModBlocks.BLAST_BRICK.get()),
                    'I', List.of(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get()),
                    'O', List.of(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get()),
                    'X', List.of(ARLibRegistry.BLOCK_COIL_COPPER.get()),
                    'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
                    'C', List.of(ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get())
            );

            Map<Character, List<net.minecraft.world.level.block.Block>> crystallizerProjectorMapping = Map.of(
                    'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
                    'F', List.of(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get()),
                    'V', List.of(ARLibRegistry.BLOCK_FLUID_OUTPUT_BLOCK.get()),
                    'S', List.of(ARLibRegistry.BLOCK_STRUCTURE.get()),
                    'I', List.of(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get()),
                    'O', List.of(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get()),
                    'C', List.of(ModBlocks.CRYSTALLIZER_CONTROLLER.get())
            );

            // -------------------------
            // REGISTER ALL MULTIBLOCKS
            // -------------------------

            itemHoloProjector.registerMultiblock(
                    "Arc Furnace",
                    electricArcFurnaceProjectorPattern,
                    new HashMap<>(electricArcFurnaceProjectorMapping)
            );
            itemHoloProjector.registerMultiblock(
                    "Chemical Reactor",
                    chemicalReactorProjectorPattern,
                    new HashMap<>(chemicalReactorProjectorMapping)
            );

            // ⭐ NEW ELECTROLYZER ⭐
            itemHoloProjector.registerMultiblock(
                    "Electrolyzer",
                    electrolyzerProjectorPattern,
                    new HashMap<>(electrolyzerProjectorMapping)
            );
            itemHoloProjector.registerMultiblock(
                    "Lathe",
                    latheProjectorPattern,
                    new HashMap<>(latheProjectorMapping)
            );

            itemHoloProjector.registerMultiblock(
                    "Rolling Machine",
                    rollingProjectorPattern,
                    new HashMap<>(rollingProjectorMapping)
            );
            itemHoloProjector.registerMultiblock(
                    "Crystallizer",
                    crystallizerProjectorPattern,
                    new HashMap<>(crystallizerProjectorMapping)
            );


        });
    }
}
