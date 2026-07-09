package com.raven.arsimplemachines.util;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.item.ItemStack;

public class FullStackItemHandler implements IItemHandler {

    private final IItemHandler inner;

    public FullStackItemHandler(IItemHandler inner) {
        this.inner = inner;
    }

    @Override
    public int getSlots() {
        return inner.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inner.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return inner.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return inner.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64; // override ARLib’s limit
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return inner.isItemValid(slot, stack);
    }
}
