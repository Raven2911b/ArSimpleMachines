package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.recipe.cutter.CuttingMachineRecipe;
import com.raven.arsimplemachines.registry.ModBlocks;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CuttingRecipeCategory implements IRecipeCategory<CuttingMachineRecipe> {

    public static final RecipeType<CuttingMachineRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath("arsimplemachines", "cutting_machine"),
                    CuttingMachineRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public CuttingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 60);

        // JEI tab icon = Cutting Machine controller block
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModBlocks.CUTTING_MACHINE_CONTROLLER.get())
        );
    }

    @Override
    public RecipeType<CuttingMachineRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Cutting Machine");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder,
                          CuttingMachineRecipe recipe,
                          IFocusGroup focuses) {

        // -----------------------------
        // SINGLE ITEM INPUT
        // -----------------------------
        builder.addSlot(RecipeIngredientRole.INPUT, 20, 22)
                .addItemStack(new ItemStack(recipe.getInputItem()));

        // -----------------------------
        // SINGLE ITEM OUTPUT
        // -----------------------------
        builder.addSlot(RecipeIngredientRole.OUTPUT, 110, 22)
                .addItemStack(new ItemStack(recipe.getOutputItem(), recipe.getOutputCount()));
    }
}
