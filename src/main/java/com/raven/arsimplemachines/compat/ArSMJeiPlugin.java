package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.compat.GasChargeRecipeCategory;
import com.raven.arsimplemachines.compat.LatheRecipeCategory;
import com.raven.arsimplemachines.compat.RollingRecipeCategory;

import com.raven.arsimplemachines.recipe.lathe.LatheRecipe;
import com.raven.arsimplemachines.recipe.roller.RollingRecipe;

import com.raven.arsimplemachines.registry.ModBlocks;
import com.raven.arsimplemachines.registry.ModRecipeTypes;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class ArSMJeiPlugin implements IModPlugin {

    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "jei_plugin");

    // JEI recipe type for the lathe
    public static final RecipeType<LatheRecipe> LATHE_JEI_TYPE =
            RecipeType.create(ArSimpleMachines.MODID, "lathe", LatheRecipe.class);

    // JEI recipe type for the rolling machine
    public static final RecipeType<RollingRecipe> ROLLING_JEI_TYPE =
            RecipeType.create(ArSimpleMachines.MODID, "rolling", RollingRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new GasChargeRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new LatheRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new RollingRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        var level = Minecraft.getInstance().level;
        if (level == null) return;

        // -----------------------------
        // GAS CHARGE PAD RECIPES
        // -----------------------------
        var gasRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.GAS_CHARGE_TYPE.get())
                .stream()
                .map(holder -> holder.value())
                .toList();

        registration.addRecipes(
                GasChargeRecipeCategory.RECIPE_TYPE,
                gasRecipes
        );

        // -----------------------------
        // LATHE RECIPES
        // -----------------------------
        var latheRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.LATHE_TYPE)
                .stream()
                .map(holder -> holder.value())
                .toList();

        registration.addRecipes(
                LATHE_JEI_TYPE,
                latheRecipes
        );

        // -----------------------------
        // ROLLING MACHINE RECIPES
        // -----------------------------
        var rollingRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.ROLLING_TYPE)
                .stream()
                .map(holder -> holder.value())
                .toList();

        registration.addRecipes(
                ROLLING_JEI_TYPE,
                rollingRecipes
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {

        // Gas Charge Pad
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.GAS_CHARGE_PAD.get()),
                GasChargeRecipeCategory.RECIPE_TYPE
        );

        // Lathe Controller
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.LATHE_CONTROLLER.get()),
                LATHE_JEI_TYPE
        );

        // Rolling Machine Controller
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.ROLLING_CONTROLLER.get()),
                ROLLING_JEI_TYPE
        );
    }
}
