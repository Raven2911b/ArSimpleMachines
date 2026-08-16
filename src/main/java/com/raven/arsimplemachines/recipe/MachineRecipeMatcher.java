package com.raven.arsimplemachines.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
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

        System.out.println("=== MachineRecipeMatcher.findMatch ===");

        RecipeManager manager = level.getRecipeManager();
        List<RecipeHolder<T>> holders = manager.getAllRecipesFor(type);

        List<ItemStack> itemInputs = inputs.getItems();
        List<FluidStack> fluidInputs = inputs.getFluids();

        System.out.println("Recipe type: " + type);
        System.out.println("Item inputs:");
        for (ItemStack s : itemInputs) {
            System.out.println("  - " + s + " / " +
                    BuiltInRegistries.ITEM.getKey(s.getItem()) +
                    " x" + s.getCount());
        }

        System.out.println("Fluid inputs:");
        for (FluidStack f : fluidInputs) {
            System.out.println("  - " + BuiltInRegistries.FLUID.getKey(f.getFluid()) +
                    " x" + f.getAmount());
        }

        System.out.println("Total recipes for type: " + holders.size());

        for (RecipeHolder<T> holder : holders) {
            System.out.println("Checking recipe: " + holder.id());
            T recipe = holder.value();

            boolean ok = matches(recipe, itemInputs, fluidInputs, level);
            System.out.println("  -> matches = " + ok);

            if (ok) {
                System.out.println("=== MATCH FOUND: " + holder.id() + " ===");
                return recipe;
            }
        }

        System.out.println("=== NO MATCH FOUND ===");
        return null;
    }

    public static boolean matches(MachineRecipe recipe,
                                  List<ItemStack> items,
                                  List<FluidStack> fluids,
                                  Level level) {

        System.out.println("=== MachineRecipeMatcher.matches ===");
        System.out.println("Recipe: " + recipe.getId());

        if (items.isEmpty() && fluids.isEmpty()) {
            System.out.println("No inputs at all -> fail");
            return false;
        }

        // ---------------------------------------------------------
        // DIRECT ITEM INPUTS
        // ---------------------------------------------------------
        System.out.println("Checking direct item inputs...");
        for (ItemStack req : recipe.getItemInputs()) {
            int needed = req.getCount();
            int found = 0;

            System.out.println("  Need item: " +
                    BuiltInRegistries.ITEM.getKey(req.getItem()) +
                    " x" + needed);

            for (ItemStack in : items) {
                if (!in.isEmpty()) {
                    System.out.println("    Input: " +
                            BuiltInRegistries.ITEM.getKey(in.getItem()) +
                            " x" + in.getCount());

                    if (in.is(req.getItem())) {
                        found += in.getCount();
                        System.out.println("      MATCH -> found=" + found);
                        if (found >= needed) break;
                    }
                }
            }

            System.out.println("  Direct item result: found=" + found + " needed=" + needed);

            if (found < needed) {
                System.out.println("  FAIL direct item");
                return false;
            }
        }

        // ---------------------------------------------------------
        // TAG INPUTS
        // ---------------------------------------------------------
        System.out.println("Checking tag inputs...");
        for (TagInput tagInput : recipe.getItemTags()) {

            TagKey<net.minecraft.world.item.Item> tagKey =
                    TagKey.create(BuiltInRegistries.ITEM.key(), tagInput.tag());

            int needed = tagInput.count();
            int matched = 0;

            System.out.println("  TAG CHECK: " + tagInput.tag() + " need=" + needed);

            for (ItemStack in : items) {
                if (!in.isEmpty()) {
                    var id = BuiltInRegistries.ITEM.getKey(in.getItem());
                    boolean isMatch = in.is(tagKey);

                    System.out.println("    input: " + id +
                            " x" + in.getCount() +
                            " matches=" + isMatch);

                    if (isMatch) {
                        matched += in.getCount();
                        System.out.println("      matched now=" + matched);
                        if (matched >= needed) break;
                    }
                }
            }

            System.out.println("  TAG RESULT: matched=" + matched + " needed=" + needed);

            if (matched < needed) {
                System.out.println("  FAIL tag");
                return false;
            }
        }

        // ---------------------------------------------------------
        // FLUID INPUTS
        // ---------------------------------------------------------
        System.out.println("Checking fluid inputs...");
        for (FluidStack req : recipe.getFluidInputs()) {
            int needed = req.getAmount();
            int found = 0;

            System.out.println("  Need fluid: " +
                    BuiltInRegistries.FLUID.getKey(req.getFluid()) +
                    " x" + needed);

            for (FluidStack in : fluids) {
                if (!in.isEmpty()) {
                    System.out.println("    Input fluid: " +
                            BuiltInRegistries.FLUID.getKey(in.getFluid()) +
                            " x" + in.getAmount());

                    if (in.getFluid() == req.getFluid()) {
                        found += in.getAmount();
                        System.out.println("      MATCH -> found=" + found);
                        if (found >= needed) break;
                    }
                }
            }

            System.out.println("  Fluid result: found=" + found + " needed=" + needed);

            if (found < needed) {
                System.out.println("  FAIL fluid");
                return false;
            }
        }

        System.out.println("=== MATCH SUCCESS ===");
        return true;
    }
}
