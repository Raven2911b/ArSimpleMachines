package com.raven.arsimplemachines.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public abstract class MachineRecipe implements Recipe<MachineRecipeInput> {

    protected final ResourceLocation id;

    protected final List<ItemStack> itemInputs;
    protected final List<CategoryInput> itemCategories;   // UPDATED
    protected final List<FluidStack> fluidInputs;

    protected final List<ItemStack> itemOutputs;
    protected final List<FluidStack> fluidOutputs;

    protected final int processingTime;
    protected final int energyPerTick;

    public MachineRecipe(ResourceLocation id,
                         List<ItemStack> itemInputs,
                         List<CategoryInput> itemCategories,   // UPDATED
                         List<FluidStack> fluidInputs,
                         List<ItemStack> itemOutputs,
                         List<FluidStack> fluidOutputs,
                         int processingTime,
                         int energyPerTick) {

        this.id = id;
        this.itemInputs = itemInputs;
        this.itemCategories = itemCategories;   // UPDATED
        this.fluidInputs = fluidInputs;
        this.itemOutputs = itemOutputs;
        this.fluidOutputs = fluidOutputs;
        this.processingTime = processingTime;
        this.energyPerTick = energyPerTick;
    }

    @Override
    public boolean matches(MachineRecipeInput input, Level level) {
        return MachineRecipeMatcher.matches(this, input.getItems(), input.getFluids(), level);
    }

    @Override
    public ItemStack assemble(MachineRecipeInput input, HolderLookup.Provider provider) {
        return itemOutputs.isEmpty() ? ItemStack.EMPTY : itemOutputs.get(0).copy();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return itemOutputs.isEmpty() ? ItemStack.EMPTY : itemOutputs.get(0).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public abstract net.minecraft.world.item.crafting.RecipeSerializer<?> getSerializer();

    @Override
    public abstract net.minecraft.world.item.crafting.RecipeType<?> getType();

    // ---------------------------------------------------------
    // ACCESSORS
    // ---------------------------------------------------------

    public ResourceLocation getId() {
        return id;
    }

    public List<ItemStack> getItemInputs() { return itemInputs; }
    public List<CategoryInput> getItemCategories() { return itemCategories; }   // UPDATED
    public List<FluidStack> getFluidInputs() { return fluidInputs; }
    public List<ItemStack> getItemOutputs() { return itemOutputs; }
    public List<FluidStack> getFluidOutputs() { return fluidOutputs; }
    public int getProcessingTime() { return processingTime; }
    public int getEnergyPerTick() { return energyPerTick; }
}
