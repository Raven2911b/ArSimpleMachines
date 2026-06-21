package com.raven.arsimplemachines.recipe;

import com.raven.arsimplemachines.registry.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

public class GasChargeRecipe implements Recipe<GasChargeRecipeInput> {

    private final ResourceLocation id;
    private final Fluid fluid;
    private final int fluidAmount;
    private final int processingTime;

    public GasChargeRecipe(ResourceLocation id, Fluid fluid, int fluidAmount, int processingTime) {
        this.id = id;
        this.fluid = fluid;
        this.fluidAmount = fluidAmount;
        this.processingTime = processingTime;
    }

    // helper, NOT part of Recipe interface
    public ResourceLocation getId() {
        return id;
    }

    public Fluid getFluid() {
        return fluid;
    }

    public int getFluidAmount() {
        return fluidAmount;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    @Override
    public boolean matches(GasChargeRecipeInput input, Level level) {
        return input.getFluid().isSame(fluid) && input.getAmount() >= fluidAmount;
    }

    @Override
    public ItemStack assemble(GasChargeRecipeInput input, HolderLookup.Provider provider) {
        // machine produces no item output
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        // JEI / book display result; still empty for this machine
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.GAS_CHARGE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.GAS_CHARGE_TYPE.get();
    }
}
