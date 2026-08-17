package com.raven.arsimplemachines.recipe.lathe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.raven.arsimplemachines.recipe.TagInput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.List;

public class LatheRecipeSerializer implements RecipeSerializer<LatheRecipe> {

    private static final Codec<ItemStack> ITEM_STACK_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("item").forGetter(s -> BuiltInRegistries.ITEM.getKey(s.getItem())),
                    Codec.INT.fieldOf("count").forGetter(ItemStack::getCount)
            ).apply(instance, (id, count) -> {
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item == null) {
                    return ItemStack.EMPTY;
                }
                return new ItemStack(item, count);
            })
    );

    private static final Codec<TagInput> TAG_INPUT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("tag").forGetter(TagInput::tag),
                    Codec.INT.fieldOf("count").forGetter(TagInput::count)
            ).apply(instance, TagInput::new)
    );

    // Lathe does NOT use fluids, but unified system requires the fields to exist.
    private static final Codec<FluidStack> FLUID_STACK_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("fluid").forGetter(s -> BuiltInRegistries.FLUID.getKey(s.getFluid())),
                    Codec.INT.fieldOf("amount").forGetter(FluidStack::getAmount)
            ).apply(instance, (id, amount) ->
                    new FluidStack(BuiltInRegistries.FLUID.get(id), amount)
            )
    );

    private static final MapCodec<LatheRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ITEM_STACK_CODEC.listOf().optionalFieldOf("item_inputs", List.of()).forGetter(LatheRecipe::getItemInputs),
                    TAG_INPUT_CODEC.listOf().optionalFieldOf("item_tags", List.of()).forGetter(LatheRecipe::getItemTags),

                    // Lathe does NOT use fluids, but unified architecture requires these fields.
                    FLUID_STACK_CODEC.listOf().optionalFieldOf("fluid_inputs", List.of()).forGetter(LatheRecipe::getFluidInputs),
                    ITEM_STACK_CODEC.listOf().optionalFieldOf("item_outputs", List.of()).forGetter(LatheRecipe::getItemOutputs),
                    FLUID_STACK_CODEC.listOf().optionalFieldOf("fluid_outputs", List.of()).forGetter(LatheRecipe::getFluidOutputs),

                    Codec.INT.fieldOf("processing_time").forGetter(LatheRecipe::getProcessingTime),
                    Codec.INT.fieldOf("energy_per_tick").forGetter(LatheRecipe::getEnergyPerTick)
            ).apply(instance, (itemInputs, tagInputs, fluidInputs, itemOutputs, fluidOutputs, time, energy) ->
                    new LatheRecipe(
                            null,              // ID injected by NeoForge
                            itemInputs,
                            tagInputs,
                            fluidInputs,
                            itemOutputs,
                            fluidOutputs,
                            time,
                            energy
                    )
            )
    );

    @Override
    public MapCodec<LatheRecipe> codec() {
        return CODEC;
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, LatheRecipe> STREAM_CODEC =
            StreamCodec.of(

                    // WRITE
                    (buf, recipe) -> {

                        // ITEM INPUTS
                        buf.writeVarInt(recipe.getItemInputs().size());
                        for (ItemStack s : recipe.getItemInputs()) {
                            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(s.getItem()));
                            buf.writeVarInt(s.getCount());
                        }

                        // TAG INPUTS
                        buf.writeVarInt(recipe.getItemTags().size());
                        for (TagInput ti : recipe.getItemTags()) {
                            buf.writeResourceLocation(ti.tag());
                            buf.writeVarInt(ti.count());
                        }

                        // FLUID INPUTS (always empty for Lathe)
                        buf.writeVarInt(recipe.getFluidInputs().size());
                        for (FluidStack fs : recipe.getFluidInputs()) {
                            buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(fs.getFluid()));
                            buf.writeVarInt(fs.getAmount());
                        }

                        // ITEM OUTPUTS
                        buf.writeVarInt(recipe.getItemOutputs().size());
                        for (ItemStack s : recipe.getItemOutputs()) {
                            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(s.getItem()));
                            buf.writeVarInt(s.getCount());
                        }

                        // FLUID OUTPUTS (always empty for Lathe)
                        buf.writeVarInt(recipe.getFluidOutputs().size());
                        for (FluidStack fs : recipe.getFluidOutputs()) {
                            buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(fs.getFluid()));
                            buf.writeVarInt(fs.getAmount());
                        }

                        // PROCESSING / ENERGY
                        buf.writeVarInt(recipe.getProcessingTime());
                        buf.writeVarInt(recipe.getEnergyPerTick());
                    },

                    // READ
                    buf -> {

                        // ITEM INPUTS
                        int inItems = buf.readVarInt();
                        List<ItemStack> itemInputs = new ArrayList<>();
                        for (int i = 0; i < inItems; i++) {
                            Item item = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
                            int count = buf.readVarInt();
                            if (item != null) {
                                itemInputs.add(new ItemStack(item, count));
                            }
                        }

                        // TAG INPUTS
                        int tagCount = buf.readVarInt();
                        List<TagInput> itemTags = new ArrayList<>();
                        for (int i = 0; i < tagCount; i++) {
                            ResourceLocation tag = buf.readResourceLocation();
                            int count = buf.readVarInt();
                            itemTags.add(new TagInput(tag, count));
                        }

                        // FLUID INPUTS (always empty)
                        int inFluids = buf.readVarInt();
                        List<FluidStack> fluidInputs = new ArrayList<>();
                        for (int i = 0; i < inFluids; i++) {
                            var fluid = BuiltInRegistries.FLUID.get(buf.readResourceLocation());
                            int amt = buf.readVarInt();
                            fluidInputs.add(new FluidStack(fluid, amt));
                        }

                        // ITEM OUTPUTS
                        int outItems = buf.readVarInt();
                        List<ItemStack> itemOutputs = new ArrayList<>();
                        for (int i = 0; i < outItems; i++) {
                            Item item = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
                            int count = buf.readVarInt();
                            if (item != null) {
                                itemOutputs.add(new ItemStack(item, count));
                            }
                        }

                        // FLUID OUTPUTS (always empty)
                        int outFluids = buf.readVarInt();
                        List<FluidStack> fluidOutputs = new ArrayList<>();
                        for (int i = 0; i < outFluids; i++) {
                            var fluid = BuiltInRegistries.FLUID.get(buf.readResourceLocation());
                            int amt = buf.readVarInt();
                            fluidOutputs.add(new FluidStack(fluid, amt));
                        }

                        // PROCESSING / ENERGY
                        int time = buf.readVarInt();
                        int energy = buf.readVarInt();

                        return new LatheRecipe(
                                null,              // ID injected by NeoForge
                                itemInputs,
                                itemTags,
                                fluidInputs,
                                itemOutputs,
                                fluidOutputs,
                                time,
                                energy
                        );
                    }
            );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, LatheRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
