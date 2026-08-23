package com.raven.arsimplemachines.recipe.electrolyzer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.raven.arsimplemachines.registry.ModRecipeTypes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import net.minecraft.world.item.crafting.RecipeSerializer;

import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Unified Electrolyzer Serializer
 *
 * JSON FORMAT (unchanged):
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
    //  FLUID CODEC (unchanged)
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

    // ---------------------------------------------------------
    //  JSON CODEC (unified recipe construction)
    // ---------------------------------------------------------
    private static final MapCodec<ElectrolyzerRecipe> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(

                            FLUID_CODEC.fieldOf("input").forGetter(r -> r.getFluidInputs().get(0)),
                            FLUID_CODEC.fieldOf("outputA").forGetter(r -> r.getFluidOutputs().get(0)),
                            FLUID_CODEC.fieldOf("outputB").forGetter(r -> r.getFluidOutputs().get(1)),

                            Codec.INT.fieldOf("processing_time").forGetter(ElectrolyzerRecipe::getProcessingTime),
                            Codec.INT.fieldOf("energy_per_tick").forGetter(ElectrolyzerRecipe::getEnergyPerTick)

                    ).apply(instance, (input, outA, outB, time, energy) ->
                            new ElectrolyzerRecipe(
                                    null,        // ID assigned later by RecipeManager
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
    //  NETWORK CODEC (unchanged)
    // ---------------------------------------------------------
    private static final StreamCodec<RegistryFriendlyByteBuf, ElectrolyzerRecipe> STREAM_CODEC =
            StreamCodec.of(
                    (buf, recipe) -> {

                        // Input
                        FluidStack in = recipe.getFluidInputs().get(0);
                        buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(in.getFluid()));
                        buf.writeInt(in.getAmount());

                        // Output A
                        FluidStack outA = recipe.getFluidOutputs().get(0);
                        buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(outA.getFluid()));
                        buf.writeInt(outA.getAmount());

                        // Output B
                        FluidStack outB = recipe.getFluidOutputs().get(1);
                        buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(outB.getFluid()));
                        buf.writeInt(outB.getAmount());

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
