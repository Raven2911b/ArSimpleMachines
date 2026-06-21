package com.raven.arsimplemachines.menu;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.fluids.FluidUtil;

public class FluidInputSlot extends SlotItemHandler {

    public FluidInputSlot(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        boolean result = stack.getItem() instanceof net.minecraft.world.item.BucketItem;
        return result;
    }


}

