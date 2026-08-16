package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.recipe.eaf.ElectricArcFurnaceRecipe;
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
import net.neoforged.neoforge.fluids.FluidStack;

public class ElectricArcFurnaceRecipeCategory implements IRecipeCategory<ElectricArcFurnaceRecipe> {

    public static final RecipeType<ElectricArcFurnaceRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath("arsimplemachines", "electric_arc_furnace"),
                    ElectricArcFurnaceRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public ElectricArcFurnaceRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(180, 90);
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get())
        );
    }

    @Override
    public RecipeType<ElectricArcFurnaceRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Electric Arc Furnace");
    }

    @Override
    public IDrawable getBackground() {
        return background; // deprecated but required
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder,
                          ElectricArcFurnaceRecipe recipe,
                          IFocusGroup focuses) {

        int x = 10;
        int y = 20;

        // -----------------------------
        // ITEM INPUTS
        // -----------------------------
        for (ItemStack in : recipe.getItemInputs()) {
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addItemStack(in)
                    .addTooltipCallback((slot, tooltip) -> {
                        tooltip.add(Component.literal("Input: " + in.getCount()));
                    });
            x += 20;
        }

        // -----------------------------
        // OUTPUTS
        // -----------------------------
        x = 10;
        y = 55;

        for (ItemStack out : recipe.getItemOutputs()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .addItemStack(out)
                    .addTooltipCallback((slot, tooltip) -> {
                        tooltip.add(Component.literal("Output: " + out.getCount()));
                        tooltip.add(Component.literal("Time: " + recipe.getProcessingTime() + " ticks"));
                        tooltip.add(Component.literal("Energy: " + recipe.getEnergyPerTick() + " RF/t"));
                    });
            x += 20;
        }
    }
}
