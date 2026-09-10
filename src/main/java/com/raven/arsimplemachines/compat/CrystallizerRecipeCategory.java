package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.recipe.crystallizer.CrystallizerRecipe;
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

public class CrystallizerRecipeCategory implements IRecipeCategory<CrystallizerRecipe> {

    public static final RecipeType<CrystallizerRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath("arsimplemachines", "crystallizer"),
                    CrystallizerRecipe.class);
    private static final ResourceLocation CRYSTALLIZER_PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/progressbars.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progress;

    public CrystallizerRecipeCategory(IGuiHelper guiHelper) {

        // Unified background slice
        this.background = guiHelper.createDrawable(
                ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/generic_jei_background.png"),
                3, 4, 170, 80
        );

        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModBlocks.CRYSTALLIZER_CONTROLLER.get())
        );
        this.progress = guiHelper.drawableBuilder(
                CRYSTALLIZER_PROGRESS_TEXTURE,
                31, 0,        // U, V of the FILLED portion start
                23, 50       // width, height
        ).buildAnimated(200, IDrawableAnimated.StartDirection.BOTTOM, false);
    }

    @Override
    public void draw(CrystallizerRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        int barX = 68;
        int barY = 5;   // moved up from 40 → 28

// Static frame
        graphics.blit(
                CRYSTALLIZER_PROGRESS_TEXTURE,
                barX, barY,
                0, 0,
                31, 65
        );

// Animated overlay
        progress.draw(graphics,
                barX + 4,
                barY + 16
        );
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
    public RecipeType<CrystallizerRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Crystallizer");
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
                          CrystallizerRecipe recipe,
                          IFocusGroup focuses) {

        int rowY = 13;
        int outputX = 113;

        // -----------------------------
        // ITEM INPUT 1
        // -----------------------------
        if (recipe.getItemInputs().size() > 0) {
            ItemStack in1 = recipe.getItemInputs().get(0);
            builder.addSlot(RecipeIngredientRole.INPUT, 5, rowY)
                    .addItemStack(in1)
                    .addTooltipCallback((slotView, tooltip) ->
                            tooltip.add(Component.literal("Required: " + in1.getCount()))
                    );
        }

        // -----------------------------
        // ITEM INPUT 2
        // -----------------------------
        if (recipe.getItemInputs().size() > 1) {
            ItemStack in2 = recipe.getItemInputs().get(1);
            builder.addSlot(RecipeIngredientRole.INPUT, 23, rowY)
                    .addItemStack(in2)
                    .addTooltipCallback((slotView, tooltip) ->
                            tooltip.add(Component.literal("Required: " + in2.getCount()))
                    );
        }

        // -----------------------------
        // FLUID INPUT
        // -----------------------------
        if (!recipe.getFluidInputs().isEmpty()) {
            FluidStack fluidIn = recipe.getFluidInputs().get(0);

            // Adjust this X/Y independently
            builder.addSlot(RecipeIngredientRole.INPUT, 41, rowY)
                    .addFluidStack(fluidIn.getFluid(), fluidIn.getAmount())
                    .addTooltipCallback((slotView, tooltip) ->
                            tooltip.add(Component.literal("Required: " + fluidIn.getAmount() + " mB"))
                    );
        }

        // -----------------------------
        // OUTPUTS
        // -----------------------------
        int outY = rowY;

        for (ItemStack out : recipe.getItemOutputs()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, outputX, outY)
                    .addItemStack(out);
            outY += 20;
        }

        for (FluidStack fs : recipe.getFluidOutputs()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, outputX, outY)
                    .addFluidStack(fs.getFluid(), fs.getAmount());
            outY += 20;
        }
    }

}
