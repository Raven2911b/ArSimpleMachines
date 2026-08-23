package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.recipe.TagInput;
import com.raven.arsimplemachines.recipe.precision.PrecisionAssemblerRecipe;
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
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class PrecisionRecipeCategory implements IRecipeCategory<PrecisionAssemblerRecipe> {

    public static final RecipeType<PrecisionAssemblerRecipe> TYPE =
            new RecipeType<>(
                    ResourceLocation.fromNamespaceAndPath("arsimplemachines", "precision_assembler"),
                    PrecisionAssemblerRecipe.class
            );

    private final IDrawable background;
    private final IDrawable icon;

    public PrecisionRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 90);
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModBlocks.PRECISION_ASSEMBLER_CONTROLLER.get())
        );
    }

    @Override
    public RecipeType<PrecisionAssemblerRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Precision Assembler");
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
                          PrecisionAssemblerRecipe recipe,
                          IFocusGroup focuses) {

        int x = 10;
        int y = 15;

        // ---------------------------------------------------------
        // ITEM INPUTS
        // ---------------------------------------------------------
        for (ItemStack in : recipe.getItemInputs()) {
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addItemStack(in)
                    .addTooltipCallback((slot, tooltip) -> {
                        tooltip.add(Component.literal("Item Input: " + in.getCount()));
                    });
            x += 20;
        }

        // ---------------------------------------------------------
        // TAG INPUTS (NO TagInput.java changes needed)
        // ---------------------------------------------------------
        for (TagInput tag : recipe.getItemTags()) {

            TagKey<Item> tagKey = TagKey.create(
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.key(),
                    tag.tag()
            );

            Ingredient tagIngredient = Ingredient.of(tagKey);

            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addIngredients(tagIngredient)
                    .addTooltipCallback((slot, tooltip) -> {
                        tooltip.add(Component.literal("Tag Input: #" + tag.tag()));
                        tooltip.add(Component.literal("Count: " + tag.count()));
                    });

            x += 20;
        }

        // ---------------------------------------------------------
        // OUTPUTS
        // ---------------------------------------------------------
        x = 10;
        y = 55;

        for (ItemStack out : recipe.getItemOutputs()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .addItemStack(out)
                    .addTooltipCallback((slot, tooltip) -> {
                        tooltip.add(Component.literal("Output: " + out.getCount()));
                        tooltip.add(Component.literal("Time: " + recipe.getProcessingTime() + " ticks"));
                        tooltip.add(Component.literal("Energy: " + recipe.getEnergyPerTick() + " RF/t"));
                    });
            x += 20;
        }
    }
}
