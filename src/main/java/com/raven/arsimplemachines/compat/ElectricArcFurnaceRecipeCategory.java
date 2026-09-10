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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;

public class ElectricArcFurnaceRecipeCategory implements IRecipeCategory<ElectricArcFurnaceRecipe> {

    public static final RecipeType<ElectricArcFurnaceRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath("arsimplemachines", "electric_arc_furnace"),
                    ElectricArcFurnaceRecipe.class);
    private static final ResourceLocation ARC_PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/progressbars.png");

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
                ARC_PROGRESS_TEXTURE,
                42, 66,     // U, V of the FILLED portion
                42, 42      // width, height
        ).buildAnimated(200, IDrawableAnimated.StartDirection.BOTTOM , false);

    }

    @Override
    public void draw(ElectricArcFurnaceRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {

        // Progress bar
        graphics.blit(
                ARC_PROGRESS_TEXTURE,
                65, 20,     // JEI position (match your GUI or adjust)
                0, 66,      // U, V of EMPTY frame
                42, 42
        );
// Animated fill
        progress.draw(graphics, 65, 20);


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
        int inputX = 5;

        // ITEM INPUTS
        for (ItemStack in : recipe.getItemInputs()) {
            builder.addSlot(RecipeIngredientRole.INPUT, inputX, rowY)
                    .addItemStack(in)
                    .addTooltipCallback((slotView, tooltip) ->
                            tooltip.add(Component.literal("Required: " + in.getCount()))
                    );
            inputX += 18;
        }

        // -----------------------------
        // TAG INPUTS
        // -----------------------------
        for (var tagInput : recipe.getItemTags()) {

            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagInput.tag());

            builder.addSlot(RecipeIngredientRole.INPUT, inputX, rowY)
                    .addIngredients(Ingredient.of(tagKey))
                    .addTooltipCallback((slotView, tooltip) -> {
                        tooltip.add(Component.literal("Tag: " + tagInput.tag()));
                        tooltip.add(Component.literal("Required: " + tagInput.count()));
                    });

            inputX += 18;
        }


        // ITEM OUTPUTS
        int outputX = 113;
        int outY = rowY;

        for (ItemStack out : recipe.getItemOutputs()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, outputX, outY)
                    .addItemStack(out)
                    .addTooltipCallback((slotView, tooltip) ->
                            tooltip.add(Component.literal("Output: " + out.getCount()))
                    );
            outY += 20;
        }
    }

}
