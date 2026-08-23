package com.raven.arsimplemachines.recipe.electrolyzer;

import com.raven.arsimplemachines.recipe.MachineRecipe;
import com.raven.arsimplemachines.recipe.MachineRecipeInput;
import com.raven.arsimplemachines.recipe.TagInput;
import com.raven.arsimplemachines.registry.ModRecipeTypes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/**
 * Unified Electrolyzer Recipe:
 *  - One fluid input
 *  - Two fluid outputs
 *  - No item inputs
 *  - No item outputs
 *  - No tag inputs
 */
public class ElectrolyzerRecipe extends MachineRecipe {

    public ElectrolyzerRecipe(
            ResourceLocation id,
            FluidStack input,
            FluidStack outputA,
            FluidStack outputB,
            int processingTime,
            int energyPerTick
    ) {
        super(
                id,

                // Item inputs
                List.of(),

                // Item tag inputs
                List.of(),

                // Fluid inputs
                List.of(input),

                // Item outputs
                List.of(),

                // Fluid outputs
                List.of(outputA, outputB),

                processingTime,
                energyPerTick
        );
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ELECTROLYZER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ELECTROLYZER_TYPE.get();
    }
}
