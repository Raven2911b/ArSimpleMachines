package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.recipe.lathe.LatheRecipe;
import com.raven.arsimplemachines.registry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class LatheRecipeCategory implements IRecipeCategory<LatheRecipe> {

    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "lathe");

    public static final RecipeType<LatheRecipe> RECIPE_TYPE =
            new RecipeType<>(UID, LatheRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable latheImage;

    public LatheRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 60);

        // JEI tab icon
        this.icon = guiHelper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.LATHE_CONTROLLER.get())
        );

        // Center image (block icon)
        this.latheImage = guiHelper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.LATHE_CONTROLLER.get())
        );
    }

    @Override
    public RecipeType<LatheRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Lathe");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder,
                          LatheRecipe recipe,
                          IFocusGroup focuses) {

        builder.addSlot(RecipeIngredientRole.INPUT, 20, 22)
                .addItemStack(new ItemStack(recipe.getInputItem()));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 110, 22)
                .addItemStack(new ItemStack(recipe.getOutputItem(), recipe.getOutputCount()));
    }

    @Override
    public void draw(LatheRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {

        // Draw the lathe block between input and output
        latheImage.draw(graphics, 65, 10);
    }
}
