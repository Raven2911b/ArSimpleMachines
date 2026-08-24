package com.raven.arsimplemachines.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.raven.arsimplemachines.recipe.TagInput;
import com.raven.arsimplemachines.recipe.lathe.LatheRecipe;
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

public class LatheRecipeCategory implements IRecipeCategory<LatheRecipe> {

    public static final RecipeType<LatheRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath("arsimplemachines", "lathe"), LatheRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progress;

    public LatheRecipeCategory(IGuiHelper guiHelper) {
        //this.background = guiHelper.createBlankDrawable(150, 60);

        this.background = guiHelper.createDrawable(
                ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/generic_jei_background.png"),
                3, 4, 170, 80
        );

        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.LATHE_CONTROLLER.get()));
        this.progress = guiHelper.drawableBuilder(
                ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/generic_jei_background.png"),
                192, 0, 37, 10   // adjust based on your texture
        ).buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
    }
    @Override
    public void draw(LatheRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {

        // Draw animated progress bar
        progress.draw(graphics, 65, 40);

        // Draw power text
        graphics.drawString(
                Minecraft.getInstance().font,
                "Power: " + recipe.getEnergyPerTick() + " RF/t",
                2, 85,
                0x404040,
                false
        );

        // Draw time text
        graphics.drawString(
                Minecraft.getInstance().font,
                "Time: " + (recipe.getProcessingTime() / 20) + " s",
                120, 85,
                0x404040,
                false
        );
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
        int itemInputX = 5;
        int outputX    = 113;
        int rowY       = 13;

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
