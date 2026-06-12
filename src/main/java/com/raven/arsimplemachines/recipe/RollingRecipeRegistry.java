package com.raven.arsimplemachines.recipe;

import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class RollingRecipeRegistry {

    public static class RollingRecipe {
        public final ItemStack input;
        public final ItemStack output;
        public final int processingTime;

        public RollingRecipe(ItemStack in, ItemStack out, int time) {
            input = in;
            output = out;
            processingTime = time;
        }
    }

    private static final Map<String, RollingRecipe> RECIPES = new HashMap<>();

    public static void register(ItemStack in, ItemStack out, int time) {
        RECIPES.put(in.getItem().toString(), new RollingRecipe(in, out, time));
    }

    public static RollingRecipe findRecipe(ItemStack stack) {
        return RECIPES.get(stack.getItem().toString());
    }
}
