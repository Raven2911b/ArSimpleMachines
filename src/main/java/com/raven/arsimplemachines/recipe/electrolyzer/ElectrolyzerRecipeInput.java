package com.raven.arsimplemachines.recipe.electrolyzer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Input wrapper for Electrolyzer recipes.
 * NeoForge requires a RecipeInput implementation for matching.
 *
 * This wraps ONE fluid stack:
 *  - input (the electrolyzer's single input tank)
 */
public class ElectrolyzerRecipeInput implements RecipeInput {

    private final FluidStack input;

    public ElectrolyzerRecipeInput(FluidStack input) {
        this.input = input;
    }

    public FluidStack getInput() {
        return input;
    }

    @Override
    public int size() {
        // One fluid input
        return 1;
    }

    @Override
    public ItemStack getItem(int slot) {
        // Electrolyzer uses fluids, not items.
        // NeoForge requires this method, so return EMPTY.
        return ItemStack.EMPTY;
    }
}
