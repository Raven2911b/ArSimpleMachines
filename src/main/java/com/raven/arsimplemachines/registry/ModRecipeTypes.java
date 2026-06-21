package com.raven.arsimplemachines.registry;

import com.raven.arsimplemachines.ArSimpleMachines;
import com.raven.arsimplemachines.recipe.GasChargeRecipe;
import com.raven.arsimplemachines.recipe.GasChargeRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipeTypes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, ArSimpleMachines.MODID);

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ArSimpleMachines.MODID);

    // -----------------------------
    // RECIPE TYPE
    // -----------------------------
    public static final DeferredHolder<RecipeType<?>, RecipeType<GasChargeRecipe>> GAS_CHARGE_TYPE =
            RECIPE_TYPES.register("gas_charge",
                    () -> new RecipeType<GasChargeRecipe>() {
                        public String toString() {
                            return ArSimpleMachines.MODID + ":gas_charge";
                        }
                    });

    // -----------------------------
    // SERIALIZER
    // -----------------------------
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GasChargeRecipe>> GAS_CHARGE_SERIALIZER =
            SERIALIZERS.register("gas_charge", GasChargeRecipeSerializer::new);

}
