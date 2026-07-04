package com.raven.arsimplemachines.recipe.electrolyzer;

import com.raven.arsimplemachines.registry.ModRecipeTypes;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import net.minecraft.world.level.Level;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Electrolyzer Recipe:
 *  - One fluid input
 *  - Two fluid outputs
 *  - Processing time
 *  - Energy per tick
 */
public class ElectrolyzerRecipe implements Recipe<ElectrolyzerRecipeInput> {

    private final ResourceLocation id;

    private final FluidStack input;
    private final FluidStack outputA;
    private final FluidStack outputB;

    private final int processingTime;
    private final int energyPerTick;

    public ElectrolyzerRecipe(
            ResourceLocation id,
            FluidStack input,
            FluidStack outputA,
            FluidStack outputB,
            int processingTime,
            int energyPerTick
    ) {
        this.id = id;
        this.input = input;
        this.outputA = outputA;
        this.outputB = outputB;
        this.processingTime = processingTime;
        this.energyPerTick = energyPerTick;
    }

    // ---------------------------------------------------------
    //  GETTERS
    // ---------------------------------------------------------
    public FluidStack getInput() { return input.copy(); }
    public FluidStack getOutputA() { return outputA.copy(); }
    public FluidStack getOutputB() { return outputB.copy(); }

    public int getProcessingTime() { return processingTime; }
    public int getEnergyPerTick() { return energyPerTick; }

    // ---------------------------------------------------------
    //  MATCHING LOGIC
    // ---------------------------------------------------------
    @Override
    public boolean matches(ElectrolyzerRecipeInput inputData, Level level) {

        FluidStack in = inputData.getInput();

        return in.getFluid() == input.getFluid() &&
                in.getAmount() >= input.getAmount();
    }

    // ---------------------------------------------------------
    //  CONSUMPTION LOGIC
    // ---------------------------------------------------------
    public boolean canConsume(FluidStack in) {
        return in.getFluid() == input.getFluid() &&
                in.getAmount() >= input.getAmount();
    }

    public void consumeInputs(IFluidHandler tankInput) {
        tankInput.drain(input.getAmount(), IFluidHandler.FluidAction.EXECUTE);
    }

    // ---------------------------------------------------------
    //  REQUIRED OVERRIDES
    // ---------------------------------------------------------
    @Override
    public ItemStack assemble(ElectrolyzerRecipeInput input, HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    public ResourceLocation getId() {
        return id;
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
