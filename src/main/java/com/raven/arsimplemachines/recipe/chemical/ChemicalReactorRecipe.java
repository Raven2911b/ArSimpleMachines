package com.raven.arsimplemachines.recipe.chemical;

import com.raven.arsimplemachines.recipe.MachineRecipe;
import com.raven.arsimplemachines.recipe.MachineRecipeInput;
import com.raven.arsimplemachines.registry.ModRecipeTypes;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/**
 * Unified Chemical Reactor Recipe:
 *  - Two fluid inputs (A + B)
 *  - Optional fluid tag inputs (A + B)
 *  - One fluid output
 *  - Processing time
 *  - Energy per tick
 *
 * This class now extends MachineRecipe so the controller can use
 * MachineRecipeMatcher + MachineRecipeInput, while still exposing
 * fluidA/fluidB + tag lists for tag-aware matching.
 */
public class ChemicalReactorRecipe extends MachineRecipe {

    private final ResourceLocation id;

    // Direct fluid inputs (for tag-aware matching)
    private final FluidStack fluidA;
    private final FluidStack fluidB;

    // Tag-based fluid inputs (special-case for Chemical Reactor)
    private final List<FluidTagInput> fluidATags;
    private final List<FluidTagInput> fluidBTags;

    // Unified fields inherited from MachineRecipe:
    private final int processingTime;
    private final int energyPerTick;

    public ChemicalReactorRecipe(
            ResourceLocation id,
            FluidStack fluidA,
            FluidStack fluidB,
            FluidStack output,
            List<FluidTagInput> fluidATags,
            List<FluidTagInput> fluidBTags,
            int processingTime,
            int energyPerTick
    ) {
        super(
                id,
                List.of(),                                // itemInputs
                List.of(),                                // itemTags
                List.of(fluidA.copy(), fluidB.copy()),    // fluidInputs
                List.of(),                                // itemOutputs
                List.of(output.copy()),                   // fluidOutputs
                processingTime,
                energyPerTick
        );

        this.id = id;

        this.fluidA = fluidA.copy();
        this.fluidB = fluidB.copy();
        this.fluidATags = fluidATags;
        this.fluidBTags = fluidBTags;

        this.processingTime = processingTime;
        this.energyPerTick = energyPerTick;
    }

    /**
     * Fluid tag input (Chemical-Reactor-only feature)
     */
    public static class FluidTagInput {
        private final ResourceLocation tag;
        private final int amount;

        public FluidTagInput(ResourceLocation tag, int amount) {
            this.tag = tag;
            this.amount = amount;
        }

        public ResourceLocation tag() { return tag; }
        public int amount() { return amount; }
    }

    // ---------------------------------------------------------------------
    // GETTERS (used by controller for tag-aware matching)
    // ---------------------------------------------------------------------

    public FluidStack getFluidA() { return fluidA.copy(); }
    public FluidStack getFluidB() { return fluidB.copy(); }

    public List<FluidTagInput> getFluidATags() { return fluidATags; }
    public List<FluidTagInput> getFluidBTags() { return fluidBTags; }

    public FluidStack getOutput() {
        return getFluidOutputs().isEmpty()
                ? FluidStack.EMPTY
                : getFluidOutputs().get(0).copy();
    }

    public int getProcessingTime() { return processingTime; }
    public int getEnergyPerTick() { return energyPerTick; }

    // ---------------------------------------------------------------------
    // REQUIRED OVERRIDES
    // ---------------------------------------------------------------------

    @Override
    public ItemStack assemble(MachineRecipeInput input, HolderLookup.Provider provider) {
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
