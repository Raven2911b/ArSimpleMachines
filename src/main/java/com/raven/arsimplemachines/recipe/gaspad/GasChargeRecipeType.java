package com.raven.arsimplemachines.recipe.gaspad;

import com.raven.arsimplemachines.ArSimpleMachines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

public class GasChargeRecipeType implements RecipeType<GasChargeRecipe> {

    public static final GasChargeRecipeType INSTANCE = new GasChargeRecipeType();
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "gas_charge");

    @Override
    public String toString() {
        return ID.toString();
    }
}
