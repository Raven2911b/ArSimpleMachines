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
            //CategoryRegistry.add("ingot/electrum", ModItems.ELECTRUM_INGOT.get());


// Immersive Engineering electrum
            CategoryRegistry.add("ingot/electrum",
                    BuiltInRegistries.ITEM.get(
                            ResourceLocation.fromNamespaceAndPath("immersiveengineering", "ingot_electrum")
                    )
            );

// Thermal electrum
            CategoryRegistry.add("ingot/electrum",
                    BuiltInRegistries.ITEM.get(
                            ResourceLocation.fromNamespaceAndPath("thermal", "electrum_ingot")
                    )
            );

// More Ores & Gems electrum
            CategoryRegistry.add("ingot/electrum",
                    BuiltInRegistries.ITEM.get(
                            ResourceLocation.fromNamespaceAndPath("more_ores_more_gems", "electrum_ingot")
                    )
            );

            CategoryRegistry.add("ingot/aluminum", BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("immersiveengineering", "ingot_aluminum")
            ));

            CategoryRegistry.add("ingot/aluminum", BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("thermal", "aluminum_ingot")
            ));



            // CategoryRegistry.add("plate/steel", ModItems.STEEL_PLATE.get());
            //CategoryRegistry.add("dust/copper", ModItems.COPPER_DUST.get());
            // Add more categories as needed
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

            // -------------------------
            // REGISTER ALL MULTIBLOCKS
            // -------------------------
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
        });
    }
}
