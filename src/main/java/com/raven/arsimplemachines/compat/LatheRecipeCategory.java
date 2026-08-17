package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.recipe.TagInput;
import com.raven.arsimplemachines.recipe.lathe.LatheRecipe;
import com.raven.arsimplemachines.registry.ModBlocks;

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

public class LatheRecipeCategory implements IRecipeCategory<LatheRecipe> {

    public static final RecipeType<LatheRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath("arsimplemachines", "lathe"), LatheRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public LatheRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 60);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.LATHE_CONTROLLER.get()));
    }

    @Override
    public RecipeType<LatheRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Lathe");
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
    public void setRecipe(IRecipeLayoutBuilder builder, LatheRecipe recipe, IFocusGroup focuses) {

        // -----------------------------
        // SLOT POSITIONS (mirrors Rolling)
        // -----------------------------
        int itemInputX = 20;
        int outputX    = 100;
        int rowY       = 10;

        // -----------------------------
        // ITEM INPUTS (direct)
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
        // TAG INPUTS
        // -----------------------------
        if (!recipe.getItemTags().isEmpty()) {

            TagInput tagInput = recipe.getItemTags().get(0);

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
        // OUTPUTS
        // -----------------------------
        if (!recipe.getItemOutputs().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, outputX, rowY)
                    .addItemStack(recipe.getItemOutputs().get(0));
        }

        // -----------------------------
        // POWER + TIME TEXT (bottom row)
        // -----------------------------
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 40)
                .addItemStack(ItemStack.EMPTY).addRichTooltipCallback((slotView, tooltip) -> {
                    tooltip.clear();
                    tooltip.add(Component.literal("Power: " + recipe.getEnergyPerTick() + " RF/t"));
                    tooltip.add(Component.literal("Time: " + (recipe.getProcessingTime() / 20) + " s"));
                });
    }
}
