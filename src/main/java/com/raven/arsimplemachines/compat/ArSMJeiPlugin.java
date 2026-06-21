package com.raven.arsimplemachines.compat.jei;

import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.recipe.GasChargeRecipe;
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

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new GasChargeRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var recipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.GAS_CHARGE_TYPE.get())
                .stream()
                .map(holder -> holder.value())
                .toList();

        registration.addRecipes(
                com.raven.arsimplemachines.compat.jei.GasChargeRecipeCategory.RECIPE_TYPE,
                recipes
        );
    }


    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.GAS_CHARGE_PAD.get()),
                GasChargeRecipeCategory.RECIPE_TYPE
        );
    }
}
