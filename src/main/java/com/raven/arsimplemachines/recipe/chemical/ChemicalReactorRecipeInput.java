package com.raven.arsimplemachines.recipe.chemical;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/**
 * Input wrapper for Chemical Reactor recipes.
 * NeoForge requires a RecipeInput implementation for matching.
 *
 * This wraps TWO fluid stacks:
 *  - fluidA (Hydrogen tank)
 *  - fluidB (Oxygen tank)
 *
 * And optional fluid tag inputs.
 */
public class ChemicalReactorRecipeInput implements RecipeInput {

    // ---------------------------------------------------------
    //  TAG INPUT RECORD
    // ---------------------------------------------------------
    public record FluidTagInput(ResourceLocation tag, int amount) {}

    private final FluidStack fluidA;
    private final FluidStack fluidB;

    private final List<FluidTagInput> fluidATags;
    private final List<FluidTagInput> fluidBTags;

    // ---------------------------------------------------------
    //  CONSTRUCTORS
    // ---------------------------------------------------------

    // Direct fluids only
    public ChemicalReactorRecipeInput(FluidStack fluidA, FluidStack fluidB) {
        this(fluidA, fluidB, List.of(), List.of());
    }

    // Direct fluids + tag inputs
    public ChemicalReactorRecipeInput(
            FluidStack fluidA,
            FluidStack fluidB,
            List<FluidTagInput> fluidATags,
            List<FluidTagInput> fluidBTags
    ) {
        this.fluidA = fluidA;
        this.fluidB = fluidB;
        this.fluidATags = fluidATags;
        this.fluidBTags = fluidBTags;
    }

    // ---------------------------------------------------------
    //  GETTERS
    // ---------------------------------------------------------
    public FluidStack getFluidA() { return fluidA; }
    public FluidStack getFluidB() { return fluidB; }

    public List<FluidTagInput> getFluidATags() { return fluidATags; }
    public List<FluidTagInput> getFluidBTags() { return fluidBTags; }

    // ---------------------------------------------------------
    //  REQUIRED OVERRIDES
    // ---------------------------------------------------------
    @Override
    public int size() {
        // Two fluid inputs
        return 2;
    }

    @Override
    public ItemStack getItem(int slot) {
        // Chemical Reactor uses fluids, not items.
        // NeoForge requires this method, so return EMPTY.
        return ItemStack.EMPTY;
    }
}
