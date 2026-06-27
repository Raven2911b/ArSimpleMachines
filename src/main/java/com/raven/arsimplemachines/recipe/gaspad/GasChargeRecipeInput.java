package com.raven.arsimplemachines.recipe.gaspad;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.material.Fluid;

public class GasChargeRecipeInput implements RecipeInput {

    private final Fluid fluid;
    private final int amount;

    public GasChargeRecipeInput(Fluid fluid, int amount) {
        this.fluid = fluid;
        this.amount = amount;
    }

    public Fluid getFluid() {
        return fluid;
    }

    public int getAmount() {
        return amount;
    }

    // REQUIRED BY RecipeInput
    @Override
    public ItemStack getItem(int index) {
        // This machine does not use item inputs, so return EMPTY
        return new ItemStack(fluid.getBucket());
    }

    // REQUIRED BY RecipeInput
    @Override
    public int size() {
        // One conceptual “slot” for the fluid input
        return 1;
    }
}
