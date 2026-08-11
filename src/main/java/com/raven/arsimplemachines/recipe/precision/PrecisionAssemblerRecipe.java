package com.raven.arsimplemachines.recipe.precision;

import com.raven.arsimplemachines.registry.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public class PrecisionAssemblerRecipe implements Recipe<PrecisionAssemblerRecipeInput> {

    private final ResourceLocation id;

    private final List<ItemStack> inputs;
    private final List<ItemStack> outputs;

    private final int processingTime;
    private final int energyPerTick;

    public PrecisionAssemblerRecipe(
            ResourceLocation id,
            List<ItemStack> inputs,
            List<ItemStack> outputs,
            int processingTime,
            int energyPerTick
    ) {
        this.id = id;
        this.inputs = inputs;
        this.outputs = outputs;
        this.processingTime = processingTime;
        this.energyPerTick = energyPerTick;
    }

    public List<ItemStack> getItemInputs() {
        return inputs;
    }

    public List<ItemStack> getItemOutputs() {
        return outputs;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public int getEnergyPerTick() {
        return energyPerTick;
    }

    @Override
    public boolean matches(PrecisionAssemblerRecipeInput wrapper, Level level) {
        return wrapper.containsAll(inputs);
    }

    @Override
    public ItemStack assemble(PrecisionAssemblerRecipeInput wrapper, HolderLookup.Provider provider) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).copy();
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).copy();
    }

    public ResourceLocation getId() {
        return id;
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
