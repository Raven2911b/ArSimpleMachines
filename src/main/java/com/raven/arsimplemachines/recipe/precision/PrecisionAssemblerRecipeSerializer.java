package com.raven.arsimplemachines.recipe.precision;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;

public class PrecisionAssemblerRecipeSerializer implements RecipeSerializer<PrecisionAssemblerRecipe> {

    // ------------------------------------------------------------
    // JSON CODEC
    // ------------------------------------------------------------
    private static final MapCodec<PrecisionAssemblerRecipe> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            // List<ItemStack> inputs
                            ItemStack.CODEC.listOf().fieldOf("inputs")
                                    .forGetter(PrecisionAssemblerRecipe::getItemInputs),

                            // List<ItemStack> outputs
                            ItemStack.CODEC.listOf().fieldOf("outputs")
                                    .forGetter(PrecisionAssemblerRecipe::getItemOutputs),

                            // Processing time
                            Codec.INT.fieldOf("processing_time")
                                    .forGetter(PrecisionAssemblerRecipe::getProcessingTime),

                            // Energy per tick
                            Codec.INT.fieldOf("energy_per_tick")
                                    .forGetter(PrecisionAssemblerRecipe::getEnergyPerTick)

                    ).apply(instance, (inputs, outputs, time, energy) ->
                            new PrecisionAssemblerRecipe(
                                    null,          // ID assigned later by RecipeManager
                                    inputs,
                                    outputs,
                                    time,
                                    energy
                            )
                    )
            );

    @Override
    public MapCodec<PrecisionAssemblerRecipe> codec() {
        return CODEC;
    }

    // ------------------------------------------------------------
// NETWORK STREAM CODEC
// ------------------------------------------------------------
    private static final StreamCodec<RegistryFriendlyByteBuf, PrecisionAssemblerRecipe> STREAM_CODEC =
            StreamCodec.of(
                    // Write
                    (buf, recipe) -> {
                        // Inputs
                        buf.writeInt(recipe.getItemInputs().size());
                        for (ItemStack stack : recipe.getItemInputs()) {
                            ItemStack.STREAM_CODEC.encode(buf, stack);
                        }

                        // Outputs
                        buf.writeInt(recipe.getItemOutputs().size());
                        for (ItemStack stack : recipe.getItemOutputs()) {
                            ItemStack.STREAM_CODEC.encode(buf, stack);
                        }

                        buf.writeInt(recipe.getProcessingTime());
                        buf.writeInt(recipe.getEnergyPerTick());
                    },

                    // Read
                    buf -> {
                        int inCount = buf.readInt();
                        List<ItemStack> inputs = new java.util.ArrayList<>();
                        for (int i = 0; i < inCount; i++) {
                            inputs.add(ItemStack.STREAM_CODEC.decode(buf));
                        }

                        int outCount = buf.readInt();
                        List<ItemStack> outputs = new java.util.ArrayList<>();
                        for (int i = 0; i < outCount; i++) {
                            outputs.add(ItemStack.STREAM_CODEC.decode(buf));
                        }

                        int time = buf.readInt();
                        int energy = buf.readInt();

                        return new PrecisionAssemblerRecipe(
                                null,
                                inputs,
                                outputs,
                                time,
                                energy
                        );
                    }
            );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, PrecisionAssemblerRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
