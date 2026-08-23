package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.recipe.electrolyzer.ElectrolyzerRecipe;
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

public class ElectrolyzerRecipeCategory implements IRecipeCategory<ElectrolyzerRecipe> {

    public static final RecipeType<ElectrolyzerRecipe> TYPE =
            new RecipeType<>(
                    ResourceLocation.fromNamespaceAndPath("arsimplemachines", "electrolyzer"),
                    ElectrolyzerRecipe.class
            );

    private final IDrawable background;
    private final IDrawable icon;

    public ElectrolyzerRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 70);
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModBlocks.ELECTROLYZER_CONTROLLER.get())
        );
    }

    @Override
    public RecipeType<ElectrolyzerRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Electrolyzer");
    }

    @Override
    public IDrawable getBackground() {
        return background; // deprecated but still required
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    // -------------------------------------------------------------------------
    // JEI LAYOUT (Unified)
    // -------------------------------------------------------------------------
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder,
                          ElectrolyzerRecipe recipe,
                          IFocusGroup focuses) {

        FluidStack in = recipe.getFluidInputs().get(0);
        FluidStack outA = recipe.getFluidOutputs().get(0);
        FluidStack outB = recipe.getFluidOutputs().get(1);

        // -----------------------------
        // INPUT FLUID
        // -----------------------------
        builder.addSlot(RecipeIngredientRole.INPUT, 20, 25)
                .addFluidStack(in.getFluid(), in.getAmount())
                .addTooltipCallback((slot, tooltip) -> {
                    tooltip.add(Component.literal(
                            "Input: " + in.getAmount() + " mB"
                    ));
                    tooltip.add(Component.literal(
                            "Fluid: " + Component.translatable(in.getTranslationKey()).getString()
                    ));
                });

        // -----------------------------
        // OUTPUT A
        // -----------------------------
        builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 15)
                .addFluidStack(outA.getFluid(), outA.getAmount())
                .addTooltipCallback((slot, tooltip) -> {
                    tooltip.add(Component.literal(
                            "Output A: " + outA.getAmount() + " mB"
                    ));
                    tooltip.add(Component.literal(
                            "Fluid: " + Component.translatable(outA.getTranslationKey()).getString()
                    ));
                });

        // -----------------------------
        // OUTPUT B
        // -----------------------------
        builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 35)
                .addFluidStack(outB.getFluid(), outB.getAmount())
                .addTooltipCallback((slot, tooltip) -> {
                    tooltip.add(Component.literal(
                            "Output B: " + outB.getAmount() + " mB"
                    ));
                    tooltip.add(Component.literal(
                            "Fluid: " + Component.translatable(outB.getTranslationKey()).getString()
                    ));
                    tooltip.add(Component.literal(
                            "Time: " + recipe.getProcessingTime() + " ticks"
                    ));
                    tooltip.add(Component.literal(
                            "Energy: " + recipe.getEnergyPerTick() + " RF/t"
                    ));
                });
    }
}
