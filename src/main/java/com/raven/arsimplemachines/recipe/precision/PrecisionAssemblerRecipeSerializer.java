package com.raven.arsimplemachines.recipe.precision;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.raven.arsimplemachines.recipe.TagInput;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;

/**
 * Serializer for Precision Assembler:
 * JSON:
 *  - item_inputs: [ { item, count } ]
 *  - item_tags:   [ { tag,  count } ]
 *  - item_outputs:[ { item, count } ]
 *  - processing_time
 *  - energy_per_tick
 *
 * Network:
 *  - item_inputs
 *  - item_tags
 *  - item_outputs
 *  - processing_time
 *  - energy_per_tick
 */
public class PrecisionAssemblerRecipeSerializer implements RecipeSerializer<PrecisionAssemblerRecipe> {

    private static final MapCodec<PrecisionAssemblerRecipe> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            // item_inputs
                            ItemStack.CODEC.listOf()
                                    .optionalFieldOf("item_inputs", List.of())
                                    .forGetter(PrecisionAssemblerRecipe::getItemInputs),

                            // item_tags
                            TagInput.CODEC.listOf()
                                    .optionalFieldOf("item_tags", List.of())
                                    .forGetter(PrecisionAssemblerRecipe::getItemTags),

                            // item_outputs
                            ItemStack.CODEC.listOf()
                                    .optionalFieldOf("item_outputs", List.of())
                                    .forGetter(PrecisionAssemblerRecipe::getItemOutputs),

                            // processing_time
                            Codec.INT.fieldOf("processing_time")
                                    .forGetter(PrecisionAssemblerRecipe::getProcessingTime),

                            // energy_per_tick
                            Codec.INT.fieldOf("energy_per_tick")
                                    .forGetter(PrecisionAssemblerRecipe::getEnergyPerTick)

                    ).apply(instance, (inputs, tags, outputs, time, energy) ->
                            new PrecisionAssemblerRecipe(
                                    null,
                                    inputs,
                                    tags,
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

    private static final StreamCodec<RegistryFriendlyByteBuf, PrecisionAssemblerRecipe> STREAM_CODEC =
            StreamCodec.of(
                    // write
                    (buf, recipe) -> {
                        // item_inputs
                        buf.writeInt(recipe.getItemInputs().size());
                        for (ItemStack stack : recipe.getItemInputs()) {
                            ItemStack.STREAM_CODEC.encode(buf, stack);
                        }

                        // item_tags
                        buf.writeInt(recipe.getItemTags().size());
                        for (TagInput tag : recipe.getItemTags()) {
                            buf.writeResourceLocation(tag.tag());
                            buf.writeInt(tag.count());

                        }

                        // item_outputs
                        buf.writeInt(recipe.getItemOutputs().size());
                        for (ItemStack stack : recipe.getItemOutputs()) {
                            ItemStack.STREAM_CODEC.encode(buf, stack);
                        }

                        buf.writeInt(recipe.getProcessingTime());
                        buf.writeInt(recipe.getEnergyPerTick());
                    },

                    // read
                    buf -> {
                        // item_inputs
                        int inCount = buf.readInt();
                        List<ItemStack> inputs = new java.util.ArrayList<>();
                        for (int i = 0; i < inCount; i++) {
                            inputs.add(ItemStack.STREAM_CODEC.decode(buf));
                        }

                        // item_tags
                        int tagCount = buf.readInt();
                        List<TagInput> tags = new java.util.ArrayList<>();
                        for (int i = 0; i < tagCount; i++) {
                            ResourceLocation rl = buf.readResourceLocation();
                            int count = buf.readInt();
                            tags.add(new TagInput(rl, count));

                        }

                        // item_outputs
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
                                tags,
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
