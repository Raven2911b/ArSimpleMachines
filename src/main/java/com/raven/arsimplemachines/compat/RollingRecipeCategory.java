package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.recipe.roller.RollingRecipe;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

public class RollingRecipeCategory implements IRecipeCategory<RollingRecipe> {

    public static final RecipeType<RollingRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath("arsimplemachines", "rolling"),
                    RollingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progress;

    public RollingRecipeCategory(IGuiHelper guiHelper) {

        // Unified background slice
        this.background = guiHelper.createDrawable(
                ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/generic_jei_background.png"),
                3, 4, 170, 80
        );

        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModBlocks.ROLLING_CONTROLLER.get())
        );

        // Same progress bar slice used in other machines
        this.progress = guiHelper.drawableBuilder(
                ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/generic_jei_background.png"),
                192, 0, 37, 10
        ).buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void draw(RollingRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
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
    public RecipeType<RollingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Rolling Machine");
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
                          RollingRecipe recipe,
                          IFocusGroup focuses) {

        int rowY = 13;

        // ---------------------------------
        // ITEM + TAG INPUTS (side-by-side)
        // ---------------------------------
        int inputX = 5;

        // ITEM INPUTS
        for (ItemStack stack : recipe.getItemInputs()) {
            builder.addSlot(RecipeIngredientRole.INPUT, inputX, rowY)
                    .addItemStack(stack)
                    .addTooltipCallback((slotView, tooltip) ->
                            tooltip.add(Component.literal("Required: " + stack.getCount()))
                    );

            inputX += 18; // move right for next item
        }

        // TAG INPUTS
        for (var tagInput : recipe.getItemTags()) {

            TagKey<Item> tagKey = TagKey.create(
                    BuiltInRegistries.ITEM.key(),
                    tagInput.tag()
            );

            Ingredient ingredient = Ingredient.of(tagKey);

            builder.addSlot(RecipeIngredientRole.INPUT, inputX, rowY)
                    .addIngredients(ingredient)
                    .addTooltipCallback((slotView, tooltip) -> {
                        tooltip.add(Component.literal("Tag: " + tagInput.tag()));
                        tooltip.add(Component.literal("Required: " + tagInput.count()));
                    });

            inputX += 20; // move right for next tag
        }

        // ---------------------------------
        // FLUID INPUT
        // ---------------------------------
        if (!recipe.getFluidInputs().isEmpty()) {

            FluidStack fs = recipe.getFluidInputs().get(0);

            builder.addSlot(RecipeIngredientRole.INPUT, 41, rowY)
                    .addFluidStack(fs.getFluid(), fs.getAmount())
                    .addTooltipCallback((slotView, tooltip) ->
                            tooltip.add(Component.literal("Required: " + fs.getAmount() + " mB"))
                    );
        }

        // ---------------------------------
        // ITEM OUTPUT
        // ---------------------------------
        if (!recipe.getItemOutputs().isEmpty()) {

            builder.addSlot(RecipeIngredientRole.OUTPUT, 113, rowY)
                    .addItemStack(recipe.getItemOutputs().get(0));
        }

        // ---------------------------------
        // FLUID OUTPUT
        // ---------------------------------
        if (!recipe.getFluidOutputs().isEmpty()) {

            FluidStack fs = recipe.getFluidOutputs().get(0);

            builder.addSlot(RecipeIngredientRole.OUTPUT, 113, rowY + 20)
                    .addFluidStack(fs.getFluid(), fs.getAmount());
        }
    }

}
