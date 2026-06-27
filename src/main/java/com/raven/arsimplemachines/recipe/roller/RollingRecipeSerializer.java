package com.raven.arsimplemachines.recipe.roller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RollingRecipeSerializer implements RecipeSerializer<RollingRecipe> {

    // ---------------------------------------------------------
    // JSON Codec
    // ---------------------------------------------------------
    private static final MapCodec<RollingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    // Input item ID
                    ResourceLocation.CODEC.fieldOf("input").forGetter(r ->
                            BuiltInRegistries.ITEM.getKey(r.getInputItem())
                    ),

                    // Output item ID
                    ResourceLocation.CODEC.fieldOf("output").forGetter(r ->
                            BuiltInRegistries.ITEM.getKey(r.getOutput().getItem())
                    ),

                    // Output count
                    Codec.INT.fieldOf("output_count").forGetter(r ->
                            r.getOutput().getCount()
                    ),

                    // Processing time
                    Codec.INT.fieldOf("processing_time").forGetter(RollingRecipe::getProcessingTime),

                    // Energy per tick
                    Codec.INT.fieldOf("energy_per_tick").forGetter(RollingRecipe::getEnergyPerTick),

                    // Fluid required
                    Codec.INT.fieldOf("fluid_required").forGetter(RollingRecipe::getFluidRequired)
            ).apply(instance, (inputId, outputId, count, time, energy, fluid) -> {

                Item input = BuiltInRegistries.ITEM.get(inputId);
                Item output = BuiltInRegistries.ITEM.get(outputId);

                return new RollingRecipe(
                        null, // ID assigned later by RecipeManager
                        input,
                        new ItemStack(output, count),
                        time,
                        energy,
                        fluid
                );
            })
    );

    @Override
    public MapCodec<RollingRecipe> codec() {
        return CODEC;
    }

    // ---------------------------------------------------------
    // Network Codec
    // ---------------------------------------------------------
    private static final StreamCodec<RegistryFriendlyByteBuf, RollingRecipe> STREAM_CODEC =
            StreamCodec.of(
                    (buf, recipe) -> {
                        buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(recipe.getInputItem()));
                        buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(recipe.getOutput().getItem()));
                        buf.writeInt(recipe.getOutput().getCount());
                        buf.writeInt(recipe.getProcessingTime());
                        buf.writeInt(recipe.getEnergyPerTick());
                        buf.writeInt(recipe.getFluidRequired());
                    },
                    buf -> {
                        Item input = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
                        Item output = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
                        int count = buf.readInt();
                        int time = buf.readInt();
                        int energy = buf.readInt();
                        int fluid = buf.readInt();

                        return new RollingRecipe(
                                null,
                                input,
                                new ItemStack(output, count),
                                time,
                                energy,
                                fluid
                        );
                    }
            );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RollingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
