package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.recipe.roller.RollingRecipe;
import com.raven.arsimplemachines.registry.ModBlocks;

import java.util.List;

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

public class RollingRecipeCategory implements IRecipeCategory<RollingRecipe> {

    public static final RecipeType<RollingRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath("arsimplemachines", "rolling"), RollingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public RollingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 60);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.ROLLING_CONTROLLER.get()));
    }

    @Override
    public RecipeType<RollingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Rolling Machine");
    }

    @Override
    @SuppressWarnings("removal")
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RollingRecipe recipe, IFocusGroup focuses) {

        int x = 10;
        int y = 10;

        // -----------------------------
        // DIRECT ITEM INPUTS
        // -----------------------------
        for (ItemStack stack : recipe.getItemInputs()) {
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addItemStack(stack).addRichTooltipCallback((slotView, tooltip) -> {
                        tooltip.add(Component.literal("Required: " + stack.getCount()));
                    });

            x += 20;
        }

        // -----------------------------
        // FLUID INPUTS
        // -----------------------------
        for (FluidStack fs : recipe.getFluidInputs()) {
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addFluidStack(fs.getFluid(), fs.getAmount()).addRichTooltipCallback((slotView, tooltip) -> {
                        tooltip.add(Component.literal("Required: " + fs.getAmount() + " mB"));
                    });

            x += 20;
        }

        // -----------------------------
        // OUTPUTS
        // -----------------------------
        int outX = 110;
        int outY = 20;

        for (ItemStack out : recipe.getItemOutputs()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, outX, 10)
                    .addItemStack(out);
            outY += 20;
        }

        for (FluidStack fs : recipe.getFluidOutputs()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, outX, outY)
                    .addFluidStack(fs.getFluid(), fs.getAmount());
            outY += 20;
        }
    }
}
