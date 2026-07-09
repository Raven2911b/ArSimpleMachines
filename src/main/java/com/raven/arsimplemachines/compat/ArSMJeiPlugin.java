package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.recipe.roller.RollingRecipe;
import com.raven.arsimplemachines.registry.ModBlocks;
import com.raven.arsimplemachines.registry.ModRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class ArSMJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("arsimplemachines", "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new RollingRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var recipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.ROLLING_TYPE)
                .stream()
                .map(holder -> holder.value())
                .toList();

        registration.addRecipes(
                RollingRecipeCategory.TYPE,
                recipes
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.ROLLING_CONTROLLER.get()),
                RollingRecipeCategory.TYPE
        );
    }
}
