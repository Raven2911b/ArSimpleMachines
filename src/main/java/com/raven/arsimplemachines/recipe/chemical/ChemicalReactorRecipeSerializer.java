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

import java.util.ArrayList;
import java.util.List;

/**
 * Unified Chemical Reactor Recipe Serializer
 *
 * Supports:
 *  - fluidA (optional)
 *  - fluidB (optional)
 *  - fluidA_tags (optional)
 *  - fluidB_tags (optional)
 *  - output (required)
 *  - processing_time
 *  - energy_per_tick
 *
 * Produces a ChemicalReactorRecipe that extends MachineRecipe.
 */
public class ChemicalReactorRecipeSerializer implements RecipeSerializer<ChemicalReactorRecipe> {

    // ---------------------------------------------------------------------
    // FLUID CODEC
    // ---------------------------------------------------------------------
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

    // ---------------------------------------------------------------------
    // FLUID TAG CODEC (Chemical-Reactor-only feature)
    // ---------------------------------------------------------------------
    private static final Codec<ChemicalReactorRecipe.FluidTagInput> FLUID_TAG_CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC.fieldOf("tag")
                                    .forGetter(ChemicalReactorRecipe.FluidTagInput::tag),
                            Codec.INT.fieldOf("amount")
                                    .forGetter(ChemicalReactorRecipe.FluidTagInput::amount)
                    ).apply(instance, ChemicalReactorRecipe.FluidTagInput::new)
            );

    // ---------------------------------------------------------------------
    // MAIN CODEC (Unified JSON → Unified Recipe)
    // ---------------------------------------------------------------------
    private static final MapCodec<ChemicalReactorRecipe> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(

                            // Direct fluids (optional)
                            FLUID_CODEC.optionalFieldOf("fluidA", FluidStack.EMPTY)
                                    .forGetter(ChemicalReactorRecipe::getFluidA),

                            FLUID_CODEC.optionalFieldOf("fluidB", FluidStack.EMPTY)
                                    .forGetter(ChemicalReactorRecipe::getFluidB),

                            // Output fluid (required)
                            FLUID_CODEC.fieldOf("output")
                                    .forGetter(ChemicalReactorRecipe::getOutput),

                            // Tag-based fluid inputs (optional)
                            Codec.list(FLUID_TAG_CODEC).optionalFieldOf("fluidA_tags", List.of())
                                    .forGetter(ChemicalReactorRecipe::getFluidATags),

                            Codec.list(FLUID_TAG_CODEC).optionalFieldOf("fluidB_tags", List.of())
                                    .forGetter(ChemicalReactorRecipe::getFluidBTags),

                            // Timing + energy
                            Codec.INT.fieldOf("processing_time")
                                    .forGetter(ChemicalReactorRecipe::getProcessingTime),

                            Codec.INT.fieldOf("energy_per_tick")
                                    .forGetter(ChemicalReactorRecipe::getEnergyPerTick)

                    ).apply(instance, (fluidA, fluidB, output, fluidATags, fluidBTags, time, energy) -> {

                        // Validation: must have at least one input for A and B
                        if (fluidA.isEmpty() && fluidATags.isEmpty()) {
                            throw new IllegalArgumentException("Chemical Reactor recipe requires fluidA or fluidA_tags");
                        }
                        if (fluidB.isEmpty() && fluidBTags.isEmpty()) {
                            throw new IllegalArgumentException("Chemical Reactor recipe requires fluidB or fluidB_tags");
                        }

                        return new ChemicalReactorRecipe(
                                null,
                                fluidA,
                                fluidB,
                                output,
                                fluidATags,
                                fluidBTags,
                                time,
                                energy
                        );
                    })
            );

    @Override
    public MapCodec<ChemicalReactorRecipe> codec() {
        return CODEC;
    }

    // ---------------------------------------------------------------------
    // STREAM CODEC (Networking)
    // ---------------------------------------------------------------------
    private static final StreamCodec<RegistryFriendlyByteBuf, ChemicalReactorRecipe> STREAM_CODEC =
            StreamCodec.of(
                    (buf, recipe) -> {

                        // fluidA
                        buf.writeResourceLocation(
                                BuiltInRegistries.FLUID.getKey(recipe.getFluidA().getFluid())
                        );
                        buf.writeInt(recipe.getFluidA().getAmount());

                        // fluidA tags
                        buf.writeInt(recipe.getFluidATags().size());
                        for (var tag : recipe.getFluidATags()) {
                            buf.writeResourceLocation(tag.tag());
                            buf.writeInt(tag.amount());
                        }

                        // fluidB
                        buf.writeResourceLocation(
                                BuiltInRegistries.FLUID.getKey(recipe.getFluidB().getFluid())
                        );
                        buf.writeInt(recipe.getFluidB().getAmount());

                        // fluidB tags
                        buf.writeInt(recipe.getFluidBTags().size());
                        for (var tag : recipe.getFluidBTags()) {
                            buf.writeResourceLocation(tag.tag());
                            buf.writeInt(tag.amount());
                        }

                        // output
                        buf.writeResourceLocation(
                                BuiltInRegistries.FLUID.getKey(recipe.getOutput().getFluid())
                        );
                        buf.writeInt(recipe.getOutput().getAmount());

                        // timing + energy
                        buf.writeInt(recipe.getProcessingTime());
                        buf.writeInt(recipe.getEnergyPerTick());
                    },

                    buf -> {

                        // fluidA
                        ResourceLocation fluidAId = buf.readResourceLocation();
                        int fluidAAmount = buf.readInt();
                        FluidStack fluidA = new FluidStack(
                                BuiltInRegistries.FLUID.get(fluidAId),
                                fluidAAmount
                        );

                        // fluidA tags
                        int aTagCount = buf.readInt();
                        List<ChemicalReactorRecipe.FluidTagInput> fluidATags = new ArrayList<>();
                        for (int i = 0; i < aTagCount; i++) {
                            ResourceLocation tag = buf.readResourceLocation();
                            int amount = buf.readInt();
                            fluidATags.add(new ChemicalReactorRecipe.FluidTagInput(tag, amount));
                        }

                        // fluidB
                        ResourceLocation fluidBId = buf.readResourceLocation();
                        int fluidBAmount = buf.readInt();
                        FluidStack fluidB = new FluidStack(
                                BuiltInRegistries.FLUID.get(fluidBId),
                                fluidBAmount
                        );

                        // fluidB tags
                        int bTagCount = buf.readInt();
                        List<ChemicalReactorRecipe.FluidTagInput> fluidBTags = new ArrayList<>();
                        for (int i = 0; i < bTagCount; i++) {
                            ResourceLocation tag = buf.readResourceLocation();
                            int amount = buf.readInt();
                            fluidBTags.add(new ChemicalReactorRecipe.FluidTagInput(tag, amount));
                        }

                        // output
                        ResourceLocation outId = buf.readResourceLocation();
                        int outAmount = buf.readInt();
                        FluidStack output = new FluidStack(
                                BuiltInRegistries.FLUID.get(outId),
                                outAmount
                        );

                        // timing + energy
                        int time = buf.readInt();
                        int energy = buf.readInt();

                        return new ChemicalReactorRecipe(
                                null,
                                fluidA,
                                fluidB,
                                output,
                                fluidATags,
                                fluidBTags,
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
