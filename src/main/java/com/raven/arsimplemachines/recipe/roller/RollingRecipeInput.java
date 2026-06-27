package com.raven.arsimplemachines.recipe.roller;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public class RollingRecipeInput implements RecipeInput {

    private final ItemStack stack;

    public RollingRecipeInput(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack getItem(int slot) {
        // Rolling machine only has ONE input item
        return stack;
    }

    @Override
    public int size() {
        return 1;
    }
}
