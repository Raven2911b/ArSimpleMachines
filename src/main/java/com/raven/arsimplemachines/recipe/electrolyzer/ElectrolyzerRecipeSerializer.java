package com.raven.arsimplemachines.recipe.electrolyzer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import net.minecraft.world.item.crafting.RecipeSerializer;

import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Serializer for Electrolyzer recipes.
 *
 * JSON FORMAT:
 *
 * {
 *   "input":   { "id": "mod:water", "amount": 1000 },
 *   "outputA": { "id": "mod:hydrogen", "amount": 500 },
 *   "outputB": { "id": "mod:oxygen",   "amount": 500 },
 *
 *   "processing_time": 200,
 *   "energy_per_tick": 50
 * }
 */
public class ElectrolyzerRecipeSerializer implements RecipeSerializer<ElectrolyzerRecipe> {

    // ---------------------------------------------------------
    //  JSON CODEC
    // ---------------------------------------------------------

    private static final Codec<FluidStack> FLUID_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(fs ->
                            BuiltInRegistries.FLUID.getKey(fs.getFluid())
                    ),
                    Codec.INT.fieldOf("amount").forGetter(FluidStack::getAmount)
            ).apply(instance, (id, amount) ->
                    new FluidStack(BuiltInRegistries.FLUID.get(id), amount)
            )
    );

    private static final MapCodec<ElectrolyzerRecipe> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(

                            FLUID_CODEC.fieldOf("input").forGetter(r -> r.getInput()),
                            FLUID_CODEC.fieldOf("outputA").forGetter(r -> r.getOutputA()),
                            FLUID_CODEC.fieldOf("outputB").forGetter(r -> r.getOutputB()),

                            Codec.INT.fieldOf("processing_time").forGetter(ElectrolyzerRecipe::getProcessingTime),
                            Codec.INT.fieldOf("energy_per_tick").forGetter(ElectrolyzerRecipe::getEnergyPerTick)

                    ).apply(instance, (input, outA, outB, time, energy) ->
                            new ElectrolyzerRecipe(
                                    null, // ID assigned later by RecipeManager
                                    input,
                                    outA,
                                    outB,
                                    time,
                                    energy
                            )
                    )
            );

    @Override
    public MapCodec<ElectrolyzerRecipe> codec() {
        return CODEC;
    }

    // ---------------------------------------------------------
    //  NETWORK CODEC
    // ---------------------------------------------------------

    private static final StreamCodec<RegistryFriendlyByteBuf, ElectrolyzerRecipe> STREAM_CODEC =
            StreamCodec.of(
                    (buf, recipe) -> {

                        // Input
                        buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(recipe.getInput().getFluid()));
                        buf.writeInt(recipe.getInput().getAmount());

                        // Output A
                        buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(recipe.getOutputA().getFluid()));
                        buf.writeInt(recipe.getOutputA().getAmount());

                        // Output B
                        buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(recipe.getOutputB().getFluid()));
                        buf.writeInt(recipe.getOutputB().getAmount());

                        // Time + energy
                        buf.writeInt(recipe.getProcessingTime());
                        buf.writeInt(recipe.getEnergyPerTick());
                    },

                    buf -> {

                        // Input
                        var inId = buf.readResourceLocation();
                        int inAmount = buf.readInt();
                        FluidStack input = new FluidStack(
                                BuiltInRegistries.FLUID.get(inId),
                                inAmount
                        );

                        // Output A
                        var outAId = buf.readResourceLocation();
                        int outAAmount = buf.readInt();
                        FluidStack outputA = new FluidStack(
                                BuiltInRegistries.FLUID.get(outAId),
                                outAAmount
                        );

                        // Output B
                        var outBId = buf.readResourceLocation();
                        int outBAmount = buf.readInt();
                        FluidStack outputB = new FluidStack(
                                BuiltInRegistries.FLUID.get(outBId),
                                outBAmount
                        );

                        // Time + energy
                        int time = buf.readInt();
                        int energy = buf.readInt();

                        return new ElectrolyzerRecipe(
                                null,
                                input,
                                outputA,
                                outputB,
                                time,
                                energy
                        );
                    }
            );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ElectrolyzerRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
