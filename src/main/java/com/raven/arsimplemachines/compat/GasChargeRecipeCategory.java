package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.recipe.gaspad.GasChargeRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GasChargeRecipeCategory implements IRecipeCategory<GasChargeRecipe> {

    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(ArSimpleMachines.MODID, "gas_charge");

    public static final RecipeType<GasChargeRecipe> RECIPE_TYPE =
            new RecipeType<>(UID, GasChargeRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public GasChargeRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 60);
        this.icon = guiHelper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                new ItemStack(com.raven.arsimplemachines.registry.ModBlocks.GAS_CHARGE_PAD.get())
        );
    }

    @Override
    public RecipeType<GasChargeRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Gas Charge Pad");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    // ⭐ JEI now prefers background via layout builder, but still allows this:
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder,
                          GasChargeRecipe recipe,
                          IFocusGroup focuses) {

        builder.addSlot(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, 20, 20)
                .addFluidStack(recipe.getFluid(), recipe.getFluidAmount());
    }
}
