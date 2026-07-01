package com.raven.arsimplemachines.recipe.chemical;

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
 * Serializer for Chemical Reactor recipes.
 *
 * JSON FORMAT:
 *
 * {
 *   "fluidA": { "id": "adv_rocketry:hydrogen", "amount": 1000 },
 *   "fluidB": { "id": "adv_rocketry:oxygen",   "amount": 1000 },
 *   "output": { "id": "adv_rocketry:rocket_fuel", "amount": 1000 },
 *
 *   "processing_time": 200,
 *   "energy_per_tick": 50
 * }
 */
public class ChemicalReactorRecipeSerializer implements RecipeSerializer<ChemicalReactorRecipe> {

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

    private static final MapCodec<ChemicalReactorRecipe> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(

                            FLUID_CODEC.fieldOf("fluidA").forGetter(r -> r.getFluidA()),
                            FLUID_CODEC.fieldOf("fluidB").forGetter(r -> r.getFluidB()),
                            FLUID_CODEC.fieldOf("output").forGetter(r -> r.getOutput()),

                            Codec.INT.fieldOf("processing_time").forGetter(ChemicalReactorRecipe::getProcessingTime),
                            Codec.INT.fieldOf("energy_per_tick").forGetter(ChemicalReactorRecipe::getEnergyPerTick)

                    ).apply(instance, (fluidA, fluidB, output, time, energy) ->
                            new ChemicalReactorRecipe(
                                    null, // ID assigned later by RecipeManager
                                    fluidA,
                                    fluidB,
                                    output,
                                    time,
                                    energy
                            )
                    )
            );

    @Override
    public MapCodec<ChemicalReactorRecipe> codec() {
        return CODEC;
    }

    // ---------------------------------------------------------
    //  NETWORK CODEC
    // ---------------------------------------------------------

    private static final StreamCodec<RegistryFriendlyByteBuf, ChemicalReactorRecipe> STREAM_CODEC =
            StreamCodec.of(
                    (buf, recipe) -> {

                        // Fluid A
                        buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(recipe.getFluidA().getFluid()));
                        buf.writeInt(recipe.getFluidA().getAmount());

                        // Fluid B
                        buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(recipe.getFluidB().getFluid()));
                        buf.writeInt(recipe.getFluidB().getAmount());

                        // Output
                        buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(recipe.getOutput().getFluid()));
                        buf.writeInt(recipe.getOutput().getAmount());

                        // Time + energy
                        buf.writeInt(recipe.getProcessingTime());
                        buf.writeInt(recipe.getEnergyPerTick());
                    },

                    buf -> {

                        // Fluid A
                        var fluidAId = buf.readResourceLocation();
                        int fluidAAmount = buf.readInt();
                        FluidStack fluidA = new FluidStack(
                                BuiltInRegistries.FLUID.get(fluidAId),
                                fluidAAmount
                        );

                        // Fluid B
                        var fluidBId = buf.readResourceLocation();
                        int fluidBAmount = buf.readInt();
                        FluidStack fluidB = new FluidStack(
                                BuiltInRegistries.FLUID.get(fluidBId),
                                fluidBAmount
                        );

                        // Output
                        var outId = buf.readResourceLocation();
                        int outAmount = buf.readInt();
                        FluidStack output = new FluidStack(
                                BuiltInRegistries.FLUID.get(outId),
                                outAmount
                        );

                        // Time + energy
                        int time = buf.readInt();
                        int energy = buf.readInt();

                        return new ChemicalReactorRecipe(
                                null,
                                fluidA,
                                fluidB,
                                output,
                                time,
                                energy
                        );
                    }
            );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ChemicalReactorRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
