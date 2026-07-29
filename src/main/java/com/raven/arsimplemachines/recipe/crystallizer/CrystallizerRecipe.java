package com.raven.arsimplemachines.recipe.crystallizer;

import com.raven.arsimplemachines.recipe.CategoryInput;
import com.raven.arsimplemachines.recipe.MachineRecipe;
import com.raven.arsimplemachines.recipe.MachineRecipeInput;
import com.raven.arsimplemachines.registry.ModRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class CrystallizerRecipe extends MachineRecipe {

    public CrystallizerRecipe(ResourceLocation id,
                              List<ItemStack> itemInputs,
                              List<CategoryInput> itemCategories,
                              List<FluidStack> fluidInputs,
                              List<ItemStack> itemOutputs,
                              List<FluidStack> fluidOutputs,
                              int processingTime,
                              int energyPerTick) {

        super(id, itemInputs, itemCategories, fluidInputs, itemOutputs, fluidOutputs, processingTime, energyPerTick);
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.CRYSTALLIZER_SERIALIZER;
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeType<?> getType() {
        return ModRecipeTypes.CRYSTALLIZER_TYPE;
    }

    @Override
    public boolean matches(MachineRecipeInput input, net.minecraft.world.level.Level level) {
        return com.raven.arsimplemachines.recipe.MachineRecipeMatcher.matches(
                this,
                input.getItems(),
                input.getFluids(),
                level
        );
    }
}
