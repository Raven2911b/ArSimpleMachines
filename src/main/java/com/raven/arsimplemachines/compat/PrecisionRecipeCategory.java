package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.recipe.TagInput;
import com.raven.arsimplemachines.recipe.precision.PrecisionAssemblerRecipe;
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
    private final IDrawableAnimated progress;

    public PrecisionRecipeCategory(IGuiHelper guiHelper) {

        // Unified background slice
        this.background = guiHelper.createDrawable(
                ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/generic_jei_background.png"),
                3, 4, 170, 80
        );

        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModBlocks.PRECISION_ASSEMBLER_CONTROLLER.get())
        );

        // Same progress bar slice used in other machines
        this.progress = guiHelper.drawableBuilder(
                ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/generic_jei_background.png"),
                192, 0, 37, 10
        ).buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void draw(PrecisionAssemblerRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
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


        // -----------------------------
        // ITEM INPUTS (independent positions)
        // -----------------------------
        int rowY = 13;

        if (recipe.getItemInputs().size() > 0) {
            builder.addSlot(RecipeIngredientRole.INPUT, 5, rowY)
                    .addItemStack(recipe.getItemInputs().get(0));
        }

        if (recipe.getItemInputs().size() > 1) {
            builder.addSlot(RecipeIngredientRole.INPUT, 23, rowY)
                    .addItemStack(recipe.getItemInputs().get(1));
        }

        if (recipe.getItemInputs().size() > 2) {
            builder.addSlot(RecipeIngredientRole.INPUT, 41, rowY)
                    .addItemStack(recipe.getItemInputs().get(2));
        }
        // ITEM INPUT 4
        if (recipe.getItemInputs().size() > 3) {
            builder.addSlot(RecipeIngredientRole.INPUT, 5, rowY +18)
                    .addItemStack(recipe.getItemInputs().get(3));
        }

// TAG INPUTS (continue the row)
        int tagX = 56;

        for (TagInput tag : recipe.getItemTags()) {

            TagKey<Item> tagKey = TagKey.create(
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.key(),
                    tag.tag()
            );

            Ingredient tagIngredient = Ingredient.of(tagKey);

            builder.addSlot(RecipeIngredientRole.INPUT, tagX, rowY)
                    .addIngredients(tagIngredient)
                    .addTooltipCallback((slotView, tooltip) -> {
                        tooltip.add(Component.literal("Tag Input: #" + tag.tag()));
                        tooltip.add(Component.literal("Count: " + tag.count()));
                    });

            tagX += 17; // tighter spacing for tags
        }


        // -----------------------------
        // OUTPUTS (independent positions)
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
