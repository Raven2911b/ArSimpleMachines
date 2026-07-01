package com.raven.arsimplemachines.recipe.chemical;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Input wrapper for Chemical Reactor recipes.
 * NeoForge requires a RecipeInput implementation for matching.
 *
 * This wraps TWO fluid stacks:
 *  - fluidA (Hydrogen tank)
 *  - fluidB (Oxygen tank)
 */
public class ChemicalReactorRecipeInput implements RecipeInput {

    private final FluidStack fluidA;
    private final FluidStack fluidB;

    public ChemicalReactorRecipeInput(FluidStack fluidA, FluidStack fluidB) {
        this.fluidA = fluidA;
        this.fluidB = fluidB;
    }

    public FluidStack getFluidA() {
        return fluidA;
    }

    public FluidStack getFluidB() {
        return fluidB;
    }

    @Override
    public int size() {
        // Two fluid inputs
        return 2;
    }

    @Override
    public ItemStack getItem(int slot) {
        // Chemical Reactor uses fluids, not items.
        // NeoForge requires this method, so return EMPTY.
        return ItemStack.EMPTY;
    }
}
