package com.raven.arsimplemachines.recipe.eaf;

import com.raven.arsimplemachines.ArSimpleMachines;
import net.minecraft.world.item.crafting.RecipeType;

public class ElectricArcFurnaceRecipeType implements RecipeType<ElectricArcFurnaceRecipe> {

    public static final ElectricArcFurnaceRecipeType INSTANCE = new ElectricArcFurnaceRecipeType();

    @Override
    public String toString() {
        return ArSimpleMachines.MODID + ":electric_arc_furnace";
    }
}
