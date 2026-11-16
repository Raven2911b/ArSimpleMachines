package com.raven.arsimplemachines.recipe.fluidconverter;

import com.raven.arsimplemachines.recipe.MachineRecipe;
import com.raven.arsimplemachines.recipe.MachineRecipeInput;
import com.raven.arsimplemachines.recipe.TagInput;
import com.raven.arsimplemachines.registry.ModRecipeTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class FluidConverterRecipe extends MachineRecipe {

    public static record FluidTagInput(ResourceLocation tag, int amount) {
    }

    private final List<FluidTagInput> fluidInputTags;

    public FluidConverterRecipe(ResourceLocation id,
                                List<ItemStack> itemInputs,
                                List<TagInput> itemTags,
                                List<FluidStack> fluidInputs,
                                List<FluidTagInput> fluidInputTags,
                                List<ItemStack> itemOutputs,
                                List<FluidStack> fluidOutputs,
                                int processingTime,
                                int energyPerTick) {
        super(id, itemInputs, itemTags, fluidInputs, itemOutputs, fluidOutputs, processingTime, energyPerTick);
        this.fluidInputTags = fluidInputTags == null ? List.of() : fluidInputTags;
    }

    public List<FluidTagInput> getFluidInputTags() {
        return fluidInputTags;
    }

    public boolean matchesFluidInput(FluidStack available) {
        if (available == null || available.isEmpty()) return false;

        for (FluidStack req : getFluidInputs()) {
            if (available.getFluid() == req.getFluid() && available.getAmount() >= req.getAmount()) {
                return true;
            }
        }

        for (FluidTagInput tagReq : fluidInputTags) {
            TagKey<Fluid> key = TagKey.create(BuiltInRegistries.FLUID.key(), tagReq.tag());
            if (available.is(key) && available.getAmount() >= tagReq.amount()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.FLUID_CONVERTER_SERIALIZER;
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeType<?> getType() {
        return ModRecipeTypes.FLUID_CONVERTER_TYPE;
    }

    @Override
    public boolean matches(MachineRecipeInput input, net.minecraft.world.level.Level level) {
        for (FluidStack fluid : input.getFluids()) {
            if (matchesFluidInput(fluid)) {
                return true;
            }
        }
        return false;
    }
}