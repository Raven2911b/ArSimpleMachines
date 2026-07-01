package com.raven.arsimplemachines.registry;

import com.raven.arsimplemachines.blockentity.GasChargePadBlockEntity;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ModCapabilities {

    public static void register(RegisterCapabilitiesEvent event) {

        // ---------------------------------------------------------
        // GAS CHARGE PAD (fluid handler)
        // ---------------------------------------------------------
        event.registerBlock(
                Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) ->
                        be instanceof GasChargePadBlockEntity pad ? pad.getFluidHandler() : null,
                ModBlocks.GAS_CHARGE_PAD.get()
        );
    }
}
