package com.raven.arsimplemachines.recipe.cutter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CuttingMachineRecipeSerializer implements RecipeSerializer<CuttingMachineRecipe> {

    private static final MapCodec<CuttingMachineRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("input").forGetter(r ->
                            BuiltInRegistries.ITEM.getKey(r.getInputItem())
                    ),
                    ResourceLocation.CODEC.fieldOf("output").forGetter(r ->
                            BuiltInRegistries.ITEM.getKey(r.getOutputItem())
                    ),
                    Codec.INT.fieldOf("output_count").forGetter(CuttingMachineRecipe::getOutputCount),
                    Codec.INT.fieldOf("processing_time").forGetter(r -> r.processingTime)
            ).apply(instance, (inputId, outputId, count, time) -> {

                Item input = BuiltInRegistries.ITEM.get(inputId);
                Item output = BuiltInRegistries.ITEM.get(outputId);

                return new CuttingMachineRecipe(
                        null,      // ID assigned later by RecipeManager
                        input,
                        output,
                        count,
                        time
                );
            })
    );

    @Override
    public MapCodec<CuttingMachineRecipe> codec() {
        return CODEC;
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, CuttingMachineRecipe> STREAM_CODEC =
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

                        return new CuttingMachineRecipe(
                                null,
                                input,
                                output,
                                count,
                                time
                        );
                    }
            );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CuttingMachineRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
