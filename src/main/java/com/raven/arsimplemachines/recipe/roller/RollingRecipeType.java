package com.raven.arsimplemachines.recipe.roller;

import net.minecraft.world.item.crafting.RecipeType;

public class RollingRecipeType implements RecipeType<RollingRecipe> {

    public static final RollingRecipeType INSTANCE = new RollingRecipeType();

    private RollingRecipeType() {
        // Singleton
    }

    @Override
    public String toString() {
        return "arsimplemachines:rolling";
    }
}
