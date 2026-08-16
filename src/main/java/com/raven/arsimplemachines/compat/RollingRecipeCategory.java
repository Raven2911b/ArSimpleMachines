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

        // -----------------------------
        // SLOT POSITIONS (matches old AR)
        // -----------------------------
        int itemInputX = 20;
        int fluidInputX = 60;
        int outputX     = 100;
        int rowY        = 10;

        // -----------------------------
        // ITEM INPUTS
        // -----------------------------
        if (!recipe.getItemInputs().isEmpty()) {
            ItemStack stack = recipe.getItemInputs().get(0);

            builder.addSlot(RecipeIngredientRole.INPUT, itemInputX, rowY)
                    .addItemStack(stack)
                    .addRichTooltipCallback((slotView, tooltip) ->
                            tooltip.add(Component.literal("Required: " + stack.getCount()))
                    );
        }

        // -----------------------------
        // TAG-BASED ITEM INPUTS
        // (if recipe uses tags instead of direct items)
        // -----------------------------
        if (!recipe.getItemTags().isEmpty()) {

            var tagInput = recipe.getItemTags().get(0);

            TagKey<Item> tagKey = TagKey.create(
                    BuiltInRegistries.ITEM.key(),
                    tagInput.tag()
            );

            Ingredient ingredient = Ingredient.of(tagKey);

            builder.addSlot(RecipeIngredientRole.INPUT, itemInputX, rowY)
                    .addIngredients(ingredient)
                    .addRichTooltipCallback((slotView, tooltip) -> {
                        tooltip.add(Component.literal("Tag: " + tagInput.tag()));
                        tooltip.add(Component.literal("Required: " + tagInput.count()));
                    });
        }

        // -----------------------------
        // FLUID INPUT (center slot)
        // -----------------------------
        if (!recipe.getFluidInputs().isEmpty()) {
            FluidStack fs = recipe.getFluidInputs().get(0);

            builder.addSlot(RecipeIngredientRole.INPUT, fluidInputX, rowY)
                    .addFluidStack(fs.getFluid(), fs.getAmount())
                    .addRichTooltipCallback((slotView, tooltip) ->
                            tooltip.add(Component.literal("Required: " + fs.getAmount() + " mB"))
                    );
        }

        // -----------------------------
        // OUTPUT (right slot)
        // -----------------------------
        if (!recipe.getItemOutputs().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, outputX, rowY)
                    .addItemStack(recipe.getItemOutputs().get(0));
        }

        if (!recipe.getFluidOutputs().isEmpty()) {
            FluidStack fs = recipe.getFluidOutputs().get(0);

            builder.addSlot(RecipeIngredientRole.OUTPUT, outputX, rowY + 20)
                    .addFluidStack(fs.getFluid(), fs.getAmount());
        }

        // -----------------------------
        // POWER + TIME TEXT (bottom row)
        // -----------------------------
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 40)
                .addItemStack(ItemStack.EMPTY)
                .addTooltipCallback((slotView, tooltip) -> {
                    tooltip.clear();
                    tooltip.add(Component.literal("Power: " + recipe.getEnergyPerTick() + " RF/t"));
                    tooltip.add(Component.literal("Time: " + (recipe.getProcessingTime() / 20) + " s"));
                });
    }

}
