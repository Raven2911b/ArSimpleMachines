package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.recipe.chemical.ChemicalReactorRecipe;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class ChemicalReactorRecipeCategory implements IRecipeCategory<ChemicalReactorRecipe> {

    public static final RecipeType<ChemicalReactorRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath("arsimplemachines", "chemical"),
                    ChemicalReactorRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progress;

    public ChemicalReactorRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(
                ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/generic_jei_background.png"),
                3, 4, 170, 80   // adjust slice to match your PNG
        );
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CHEMICAL_REACTOR_CONTROLLER.get()));
        this.progress = guiHelper.drawableBuilder(
                ResourceLocation.fromNamespaceAndPath("arsimplemachines", "textures/gui/generic_jei_background.png"),
                192, 0, 37, 10   // use the same bar unless you want a different one
        ).buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);



    }
    @Override
    public void draw(ChemicalReactorRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
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
    public RecipeType<ChemicalReactorRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Chemical Reactor");
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
                          ChemicalReactorRecipe recipe,
                          IFocusGroup focuses) {

        FluidStack a = recipe.getFluidA();
        FluidStack b = recipe.getFluidB();
        FluidStack out = recipe.getOutput();

        List<ChemicalReactorRecipe.FluidTagInput> tagsA = recipe.getFluidATags();
        List<ChemicalReactorRecipe.FluidTagInput> tagsB = recipe.getFluidBTags();

        // -----------------------------
        // FLUID INPUT A (direct or tag)
        // -----------------------------
        if (!tagsA.isEmpty()) {
            ChemicalReactorRecipe.FluidTagInput tag = tagsA.get(0);
            TagKey<Fluid> key = TagKey.create(BuiltInRegistries.FLUID.key(), tag.tag());

            var fluids = BuiltInRegistries.FLUID.getTag(key)
                    .map(set -> set.stream().map(holder -> holder.value()).toList())
                    .orElse(List.of());

            var slot = builder.addSlot(RecipeIngredientRole.INPUT, 5, 13);

            for (Fluid f : fluids) {
                slot.addFluidStack(f, tag.amount());
            }

            slot.addRichTooltipCallback((slotView, tooltip) -> {
                tooltip.add(Component.literal("Input A (tag): " + tag.tag()));
                tooltip.add(Component.literal("Required: " + tag.amount() + " mB"));
            });

        } else {
            builder.addSlot(RecipeIngredientRole.INPUT, 5, 13)
                    .addFluidStack(a.getFluid(), a.getAmount())
                    .addRichTooltipCallback((slot, tooltip) -> {
                        tooltip.add(Component.literal("Input A: " + a.getAmount() + " mB"));
                    });
        }

        // -----------------------------
        // FLUID INPUT B (direct or tag)
        // -----------------------------
        if (!tagsB.isEmpty()) {
            ChemicalReactorRecipe.FluidTagInput tag = tagsB.get(0);
            TagKey<Fluid> key = TagKey.create(BuiltInRegistries.FLUID.key(), tag.tag());

            var fluids = BuiltInRegistries.FLUID.getTag(key)
                    .map(set -> set.stream().map(holder -> holder.value()).toList())
                    .orElse(List.of());

            var slot = builder.addSlot(RecipeIngredientRole.INPUT, 23, 13);

            for (Fluid f : fluids) {
                slot.addFluidStack(f, tag.amount());
            }

            slot.addRichTooltipCallback((slotView, tooltip) -> {
                tooltip.add(Component.literal("Input B (tag): " + tag.tag()));
                tooltip.add(Component.literal("Required: " + tag.amount() + " mB"));
            });

        } else {
            builder.addSlot(RecipeIngredientRole.INPUT, 23, 13)
                    .addFluidStack(b.getFluid(), b.getAmount())
                    .addRichTooltipCallback((slot, tooltip) -> {
                        tooltip.add(Component.literal("Input B: " + b.getAmount() + " mB"));
                    });
        }

        // -----------------------------
        // FLUID OUTPUT
        // -----------------------------
        builder.addSlot(RecipeIngredientRole.OUTPUT, 113, 13)
                .addFluidStack(out.getFluid(), out.getAmount())
                .addRichTooltipCallback((slot, tooltip) -> {
                    tooltip.add(Component.literal("Output: " + out.getAmount() + " mB"));
                    tooltip.add(Component.literal("Time: " + recipe.getProcessingTime() + " ticks"));
                    tooltip.add(Component.literal("Energy: " + recipe.getEnergyPerTick() + " RF/t"));
                });
    }
}
