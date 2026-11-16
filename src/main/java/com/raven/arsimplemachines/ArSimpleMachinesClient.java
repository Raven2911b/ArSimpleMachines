package com.raven.arsimplemachines;

import com.raven.arsimplemachines.blockentityrenderers.ChemicalReactorRenderer;
import com.raven.arsimplemachines.blockentityrenderers.CrystallizerRenderer;
import com.raven.arsimplemachines.blockentityrenderers.CuttingMachineRenderer;
import com.raven.arsimplemachines.blockentityrenderers.ElectrolyzerRenderer;
import com.raven.arsimplemachines.blockentityrenderers.LatheRenderer;
import com.raven.arsimplemachines.blockentityrenderers.PrecisionAssemblerRenderer;
import com.raven.arsimplemachines.blockentityrenderers.RollingRenderer;
import com.raven.arsimplemachines.blockentityrenderers.FluidConverterRenderer;
import com.raven.arsimplemachines.hud.SpaceSuitHud;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(
        modid = ArSimpleMachines.MODID,
        value = Dist.CLIENT
)
public class ArSimpleMachinesClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(ModBlockEntities.LATHE_CONTROLLER.get(), LatheRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.ROLLING_CONTROLLER.get(), RollingRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.CHEMICAL_REACTOR_CONTROLLER.get(), ChemicalReactorRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.ELECTROLYZER_CONTROLLER.get(), ElectrolyzerRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.CRYSTALLIZER_CONTROLLER.get(), CrystallizerRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.CUTTING_MACHINE_CONTROLLER.get(), CuttingMachineRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.PRECISION_ASSEMBLER_CONTROLLER.get(), PrecisionAssemblerRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.FLUID_CONVERTER_CONTROLLER.get(), FluidConverterRenderer::new);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "space_suit_hud"),
                SpaceSuitHud::render
        );
    }
}