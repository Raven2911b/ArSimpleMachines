package com.raven.arsimplemachines.recipe.precision;

import com.raven.arsimplemachines.recipe.MachineRecipe;
import com.raven.arsimplemachines.recipe.TagInput;
import com.raven.arsimplemachines.registry.ModRecipeTypes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

/**
 * Precision Assembler recipe using unified MachineRecipe:
 * - itemInputs
 * - itemTags (for tag-based inputs)
 * - no fluids
 * - itemOutputs
 */
public class PrecisionAssemblerRecipe extends MachineRecipe {

    public PrecisionAssemblerRecipe(
            ResourceLocation id,
            List<ItemStack> itemInputs,
            List<TagInput> itemTags,
            List<ItemStack> itemOutputs,
            int processingTime,
            int energyPerTick
    ) {
        super(
                id,
                itemInputs,
                itemTags,
                List.of(),      // fluidInputs
                itemOutputs,
                List.of(),      // fluidOutputs
                processingTime,
                energyPerTick
        );
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.PRECISION_ASSEMBLER_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.PRECISION_ASSEMBLER_TYPE;
    }
}
