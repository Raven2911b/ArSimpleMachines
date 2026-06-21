package com.raven.arsimplemachines.menu;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.SlotItemHandler;

public class FluidOutputSlot extends SlotItemHandler {

    public FluidOutputSlot(net.neoforged.neoforge.items.IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // Players cannot insert anything into the output slot
        return false;
    }

    @Override
    public boolean mayPickup(net.minecraft.world.entity.player.Player player) {
        // Player can take empty buckets
        return true;
    }
}
