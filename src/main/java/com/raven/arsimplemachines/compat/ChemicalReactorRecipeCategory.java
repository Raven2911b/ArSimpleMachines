package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.recipe.chemical.ChemicalReactorRecipe;
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

public class ChemicalReactorRecipeCategory implements IRecipeCategory<ChemicalReactorRecipe> {

    public static final RecipeType<ChemicalReactorRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath("arsimplemachines", "chemical"),
                    ChemicalReactorRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public ChemicalReactorRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 70);
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModBlocks.CHEMICAL_REACTOR_CONTROLLER.get())
        );
    }

    @Override
    public RecipeType<ChemicalReactorRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Chemical Reactor");
    }

    @Override
    public IDrawable getBackground() {
        return background; // still allowed, just deprecated
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder,
                          ChemicalReactorRecipe recipe,
                          IFocusGroup focuses) {

        FluidStack a = recipe.getFluidA();
        FluidStack b = recipe.getFluidB();
        FluidStack out = recipe.getOutput();

        // -----------------------------
        // FLUID INPUT A
        // -----------------------------
        builder.addSlot(RecipeIngredientRole.INPUT, 20, 25)
                .addFluidStack(a.getFluid(), a.getAmount())
                .addTooltipCallback((slot, tooltip) -> {
                    tooltip.add(Component.literal("Input A: " + a.getAmount() + " mB"));
                });

        // -----------------------------
        // FLUID INPUT B
        // -----------------------------
        builder.addSlot(RecipeIngredientRole.INPUT, 60, 25)
                .addFluidStack(b.getFluid(), b.getAmount())
                .addTooltipCallback((slot, tooltip) -> {
                    tooltip.add(Component.literal("Input B: " + b.getAmount() + " mB"));
                });

        // -----------------------------
        // FLUID OUTPUT
        // -----------------------------
        builder.addSlot(RecipeIngredientRole.OUTPUT, 110, 25)
                .addFluidStack(out.getFluid(), out.getAmount())
                .addTooltipCallback((slot, tooltip) -> {
                    tooltip.add(Component.literal("Output: " + out.getAmount() + " mB"));
                    tooltip.add(Component.literal("Time: " + recipe.getProcessingTime() + " ticks"));
                    tooltip.add(Component.literal("Energy: " + recipe.getEnergyPerTick() + " RF/t"));
                });
    }
}
