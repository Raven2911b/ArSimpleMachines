package com.raven.arsimplemachines.registry;

import com.raven.arsimplemachines.ArSimpleMachines;

import com.raven.arsimplemachines.recipe.gaspad.GasChargeRecipe;
import com.raven.arsimplemachines.recipe.gaspad.GasChargeRecipeSerializer;

import com.raven.arsimplemachines.recipe.lathe.LatheRecipe;
import com.raven.arsimplemachines.recipe.lathe.LatheRecipeSerializer;
import com.raven.arsimplemachines.recipe.lathe.LatheRecipeType;

import com.raven.arsimplemachines.recipe.roller.RollingRecipe;
import com.raven.arsimplemachines.recipe.roller.RollingRecipeSerializer;
import com.raven.arsimplemachines.recipe.roller.RollingRecipeType;

import com.raven.arsimplemachines.recipe.chemical.ChemicalReactorRecipe;
import com.raven.arsimplemachines.recipe.chemical.ChemicalReactorRecipeSerializer;
import com.raven.arsimplemachines.recipe.chemical.ChemicalReactorRecipeType;

import com.raven.arsimplemachines.recipe.electrolyzer.ElectrolyzerRecipe;
import com.raven.arsimplemachines.recipe.electrolyzer.ElectrolyzerRecipeSerializer;
import com.raven.arsimplemachines.recipe.electrolyzer.ElectrolyzerRecipeType;

import com.raven.arsimplemachines.recipe.eaf.ElectricArcFurnaceRecipe;
import com.raven.arsimplemachines.recipe.eaf.ElectricArcFurnaceRecipeSerializer;
import com.raven.arsimplemachines.recipe.eaf.ElectricArcFurnaceRecipeType;

import com.raven.arsimplemachines.recipe.crystallizer.CrystallizerRecipe;
import com.raven.arsimplemachines.recipe.crystallizer.CrystallizerRecipeSerializer;
import com.raven.arsimplemachines.recipe.crystallizer.CrystallizerRecipeType;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModRecipeTypes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, ArSimpleMachines.MODID);

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ArSimpleMachines.MODID);

    // ---------------------------------------------------------
    // GAS CHARGE PAD (old style, already working)
    // ---------------------------------------------------------
    public static final DeferredHolder<RecipeType<?>, RecipeType<GasChargeRecipe>> GAS_CHARGE_TYPE =
            RECIPE_TYPES.register("gas_charge",
                    () -> new RecipeType<GasChargeRecipe>() {
                        @Override
                        public String toString() {
                            return ArSimpleMachines.MODID + ":gas_charge";
                        }
                    });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GasChargeRecipe>> GAS_CHARGE_SERIALIZER =
            SERIALIZERS.register("gas_charge", GasChargeRecipeSerializer::new);

    // ---------------------------------------------------------
    // LATHE (old pattern that your code already uses)
    // ---------------------------------------------------------
    public static final RecipeType<LatheRecipe> LATHE_TYPE = new LatheRecipeType();

    static {
        RECIPE_TYPES.register("lathe", () -> LATHE_TYPE);
    }

    public static final RecipeSerializer<LatheRecipe> LATHE_SERIALIZER = new LatheRecipeSerializer();

    static {
        SERIALIZERS.register("lathe", () -> LATHE_SERIALIZER);
    }

    // ---------------------------------------------------------
    // ROLLING MACHINE (same pattern as Lathe, so existing calls work)
    // ---------------------------------------------------------
    public static final RecipeType<RollingRecipe> ROLLING_TYPE = RollingRecipeType.INSTANCE;

    static {
        RECIPE_TYPES.register("rolling", () -> ROLLING_TYPE);
    }

    public static final RecipeSerializer<RollingRecipe> ROLLING_SERIALIZER = new RollingRecipeSerializer();

    static {
        SERIALIZERS.register("rolling", () -> ROLLING_SERIALIZER);
    }

    // ---------------------------------------------------------
    // CHEMICAL REACTOR (existing DeferredHolder style)
    // ---------------------------------------------------------
    public static final DeferredHolder<RecipeType<?>, RecipeType<ChemicalReactorRecipe>> CHEMICAL_REACTOR_TYPE =
            RECIPE_TYPES.register("chemical", () -> ChemicalReactorRecipeType.INSTANCE);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ChemicalReactorRecipe>> CHEMICAL_REACTOR_SERIALIZER =
            SERIALIZERS.register("chemical", ChemicalReactorRecipeSerializer::new);

    // ---------------------------------------------------------
    // ELECTROLYZER (existing DeferredHolder style)
    // ---------------------------------------------------------
    public static final DeferredHolder<RecipeType<?>, RecipeType<ElectrolyzerRecipe>> ELECTROLYZER_TYPE =
            RECIPE_TYPES.register("electrolyzer", () -> ElectrolyzerRecipeType.INSTANCE);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ElectrolyzerRecipe>> ELECTROLYZER_SERIALIZER =
            SERIALIZERS.register("electrolyzer", ElectrolyzerRecipeSerializer::new);

    // ---------------------------------------------------------
    // ELECTRIC ARC FURNACE
    // ---------------------------------------------------------
    public static final RecipeType<ElectricArcFurnaceRecipe> ELECTRIC_ARC_FURNACE_TYPE =
            ElectricArcFurnaceRecipeType.INSTANCE;

    static {
        RECIPE_TYPES.register("electric_arc_furnace", () -> ELECTRIC_ARC_FURNACE_TYPE);
    }

    public static final RecipeSerializer<ElectricArcFurnaceRecipe> ELECTRIC_ARC_FURNACE_SERIALIZER =
            new ElectricArcFurnaceRecipeSerializer();

    static {
        SERIALIZERS.register("electric_arc_furnace", () -> ELECTRIC_ARC_FURNACE_SERIALIZER);
    }

    // ---------------------------------------------------------
// CRYSTALLIZER (same pattern as Rolling)
// ---------------------------------------------------------
    public static final RecipeType<CrystallizerRecipe> CRYSTALLIZER_TYPE =
            CrystallizerRecipeType.INSTANCE;

    static {
        RECIPE_TYPES.register("crystallizer", () -> CRYSTALLIZER_TYPE);
    }

    public static final RecipeSerializer<CrystallizerRecipe> CRYSTALLIZER_SERIALIZER =
            new CrystallizerRecipeSerializer();

    static {
        SERIALIZERS.register("crystallizer", () -> CRYSTALLIZER_SERIALIZER);
    }

    // ---------------------------------------------------------
    // REGISTER
    // ---------------------------------------------------------
    public static void register(net.neoforged.bus.api.IEventBus bus) {
        RECIPE_TYPES.register(bus);
        SERIALIZERS.register(bus);
    }
}
