package com.raven.arsimplemachines.recipe.chemical;

import com.raven.arsimplemachines.registry.ModRecipeTypes;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import net.minecraft.world.level.Level;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

/**
 * Chemical Reactor Recipe:
 *  - Two fluid inputs (A + B)
 *  - Optional fluid tag inputs (A + B)
 *  - One fluid output
 *  - Processing time
 *  - Energy per tick
 */
public class ChemicalReactorRecipe implements Recipe<ChemicalReactorRecipeInput> {

    private final ResourceLocation id;

    private final FluidStack fluidA;
    private final FluidStack fluidB;
    private final FluidStack output;

    private final List<ChemicalReactorRecipeInput.FluidTagInput> fluidATags;
    private final List<ChemicalReactorRecipeInput.FluidTagInput> fluidBTags;

    private final int processingTime;
    private final int energyPerTick;

    public ChemicalReactorRecipe(
            ResourceLocation id,
            FluidStack fluidA,
            FluidStack fluidB,
            FluidStack output,
            List<ChemicalReactorRecipeInput.FluidTagInput> fluidATags,
            List<ChemicalReactorRecipeInput.FluidTagInput> fluidBTags,
            int processingTime,
            int energyPerTick
    ) {
        this.id = id;
        this.fluidA = fluidA;
        this.fluidB = fluidB;
        this.output = output;
        this.fluidATags = fluidATags;
        this.fluidBTags = fluidBTags;
        this.processingTime = processingTime;
        this.energyPerTick = energyPerTick;
    }

    // ---------------------------------------------------------
    //  GETTERS
    // ---------------------------------------------------------
    public FluidStack getFluidA() { return fluidA.copy(); }
    public FluidStack getFluidB() { return fluidB.copy(); }
    public FluidStack getOutput() { return output.copy(); }

    public List<ChemicalReactorRecipeInput.FluidTagInput> getFluidATags() { return fluidATags; }
    public List<ChemicalReactorRecipeInput.FluidTagInput> getFluidBTags() { return fluidBTags; }

    public int getProcessingTime() { return processingTime; }
    public int getEnergyPerTick() { return energyPerTick; }

    // ---------------------------------------------------------
    //  MATCHING LOGIC (supports direct fluids + tags)
    // ---------------------------------------------------------
    @Override
    public boolean matches(ChemicalReactorRecipeInput input, Level level) {

        FluidStack inA = input.getFluidA();
        FluidStack inB = input.getFluidB();

        boolean matchA = matchesSingle(inA, fluidA, fluidATags);
        boolean matchB = matchesSingle(inB, fluidB, fluidBTags);

        boolean matchA_swapped = matchesSingle(inA, fluidB, fluidBTags);
        boolean matchB_swapped = matchesSingle(inB, fluidA, fluidATags);

        return (matchA && matchB) || (matchA_swapped && matchB_swapped);
    }

    private boolean matchesSingle(FluidStack input, FluidStack direct, List<ChemicalReactorRecipeInput.FluidTagInput> tags) {

        if (input.isEmpty()) return false;

        // Direct match
        if (direct != null &&
                input.getFluid() == direct.getFluid() &&
                input.getAmount() >= direct.getAmount())
            return true;

        // Tag match
        for (var tag : tags) {
            TagKey<Fluid> key = TagKey.create(net.minecraft.core.registries.BuiltInRegistries.FLUID.key(), tag.tag());
            if (input.is(key) && input.getAmount() >= tag.amount())
                return true;
        }

        return false;
    }

    // ---------------------------------------------------------
    //  CONSUMPTION LOGIC (supports direct fluids + tags)
    // ---------------------------------------------------------
    public boolean canConsume(FluidStack inA, FluidStack inB) {
        boolean normal =
                matchesSingle(inA, fluidA, fluidATags) &&
                        matchesSingle(inB, fluidB, fluidBTags);

        boolean swapped =
                matchesSingle(inA, fluidB, fluidBTags) &&
                        matchesSingle(inB, fluidA, fluidATags);

        return normal || swapped;
    }

    public void consumeInputs(IFluidHandler tankA, IFluidHandler tankB) {

        FluidStack a = tankA.getFluidInTank(0);
        FluidStack b = tankB.getFluidInTank(0);

        boolean normal =
                matchesSingle(a, fluidA, fluidATags) &&
                        matchesSingle(b, fluidB, fluidBTags);

        boolean swapped =
                matchesSingle(a, fluidB, fluidBTags) &&
                        matchesSingle(b, fluidA, fluidATags);

        if (normal) {
            drainMatching(tankA, fluidA, fluidATags);
            drainMatching(tankB, fluidB, fluidBTags);
        } else if (swapped) {
            drainMatching(tankA, fluidB, fluidBTags);
            drainMatching(tankB, fluidA, fluidATags);
        }
    }

    private void drainMatching(IFluidHandler tank, FluidStack direct, List<ChemicalReactorRecipeInput.FluidTagInput> tags) {

        FluidStack fs = tank.getFluidInTank(0);
        if (fs.isEmpty()) return;

        boolean matchesDirect = direct != null && fs.getFluid() == direct.getFluid();
        boolean matchesTag = false;

        for (var tag : tags) {
            TagKey<Fluid> key = TagKey.create(net.minecraft.core.registries.BuiltInRegistries.FLUID.key(), tag.tag());
            if (fs.is(key)) matchesTag = true;
        }

        if (matchesDirect) {
            tank.drain(direct.getAmount(), IFluidHandler.FluidAction.EXECUTE);
        } else if (matchesTag) {
            int amount = tags.get(0).amount();
            tank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
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
