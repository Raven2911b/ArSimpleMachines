package com.raven.arsimplemachines.recipe.chemical;

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
 * Chemical Reactor Recipe:
 *  - Two fluid inputs (A + B)
 *  - One fluid output
 *  - Processing time
 *  - Energy per tick
 */
public class ChemicalReactorRecipe implements Recipe<ChemicalReactorRecipeInput> {

    private final ResourceLocation id;

    private final FluidStack fluidA;
    private final FluidStack fluidB;
    private final FluidStack output;

    private final int processingTime;
    private final int energyPerTick;

    public ChemicalReactorRecipe(
            ResourceLocation id,
            FluidStack fluidA,
            FluidStack fluidB,
            FluidStack output,
            int processingTime,
            int energyPerTick
    ) {
        this.id = id;
        this.fluidA = fluidA;
        this.fluidB = fluidB;
        this.output = output;
        this.processingTime = processingTime;
        this.energyPerTick = energyPerTick;
    }

    // ---------------------------------------------------------
    //  GETTERS
    // ---------------------------------------------------------
    public FluidStack getFluidA() { return fluidA.copy(); }
    public FluidStack getFluidB() { return fluidB.copy(); }
    public FluidStack getOutput() { return output.copy(); }

    public int getProcessingTime() { return processingTime; }
    public int getEnergyPerTick() { return energyPerTick; }

    // ---------------------------------------------------------
    //  MATCHING LOGIC
    // ---------------------------------------------------------
    @Override
    public boolean matches(ChemicalReactorRecipeInput input, Level level) {

        FluidStack inA = input.getFluidA();
        FluidStack inB = input.getFluidB();

        boolean normal =
                inA.getFluid() == fluidA.getFluid() &&
                        inB.getFluid() == fluidB.getFluid() &&
                        inA.getAmount() >= fluidA.getAmount() &&
                        inB.getAmount() >= fluidB.getAmount();

        boolean swapped =
                inA.getFluid() == fluidB.getFluid() &&
                        inB.getFluid() == fluidA.getFluid() &&
                        inA.getAmount() >= fluidB.getAmount() &&
                        inB.getAmount() >= fluidA.getAmount();

        return normal || swapped;
    }

    // ---------------------------------------------------------
    //  CONSUMPTION LOGIC
    // ---------------------------------------------------------
    public boolean canConsume(FluidStack inA, FluidStack inB) {

        boolean normal =
                inA.getFluid() == fluidA.getFluid() &&
                        inB.getFluid() == fluidB.getFluid() &&
                        inA.getAmount() >= fluidA.getAmount() &&
                        inB.getAmount() >= fluidB.getAmount();

        boolean swapped =
                inA.getFluid() == fluidB.getFluid() &&
                        inB.getFluid() == fluidA.getFluid() &&
                        inA.getAmount() >= fluidB.getAmount() &&
                        inB.getAmount() >= fluidA.getAmount();

        return normal || swapped;
    }

    public void consumeInputs(IFluidHandler tankA, IFluidHandler tankB) {

        FluidStack a = tankA.getFluidInTank(0);
        FluidStack b = tankB.getFluidInTank(0);

        boolean normal =
                a.getFluid() == fluidA.getFluid() &&
                        b.getFluid() == fluidB.getFluid();

        if (normal) {
            tankA.drain(fluidA.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            tankB.drain(fluidB.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            return;
        }

        boolean swapped =
                a.getFluid() == fluidB.getFluid() &&
                        b.getFluid() == fluidA.getFluid();

        if (swapped) {
            tankA.drain(fluidB.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            tankB.drain(fluidA.getAmount(), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    // ---------------------------------------------------------
    //  REQUIRED OVERRIDES
    // ---------------------------------------------------------
    @Override
    public ItemStack assemble(ChemicalReactorRecipeInput input, HolderLookup.Provider provider) {
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

    //@Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.CHEMICAL_REACTOR_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.CHEMICAL_REACTOR_TYPE.get();
    }
}
