package com.raven.arsimplemachines.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;

public class GasChargeRecipeSerializer implements RecipeSerializer<GasChargeRecipe> {

    private static final MapCodec<GasChargeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(GasChargeRecipe::getId),
                    ResourceLocation.CODEC.fieldOf("fluid").forGetter(r -> BuiltInRegistries.FLUID.getKey(r.getFluid())),
                    Codec.INT.fieldOf("amount").forGetter(GasChargeRecipe::getFluidAmount),
                    Codec.INT.fieldOf("processing_time").forGetter(GasChargeRecipe::getProcessingTime)
            ).apply(instance, (id, fluidId, amount, time) ->
                    new GasChargeRecipe(id, BuiltInRegistries.FLUID.get(fluidId), amount, time)
            )
    );

    @Override
    public MapCodec<GasChargeRecipe> codec() {
        return CODEC;
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, GasChargeRecipe> STREAM_CODEC =
            StreamCodec.of(
                    (buf, recipe) -> {
                        buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(recipe.getFluid()));
                        buf.writeInt(recipe.getFluidAmount());
                        buf.writeInt(recipe.getProcessingTime());
                    },
                    buf -> {
                        ResourceLocation fluidId = buf.readResourceLocation();
                        Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);
                        int amount = buf.readInt();
                        int time = buf.readInt();
                        return new GasChargeRecipe(null, fluid, amount, time);
                    }
            );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, GasChargeRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
