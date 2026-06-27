package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.recipe.roller.RollingRecipe;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RollingRecipeCategory implements IRecipeCategory<RollingRecipe> {

    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "rolling");

    public static final RecipeType<RollingRecipe> RECIPE_TYPE =
            new RecipeType<>(UID, RollingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable machineImage;

    public RollingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 60);

        // JEI tab icon
        this.icon = guiHelper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.ROLLING_CONTROLLER.get())
        );

        // Center image (block icon)
        this.machineImage = guiHelper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.ROLLING_CONTROLLER.get())
        );
    }

    @Override
    public RecipeType<RollingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Rolling Machine");
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
                          RollingRecipe recipe,
                          IFocusGroup focuses) {

        // INPUT slot
        builder.addSlot(RecipeIngredientRole.INPUT, 20, 22)
                .addItemStack(new ItemStack(recipe.getInputItem()));

        // OUTPUT slot
        builder.addSlot(RecipeIngredientRole.OUTPUT, 110, 22)
                .addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(RollingRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {

        // Draw the rolling machine block between input and output
        machineImage.draw(graphics, 65, 10);

        // Tooltip area (x=65, y=10, width=32, height=32)
        if (mouseX >= 65 && mouseX <= 97 &&
                mouseY >= 10 && mouseY <= 42) {

            graphics.renderTooltip(
                    Minecraft.getInstance().font,
                    Component.literal("Rolling Machine"),
                    (int) mouseX,
                    (int) mouseY
            );
        }
    }
}
