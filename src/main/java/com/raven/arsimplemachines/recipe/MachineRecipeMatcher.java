package com.raven.arsimplemachines.recipe;

import com.raven.arsimplemachines.recipe.fluidconverter.FluidConverterRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class MachineRecipeMatcher {

    public static <T extends MachineRecipe> T findMatch(Level level,
                                                        RecipeType<T> type,
                                                        MachineRecipeInput inputs) {

        RecipeManager manager = level.getRecipeManager();
        List<RecipeHolder<T>> holders = manager.getAllRecipesFor(type);

        List<ItemStack> itemInputs = inputs.getItems();
        List<FluidStack> fluidInputs = inputs.getFluids();

        for (RecipeHolder<T> holder : holders) {
            T recipe = holder.value();

            if (matches(recipe, itemInputs, fluidInputs, level)) {
                return recipe;
            }
        }

        return null;
    }

    private static boolean fluidMatchesAnyTag(FluidStack fluid, ResourceLocation preferredTag) {
        if (fluid == null || fluid.isEmpty()) return false;

        List<ResourceLocation> tags = new ArrayList<>();
        tags.add(preferredTag);

        if (preferredTag.equals(ResourceLocation.tryParse("c:oxygen"))
                || preferredTag.equals(ResourceLocation.tryParse("forge:oxygen"))) {
            tags.add(ResourceLocation.tryParse("c:oxygen"));
            tags.add(ResourceLocation.tryParse("forge:oxygen"));
        }

        for (ResourceLocation tag : tags) {
            if (tag == null) continue;
            TagKey<net.minecraft.world.level.material.Fluid> key =
                    TagKey.create(BuiltInRegistries.FLUID.key(), tag);

            if (fluid.is(key)) {
                return true;
            }
        }

        return false;
    }

    public static boolean matches(MachineRecipe recipe,
                                  List<ItemStack> items,
                                  List<FluidStack> fluids,
                                  Level level) {

        if (items.isEmpty() && fluids.isEmpty()) {
            return false;
        }

        for (ItemStack req : recipe.getItemInputs()) {
            int needed = req.getCount();
            int found = 0;

            for (ItemStack in : items) {
                if (!in.isEmpty() && in.is(req.getItem())) {
                    found += in.getCount();
                    if (found >= needed) break;
                }
            }

            if (found < needed) {
                return false;
            }
        }

        for (TagInput tagInput : recipe.getItemTags()) {
            TagKey<net.minecraft.world.item.Item> tagKey =
                    TagKey.create(BuiltInRegistries.ITEM.key(), tagInput.tag());

            int needed = tagInput.count();
            int matched = 0;

            for (ItemStack in : items) {
                if (!in.isEmpty() && in.is(tagKey)) {
                    matched += in.getCount();
                    if (matched >= needed) break;
                }
            }

            if (matched < needed) {
                return false;
            }
        }

        for (FluidStack req : recipe.getFluidInputs()) {
            int needed = req.getAmount();
            int found = 0;

            for (FluidStack in : fluids) {
                if (!in.isEmpty() && in.getFluid() == req.getFluid()) {
                    found += in.getAmount();
                    if (found >= needed) break;
                }
            }

            if (found < needed) {
                return false;
            }
        }

        if (recipe instanceof FluidConverterRecipe fluidRecipe) {
            for (FluidConverterRecipe.FluidTagInput tagReq : fluidRecipe.getFluidInputTags()) {
                int needed = tagReq.amount();
                int found = 0;

                for (FluidStack in : fluids) {
                    if (!in.isEmpty() && fluidMatchesAnyTag(in, tagReq.tag())) {
                        found += in.getAmount();
                        if (found >= needed) break;
                    }
                }

                if (found < needed) {
                    return false;
                }
            }
        }

        return true;
    }
}