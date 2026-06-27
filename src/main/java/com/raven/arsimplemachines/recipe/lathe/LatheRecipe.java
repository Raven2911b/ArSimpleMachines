package com.raven.arsimplemachines.recipe.lathe;

import com.raven.arsimplemachines.registry.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class LatheRecipe implements Recipe<LatheRecipeInput> {

    private final ResourceLocation id;
    private final Item input;
    private final Item output;
    private final int outputCount;
    public final int processingTime;

    public LatheRecipe(ResourceLocation id, Item input, Item output, int outputCount, int processingTime) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.outputCount = outputCount;
        this.processingTime = processingTime;
    }

    public Item getInputItem() {
        return input;
    }

    public Item getOutputItem() {
        return output;
    }

    public int getOutputCount() {
        return outputCount;
    }

    @Override
    public boolean matches(LatheRecipeInput inputWrapper, Level level) {
        return inputWrapper.getItem(0).is(this.input);
    }

    @Override
    public ItemStack assemble(LatheRecipeInput inputWrapper, HolderLookup.Provider provider) {
        return new ItemStack(output, outputCount);
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return new ItemStack(output, outputCount);
    }

   // @Override
    public ResourceLocation getId() {
        return id;
    }

    // ✔ FIXED — no .get() calls
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.LATHE_SERIALIZER;
    }

    // ✔ FIXED — no .get() calls
    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.LATHE_TYPE;
    }
}
