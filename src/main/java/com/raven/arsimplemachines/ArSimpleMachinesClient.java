package com.raven.arsimplemachines;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

import com.raven.arsimplemachines.blockentityrenderers.LatheRenderer;
import com.raven.arsimplemachines.blockentityrenderers.RollingRenderer;
import com.raven.arsimplemachines.blockentityrenderers.ChemicalReactorRenderer;
import com.raven.arsimplemachines.registry.ModBlockEntities;

@EventBusSubscriber(
        modid = ArSimpleMachines.MODID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD
)
public class ArSimpleMachinesClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

        BlockEntityRenderers.register(
                ModBlockEntities.LATHE_CONTROLLER.get(),
                LatheRenderer::new
        );

        BlockEntityRenderers.register(
                ModBlockEntities.ROLLING_CONTROLLER.get(),
                RollingRenderer::new
        );

        // ---------------------------------------------------------
        // CHEMICAL REACTOR RENDERER
        // ---------------------------------------------------------
        BlockEntityRenderers.register(
                ModBlockEntities.CHEMICAL_REACTOR_CONTROLLER.get(),
                ChemicalReactorRenderer::new
        );
    }
}
