package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.recipe.eaf.ElectricArcFurnaceRecipe;
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

public class ElectricArcFurnaceRecipeCategory implements IRecipeCategory<ElectricArcFurnaceRecipe> {

    public static final RecipeType<ElectricArcFurnaceRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath("arsimplemachines", "electric_arc_furnace"),
                    ElectricArcFurnaceRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progress;

    public ElectricArcFurnaceRecipeCategory(IGuiHelper guiHelper) {

        // Unified background slice
        this.background = guiHelper.createDrawable(
                ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/generic_jei_background.png"),
                3, 4, 170, 80
        );

        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get())
        );

        // Same progress bar slice used in other machines
        this.progress = guiHelper.drawableBuilder(
                ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/generic_jei_background.png"),
                192, 0, 37, 10
        ).buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void draw(ElectricArcFurnaceRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
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
    public RecipeType<ElectricArcFurnaceRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Electric Arc Furnace");
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
                          ElectricArcFurnaceRecipe recipe,
                          IFocusGroup focuses) {

        int rowY = 13;

        // -----------------------------
        // ITEM INPUTS (independent positions)
        // -----------------------------
        int inputX = 5;

        for (ItemStack in : recipe.getItemInputs()) {
            builder.addSlot(RecipeIngredientRole.INPUT, inputX, rowY)
                    .addItemStack(in)
                    .addTooltipCallback((slotView, tooltip) ->
                            tooltip.add(Component.literal("Required: " + in.getCount()))
                    );
            inputX += 18; // adjust spacing if needed
        }

        // -----------------------------
        // ITEM OUTPUTS (independent positions)
        // -----------------------------
        int outputX = 113;
        int outY = rowY;

        for (ItemStack out : recipe.getItemOutputs()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, outputX, outY)
                    .addItemStack(out)
                    .addTooltipCallback((slotView, tooltip) -> {
                        tooltip.add(Component.literal("Output: " + out.getCount()));
                    });
            outY += 20;
        }
    }
}
