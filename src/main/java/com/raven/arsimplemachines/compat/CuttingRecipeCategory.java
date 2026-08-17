package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.recipe.cutter.CuttingMachineRecipe;
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

public class CuttingRecipeCategory implements IRecipeCategory<CuttingMachineRecipe> {

    public static final RecipeType<CuttingMachineRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath("arsimplemachines", "cutting"), CuttingMachineRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public CuttingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 60);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CUTTING_MACHINE_CONTROLLER.get()));
    }

    @Override
    public RecipeType<CuttingMachineRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Cutting Machine");
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
    public void setRecipe(IRecipeLayoutBuilder builder, CuttingMachineRecipe recipe, IFocusGroup focuses) {

        int itemInputX = 20;
        int outputX     = 100;
        int rowY        = 10;

        // ---------------------------------------------------------
        // TAG INPUT (Cutting Machine is tag-only)
        // ---------------------------------------------------------
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

        // ---------------------------------------------------------
        // OUTPUT
        // ---------------------------------------------------------
        if (!recipe.getItemOutputs().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, outputX, rowY)
                    .addItemStack(recipe.getItemOutputs().get(0));
        }

        // ---------------------------------------------------------
        // POWER + TIME TEXT (bottom row)
        // ---------------------------------------------------------
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 40)
                .addItemStack(ItemStack.EMPTY).addRichTooltipCallback((slotView, tooltip) -> {
                    tooltip.clear();
                    tooltip.add(Component.literal("Power: " + recipe.getEnergyPerTick() + " RF/t"));
                    tooltip.add(Component.literal("Time: " + (recipe.getProcessingTime() / 20) + " s"));
                });
    }
}
