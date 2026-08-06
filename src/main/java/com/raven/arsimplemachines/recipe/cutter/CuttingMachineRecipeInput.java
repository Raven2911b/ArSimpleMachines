package com.raven.arsimplemachines.recipe.cutter;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public class CuttingMachineRecipeInput implements RecipeInput {

    private final ItemStack stack;

    public CuttingMachineRecipeInput(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack getItem(int index) {
        return stack;
    }

    @Override
    public int size() {
        return 1;
    }

    public ItemStack getStack() {
        return stack;
    }
}
