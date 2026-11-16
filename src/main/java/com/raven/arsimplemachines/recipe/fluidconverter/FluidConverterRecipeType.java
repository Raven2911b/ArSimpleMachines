package com.raven.arsimplemachines.recipe.fluidconverter;

import net.minecraft.world.item.crafting.RecipeType;

public class FluidConverterRecipeType implements RecipeType<FluidConverterRecipe>{

        public static final FluidConverterRecipeType INSTANCE = new FluidConverterRecipeType();

        public FluidConverterRecipeType() {}

        @Override
        public String toString() {
            return "arsimplemachines:fluid_converter";
        }
    }




