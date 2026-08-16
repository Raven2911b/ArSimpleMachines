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

public class ChemicalReactorRecipeSerializer implements RecipeSerializer<ChemicalReactorRecipe> {

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

    private static final Codec<ChemicalReactorRecipeInput.FluidTagInput> FLUID_TAG_CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC.fieldOf("tag").forGetter(ChemicalReactorRecipeInput.FluidTagInput::tag),
                            Codec.INT.fieldOf("amount").forGetter(ChemicalReactorRecipeInput.FluidTagInput::amount)
                    ).apply(instance, ChemicalReactorRecipeInput.FluidTagInput::new)
            );

    private static final MapCodec<ChemicalReactorRecipe> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(

                            // Direct fluids OPTIONAL
                            FLUID_CODEC.optionalFieldOf("fluidA", FluidStack.EMPTY)
                                    .forGetter(ChemicalReactorRecipe::getFluidA),

                            FLUID_CODEC.optionalFieldOf("fluidB", FluidStack.EMPTY)
                                    .forGetter(ChemicalReactorRecipe::getFluidB),

                            // Output REQUIRED
                            FLUID_CODEC.fieldOf("output").forGetter(ChemicalReactorRecipe::getOutput),

                            // Tags OPTIONAL
                            Codec.list(FLUID_TAG_CODEC).optionalFieldOf("fluidA_tags", List.of())
                                    .forGetter(ChemicalReactorRecipe::getFluidATags),

                            Codec.list(FLUID_TAG_CODEC).optionalFieldOf("fluidB_tags", List.of())
                                    .forGetter(ChemicalReactorRecipe::getFluidBTags),

                            Codec.INT.fieldOf("processing_time").forGetter(ChemicalReactorRecipe::getProcessingTime),
                            Codec.INT.fieldOf("energy_per_tick").forGetter(ChemicalReactorRecipe::getEnergyPerTick)

                    ).apply(instance, (fluidA, fluidB, output, fluidATags, fluidBTags, time, energy) -> {

                        // VALIDATION: must have either direct or tag inputs
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

    private static final StreamCodec<RegistryFriendlyByteBuf, ChemicalReactorRecipe> STREAM_CODEC =
            StreamCodec.of(
                    (buf, recipe) -> {

                        buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(recipe.getFluidA().getFluid()));
                        buf.writeInt(recipe.getFluidA().getAmount());

                        buf.writeInt(recipe.getFluidATags().size());
                        for (var tag : recipe.getFluidATags()) {
                            buf.writeResourceLocation(tag.tag());
                            buf.writeInt(tag.amount());
                        }

                        buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(recipe.getFluidB().getFluid()));
                        buf.writeInt(recipe.getFluidB().getAmount());

                        buf.writeInt(recipe.getFluidBTags().size());
                        for (var tag : recipe.getFluidBTags()) {
                            buf.writeResourceLocation(tag.tag());
                            buf.writeInt(tag.amount());
                        }

                        buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(recipe.getOutput().getFluid()));
                        buf.writeInt(recipe.getOutput().getAmount());

                        buf.writeInt(recipe.getProcessingTime());
                        buf.writeInt(recipe.getEnergyPerTick());
                    },

                    buf -> {

                        var fluidAId = buf.readResourceLocation();
                        int fluidAAmount = buf.readInt();
                        FluidStack fluidA = new FluidStack(
                                BuiltInRegistries.FLUID.get(fluidAId),
                                fluidAAmount
                        );

                        int aTagCount = buf.readInt();
                        List<ChemicalReactorRecipeInput.FluidTagInput> fluidATags = new ArrayList<>();
                        for (int i = 0; i < aTagCount; i++) {
                            ResourceLocation tag = buf.readResourceLocation();
                            int amount = buf.readInt();
                            fluidATags.add(new ChemicalReactorRecipeInput.FluidTagInput(tag, amount));
                        }

                        var fluidBId = buf.readResourceLocation();
                        int fluidBAmount = buf.readInt();
                        FluidStack fluidB = new FluidStack(
                                BuiltInRegistries.FLUID.get(fluidBId),
                                fluidBAmount
                        );

                        int bTagCount = buf.readInt();
                        List<ChemicalReactorRecipeInput.FluidTagInput> fluidBTags = new ArrayList<>();
                        for (int i = 0; i < bTagCount; i++) {
                            ResourceLocation tag = buf.readResourceLocation();
                            int amount = buf.readInt();
                            fluidBTags.add(new ChemicalReactorRecipeInput.FluidTagInput(tag, amount));
                        }

                        var outId = buf.readResourceLocation();
                        int outAmount = buf.readInt();
                        FluidStack output = new FluidStack(
                                BuiltInRegistries.FLUID.get(outId),
                                outAmount
                        );

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
