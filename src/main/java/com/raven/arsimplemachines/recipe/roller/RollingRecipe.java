package com.raven.arsimplemachines.recipe.roller;

import com.raven.arsimplemachines.registry.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class RollingRecipe implements Recipe<RollingRecipeInput> {

    private final ResourceLocation id;

    private final Item input;
    private final ItemStack output;

    private final int processingTime;
    private final int energyPerTick;
    private final int fluidRequired;

    public RollingRecipe(
            ResourceLocation id,
            Item input,
            ItemStack output,
            int processingTime,
            int energyPerTick,
            int fluidRequired
    ) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.processingTime = processingTime;
        this.energyPerTick = energyPerTick;
        this.fluidRequired = fluidRequired;
    }

    public Item getInputItem() {
        return input;
    }

    public ItemStack getOutput() {
        return output.copy();
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public int getEnergyPerTick() {
        return energyPerTick;
    }

    public int getFluidRequired() {
        return fluidRequired;
    }

    @Override
    public boolean matches(RollingRecipeInput wrapper, Level level) {
        return wrapper.getItem(0).is(this.input);
    }

    @Override
    public ItemStack assemble(RollingRecipeInput wrapper, HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return output.copy();
    }

    //@Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ROLLING_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ROLLING_TYPE;
    }
}
