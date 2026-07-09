package com.raven.arsimplemachines.recipe;

import com.raven.arsimplemachines.util.CategoryRegistry;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

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

    public static boolean matches(MachineRecipe recipe,
                                  List<ItemStack> items,
                                  List<FluidStack> fluids,
                                  Level level) {

        if (items.isEmpty() && fluids.isEmpty()) {
            return false;
        }

        // ---------------------------------------------------------
        // DIRECT ITEM INPUTS (unordered, count-based)
        // ---------------------------------------------------------
        for (ItemStack req : recipe.getItemInputs()) {
            int needed = req.getCount();
            int found = 0;

            for (ItemStack in : items) {
                if (!in.isEmpty() && in.is(req.getItem())) {
                    found += in.getCount();
                    if (found >= needed) break;
                }
            }

            if (found < needed) return false;
        }

        // ---------------------------------------------------------
        // CATEGORY INPUTS (unordered, count-based)
        // ---------------------------------------------------------
        for (CategoryInput cat : recipe.getItemCategories()) {

            String category = cat.category;
            int needed = cat.count;
            int matched = 0;

            for (ItemStack in : items) {
                if (!in.isEmpty() && CategoryRegistry.matches(category, in)) {
                    matched += in.getCount();
                    if (matched >= needed) break;
                }
            }

            if (matched < needed) return false;
        }

        // ---------------------------------------------------------
        // FLUID INPUTS (unordered, count-based)
        // ---------------------------------------------------------
        for (FluidStack req : recipe.getFluidInputs()) {
            int needed = req.getAmount();
            int found = 0;

            for (FluidStack in : fluids) {
                if (!in.isEmpty() &&
                        in.getFluid() == req.getFluid()) {

                    found += in.getAmount();
                    if (found >= needed) break;
                }
            }

            if (found < needed) return false;
        }

        return true;
    }
}
