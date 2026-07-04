package com.raven.arsimplemachines.recipe.electrolyzer;

import net.minecraft.world.item.crafting.RecipeType;

/**
 * Electrolyzer Recipe Type
 *
 * Registered in ModRecipeTypes and used by:
 *  - ElectrolyzerRecipe
 *  - ElectrolyzerRecipeSerializer
 *  - ElectrolyzerControllerBlockEntity
 */
public class ElectrolyzerRecipeType implements RecipeType<ElectrolyzerRecipe> {

    public static final ElectrolyzerRecipeType INSTANCE = new ElectrolyzerRecipeType();

    private ElectrolyzerRecipeType() {
        // Singleton
    }

    @Override
    public String toString() {
        return "arsimplemachines:electrolyzer";
    }
}
