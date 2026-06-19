package com.raven.arsimplemachines.recipe;

import com.raven.arsimplemachines.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class RollingRecipeRegistry {

    public static class RollingRecipe {
        public final ItemStack input;
        public final ItemStack output;

        public final int processingTime;   // ticks
        public final int energyPerTick;    // FE/t
        public final int fluidRequired;    // mB
        public final String fluidName;     // "minecraft:water"

        public RollingRecipe(ItemStack in, ItemStack out,
                             int time, int energyPerTick,
                             String fluidName, int fluidRequired) {

            this.input = in;
            this.output = out;
            this.processingTime = time;
            this.energyPerTick = energyPerTick;
            this.fluidName = fluidName;
            this.fluidRequired = fluidRequired;
        }
    }

    private static final Map<String, RollingRecipe> RECIPES = new HashMap<>();

    static {
        // Titanium Ingot → Titanium Plate
        register(
                new ItemStack(ModItems.TITANIUM_INGOT.get()),
                new ItemStack(ModItems.TITANIUM_PLATE.get(), 1),  // or 2 if you want
                300,                 // ticks (titanium takes longer)
                40,                  // FE/t (more energy for harder metal)
                "minecraft:water",   // or your custom coolant
                250                  // mB (more fluid for titanium)
        );
    }


    public static void register(ItemStack in, ItemStack out,
                                int time, int energyPerTick,
                                String fluidName, int fluidRequired) {

        RECIPES.put(in.getItem().toString(),
                new RollingRecipe(in, out, time, energyPerTick, fluidName, fluidRequired)
        );
    }

    public static RollingRecipe findRecipe(ItemStack stack) {
        return RECIPES.get(stack.getItem().toString());
    }
}
