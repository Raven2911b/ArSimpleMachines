package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.recipe.electrolyzer.ElectrolyzerRecipe;
import com.raven.arsimplemachines.registry.ModBlocks;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
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
import net.neoforged.neoforge.fluids.FluidStack;

public class ElectrolyzerRecipeCategory implements IRecipeCategory<ElectrolyzerRecipe> {

    public static final RecipeType<ElectrolyzerRecipe> TYPE =
            new RecipeType<>(
                    ResourceLocation.fromNamespaceAndPath("arsimplemachines", "electrolyzer"),
                    ElectrolyzerRecipe.class
            );

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progress;

    public ElectrolyzerRecipeCategory(IGuiHelper guiHelper) {

        // Unified background slice
        this.background = guiHelper.createDrawable(
                ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/generic_jei_background.png"),
                3, 4, 170, 80
        );

        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModBlocks.ELECTROLYZER_CONTROLLER.get())
        );

        // Same progress bar slice used in other machines
        this.progress = guiHelper.drawableBuilder(
                ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/generic_jei_background.png"),
                192, 0, 37, 10
        ).buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void draw(ElectrolyzerRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {

        // Progress bar
        progress.draw(graphics, 65, 40);

        // Power text
        graphics.drawString(
                Minecraft.getInstance().font,
                "Power: " + recipe.getEnergyPerTick() + " RF/t",
                2, 85,
                0x404040,
                false
        );

        // Time text
        graphics.drawString(
                Minecraft.getInstance().font,
                "Time: " + (recipe.getProcessingTime() / 20) + " s",
                120, 85,
                0x404040,
                false
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
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder,
                          ElectrolyzerRecipe recipe,
                          IFocusGroup focuses) {

        FluidStack in = recipe.getFluidInputs().get(0);
        FluidStack outA = recipe.getFluidOutputs().get(0);
        FluidStack outB = recipe.getFluidOutputs().get(1);

        int inputX = 5;
        int outputX = 113;
        int rowY = 13;

        // INPUT
        builder.addSlot(RecipeIngredientRole.INPUT, inputX, rowY)
                .addFluidStack(in.getFluid(), in.getAmount())
                .addTooltipCallback((slot, tooltip) -> {
                    tooltip.add(Component.literal("Input: " + in.getAmount() + " mB"));
                });

        // OUTPUT A
        builder.addSlot(RecipeIngredientRole.OUTPUT, outputX, rowY)
                .addFluidStack(outA.getFluid(), outA.getAmount())
                .addTooltipCallback((slot, tooltip) -> {
                    tooltip.add(Component.literal("Output A: " + outA.getAmount() + " mB"));
                });

        // OUTPUT B
        builder.addSlot(RecipeIngredientRole.OUTPUT, outputX, rowY + 18)
                .addFluidStack(outB.getFluid(), outB.getAmount())
                .addTooltipCallback((slot, tooltip) -> {
                    tooltip.add(Component.literal("Output B: " + outB.getAmount() + " mB"));
                });
    }
}
