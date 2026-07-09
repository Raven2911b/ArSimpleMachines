package com.raven.arsimplemachines.recipe;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.ArrayList;
import java.util.List;

public class MachineRecipeInput implements RecipeInput {

    private final List<ItemStack> items = new ArrayList<>();
    private final List<FluidStack> fluids = new ArrayList<>();

    public void addItem(ItemStack stack) {
        if (!stack.isEmpty()) items.add(stack.copy());
    }

    public void addFluid(FluidStack stack) {
        if (!stack.isEmpty()) fluids.add(stack.copy());
    }

    public List<ItemStack> getItems() { return items; }
    public List<FluidStack> getFluids() { return fluids; }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public ItemStack getItem(int index) {
        return index >= 0 && index < items.size() ? items.get(index) : ItemStack.EMPTY;
    }
}
