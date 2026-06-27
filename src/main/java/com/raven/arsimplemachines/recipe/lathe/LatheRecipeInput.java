package com.raven.arsimplemachines.recipe.lathe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public class LatheRecipeInput implements RecipeInput {

    private final ItemStack stack;

    public LatheRecipeInput(ItemStack stack) {
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
