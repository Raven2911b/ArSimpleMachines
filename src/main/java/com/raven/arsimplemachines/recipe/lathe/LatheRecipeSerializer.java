package com.raven.arsimplemachines.recipe.lathe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class LatheRecipeSerializer implements RecipeSerializer<LatheRecipe> {

    private static final MapCodec<LatheRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("input").forGetter(r ->
                            BuiltInRegistries.ITEM.getKey(r.getInputItem())
                    ),
                    ResourceLocation.CODEC.fieldOf("output").forGetter(r ->
                            BuiltInRegistries.ITEM.getKey(r.getOutputItem())
                    ),
                    Codec.INT.fieldOf("output_count").forGetter(LatheRecipe::getOutputCount),
                    Codec.INT.fieldOf("processing_time").forGetter(r -> r.processingTime)
            ).apply(instance, (inputId, outputId, count, time) -> {

                Item input = BuiltInRegistries.ITEM.get(inputId);
                Item output = BuiltInRegistries.ITEM.get(outputId);

                return new LatheRecipe(
                        null,      // ID is assigned later by RecipeManager
                        input,
                        output,
                        count,
                        time
                );
            })
    );

    @Override
    public MapCodec<LatheRecipe> codec() {
        return CODEC;
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, LatheRecipe> STREAM_CODEC =
            StreamCodec.of(
                    (buf, recipe) -> {
                        buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(recipe.getInputItem()));
                        buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(recipe.getOutputItem()));
                        buf.writeInt(recipe.getOutputCount());
                        buf.writeInt(recipe.processingTime);
                    },
                    buf -> {
                        Item input = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
                        Item output = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
                        int count = buf.readInt();
                        int time = buf.readInt();

                        return new LatheRecipe(
                                null,
                                input,
                                output,
                                count,
                                time
                        );
                    }
            );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, LatheRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
