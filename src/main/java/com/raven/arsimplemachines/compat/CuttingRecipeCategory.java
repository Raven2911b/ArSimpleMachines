package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.recipe.cutter.CuttingMachineRecipe;
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

public class CuttingRecipeCategory implements IRecipeCategory<CuttingMachineRecipe> {

    public static final RecipeType<CuttingMachineRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath("arsimplemachines", "cutting"),
                    CuttingMachineRecipe.class);
    private static final ResourceLocation CUTTING_PROGRESS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/progressbars.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progress;

    public CuttingRecipeCategory(IGuiHelper guiHelper) {

        // Unified background slice
        this.background = guiHelper.createDrawable(
                ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/generic_jei_background.png"),
                3, 4, 170, 80
        );

        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModBlocks.CUTTING_MACHINE_CONTROLLER.get())
        );

        this.progress = guiHelper.drawableBuilder(
                CUTTING_PROGRESS_TEXTURE,
                95, 0,      // U, V of fill slice
                37, 35      // full fill size
        ).buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);

    }

    @Override
    public void draw(CuttingMachineRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
// Static frame (same as GUI)
        graphics.blit(
                CUTTING_PROGRESS_TEXTURE,
                65, 15,     // JEI position (matches GUI)
                55, 0,      // U, V of frame
                40, 42      // size of frame
        );

// Animated fill (same offsets as GUI)
        progress.draw(graphics,
                65 + 1,     // X offset inside frame
                15 + 4      // Y offset inside frame
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
    public RecipeType<CuttingMachineRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Cutting Machine");
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
                          CuttingMachineRecipe recipe,
                          IFocusGroup focuses) {

        int rowY = 13;

        // -----------------------------
        // TAG INPUT (single)
        // -----------------------------
        if (!recipe.getItemTags().isEmpty()) {

            var tagInput = recipe.getItemTags().get(0);

            TagKey<Item> tagKey = TagKey.create(
                    BuiltInRegistries.ITEM.key(),
                    tagInput.tag()
            );

            Ingredient ingredient = Ingredient.of(tagKey);

            // Independent X/Y position
            builder.addSlot(RecipeIngredientRole.INPUT, 5, rowY)
                    .addIngredients(ingredient)
                    .addTooltipCallback((slotView, tooltip) -> {
                        tooltip.add(Component.literal("Tag: " + tagInput.tag()));
                        tooltip.add(Component.literal("Required: " + tagInput.count()));
                    });
        }

        // -----------------------------
        // OUTPUT (single)
        // -----------------------------
        if (!recipe.getItemOutputs().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 113, rowY)
                    .addItemStack(recipe.getItemOutputs().get(0));
        }
    }
}
