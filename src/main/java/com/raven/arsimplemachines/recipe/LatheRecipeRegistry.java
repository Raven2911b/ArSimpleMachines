package com.raven.arsimplemachines.recipe;

import com.raven.arsimplemachines.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;

public class LatheRecipeRegistry {

    public static class LatheRecipe {
        public final ItemStack input;
        public final ItemStack output;
        public final int processingTime;

        public LatheRecipe(ItemStack input, ItemStack output, int processingTime) {
            this.input = input;
            this.output = output;
            this.processingTime = processingTime;
        }
    }

    private static final List<LatheRecipe> RECIPES = new ArrayList<>();

    static {
        // Immersive Engineering rod items
        Item ironRod = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "stick_iron"));
        Item steelRod = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "stick_steel"));
        Item aluminumRod = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "stick_aluminum"));
        Item netheriteRod = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "stick_netherite"));

        // Immersive Engineering ingots
        Item steelIngot = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "ingot_steel"));
        Item aluminumIngot = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "ingot_aluminum"));

        // Titanium items (your mod)
        Item titaniumIngot = ModItems.TITANIUM_INGOT.get();
        Item titaniumRod = ModItems.TITANIUM_ROD.get();
        // Iron → Iron Rod
        if (ironRod != null)
            registerRecipe(new ItemStack(Items.IRON_INGOT), new ItemStack(ironRod, 3), 200);

        // Steel → Steel Rod
        if (steelRod != null && steelIngot != null)
            registerRecipe(new ItemStack(steelIngot), new ItemStack(steelRod, 2), 250);

        // Aluminum → Aluminum Rod
        if (aluminumRod != null && aluminumIngot != null)
            registerRecipe(new ItemStack(aluminumIngot), new ItemStack(aluminumRod, 3), 180);

        // Netherite → Netherite Rod
        if (netheriteRod != null)
            registerRecipe(new ItemStack(Items.NETHERITE_INGOT), new ItemStack(netheriteRod), 400);
        // Titanium → Titanium Rod
        if (titaniumIngot != null && titaniumRod != null)
            registerRecipe(new ItemStack(titaniumIngot), new ItemStack(titaniumRod, 2), 300);
    }

    public static void registerRecipe(ItemStack input, ItemStack output, int processingTime) {
        RECIPES.add(new LatheRecipe(input, output, processingTime));
    }

    public static LatheRecipe findRecipe(ItemStack input) {
        for (LatheRecipe recipe : RECIPES) {
            if (recipe.input.getItem() == input.getItem()) {
                return recipe;
            }
        }
        return null;
    }

    public static List<LatheRecipe> getAllRecipes() {
        return new ArrayList<>(RECIPES);
    }
}
