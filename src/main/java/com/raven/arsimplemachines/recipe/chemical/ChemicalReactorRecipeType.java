package com.raven.arsimplemachines.recipe.chemical;

import net.minecraft.world.item.crafting.RecipeType;

/**
 * Chemical Reactor Recipe Type
 *
 * Registered in ModRecipeTypes and used by:
 *  - ChemicalReactorRecipe
 *  - ChemicalReactorRecipeSerializer
 *  - ChemicalReactorControllerBlockEntity
 */
public class ChemicalReactorRecipeType implements RecipeType<com.raven.arsimplemachines.recipe.chemical.ChemicalReactorRecipe> {

    public static final ChemicalReactorRecipeType INSTANCE = new ChemicalReactorRecipeType();

    private ChemicalReactorRecipeType() {
        // Singleton
    }

    @Override
    public String toString() {
        return "arsimplemachines:chemical";
    }
}
