package com.raven.arsimplemachines.compat;

import com.raven.arsimplemachines.registry.ModBlocks;
import com.raven.arsimplemachines.registry.ModRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class ArSMJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("arsimplemachines", "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new RollingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new LatheRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new GasChargeRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new CrystallizerRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new CuttingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new ChemicalReactorRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new ElectrolyzerRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new ElectricArcFurnaceRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new PrecisionRecipeCategory(registration.getJeiHelpers().getGuiHelper())

        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        var level = Minecraft.getInstance().level;
        if (level == null) return;

        // Rolling
        var rollingRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.ROLLING_TYPE)
                .stream().map(holder -> holder.value()).toList();

        registration.addRecipes(RollingRecipeCategory.TYPE, rollingRecipes);

        // Lathe
        var latheRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.LATHE_TYPE)
                .stream().map(holder -> holder.value()).toList();

        registration.addRecipes(LatheRecipeCategory.TYPE, latheRecipes);

        // Gas Charge Pad
        var gasChargeRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.GAS_CHARGE_TYPE.get())
                .stream().map(holder -> holder.value()).toList();

        registration.addRecipes(GasChargeRecipeCategory.RECIPE_TYPE, gasChargeRecipes);

        // Crystallizer
        var crystallizerRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.CRYSTALLIZER_TYPE)
                .stream().map(holder -> holder.value()).toList();

        registration.addRecipes(CrystallizerRecipeCategory.TYPE, crystallizerRecipes);

        // ⭐ Cutting Machine
        var cuttingRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.CUTTING_MACHINE_TYPE)
                .stream().map(holder -> holder.value()).toList();

        registration.addRecipes(CuttingRecipeCategory.TYPE, cuttingRecipes);
        var chemRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.CHEMICAL_REACTOR_TYPE.get())
                .stream().map(holder -> holder.value()).toList();

        registration.addRecipes(ChemicalReactorRecipeCategory.TYPE, chemRecipes);

        var electroRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.ELECTROLYZER_TYPE.get())
                .stream().map(holder -> holder.value()).toList();

        registration.addRecipes(ElectrolyzerRecipeCategory.TYPE, electroRecipes);

        var eafRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.ELECTRIC_ARC_FURNACE_TYPE)
                .stream().map(holder -> holder.value()).toList();

        registration.addRecipes(ElectricArcFurnaceRecipeCategory.TYPE, eafRecipes);
        var paRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.PRECISION_ASSEMBLER_TYPE)
                .stream().map(holder -> holder.value()).toList();

        registration.addRecipes(PrecisionRecipeCategory.TYPE, paRecipes);

    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {

        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.ROLLING_CONTROLLER.get()),
                RollingRecipeCategory.TYPE
        );

        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.LATHE_CONTROLLER.get()),
                LatheRecipeCategory.TYPE
        );

        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.GAS_CHARGE_PAD.get()),
                GasChargeRecipeCategory.RECIPE_TYPE
        );

        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.CRYSTALLIZER_CONTROLLER.get()),
                CrystallizerRecipeCategory.TYPE
        );

        // ⭐ Cutting Machine catalyst
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.CUTTING_MACHINE_CONTROLLER.get()),
                CuttingRecipeCategory.TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.CHEMICAL_REACTOR_CONTROLLER.get()),
                ChemicalReactorRecipeCategory.TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.ELECTROLYZER_CONTROLLER.get()),
                ElectrolyzerRecipeCategory.TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get()),
                ElectricArcFurnaceRecipeCategory.TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.PRECISION_ASSEMBLER_CONTROLLER.get()),
                PrecisionRecipeCategory.TYPE
        );

    }
}
