package com.raven.arsimplemachines.registry;

import com.raven.arsimplemachines.blockentity.GasChargePadBlockEntity;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class ModCapabilities {

    public static void register(RegisterCapabilitiesEvent event) {

        event.registerBlock(
                Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) ->
                        be instanceof GasChargePadBlockEntity pad ? pad.getFluidHandler() : null,
                ModBlocks.GAS_CHARGE_PAD.get()
        );
    }
}
