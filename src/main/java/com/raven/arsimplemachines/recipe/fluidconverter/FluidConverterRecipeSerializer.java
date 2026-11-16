package com.raven.arsimplemachines.recipe.fluidconverter;

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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class FluidConverterRecipeSerializer implements RecipeSerializer<FluidConverterRecipe> {

    private static final Codec<ItemStack> ITEM_STACK_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("item").forGetter(s -> BuiltInRegistries.ITEM.getKey(s.getItem())),
                    Codec.INT.fieldOf("count").forGetter(ItemStack::getCount)
            ).apply(instance, (id, count) -> {
                Item item = BuiltInRegistries.ITEM.get(id);
                return item == null ? ItemStack.EMPTY : new ItemStack(item, count);
            })
    );

    private static final Codec<TagInput> TAG_INPUT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("tag").forGetter(TagInput::tag),
                    Codec.INT.fieldOf("count").forGetter(TagInput::count)
            ).apply(instance, TagInput::new)
    );

    private static final Codec<FluidStack> FLUID_STACK_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("fluid").forGetter(s -> BuiltInRegistries.FLUID.getKey(s.getFluid())),
                    Codec.INT.fieldOf("amount").forGetter(FluidStack::getAmount)
            ).apply(instance, (id, amount) -> new FluidStack(BuiltInRegistries.FLUID.get(id), amount))
    );

    private static final Codec<FluidConverterRecipe.FluidTagInput> FLUID_TAG_CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC.fieldOf("tag").forGetter(FluidConverterRecipe.FluidTagInput::tag),
                            Codec.INT.fieldOf("amount").forGetter(FluidConverterRecipe.FluidTagInput::amount)
                    ).apply(instance, FluidConverterRecipe.FluidTagInput::new)
            );

    private static final MapCodec<FluidConverterRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ITEM_STACK_CODEC.listOf().optionalFieldOf("item_inputs", List.of()).forGetter(FluidConverterRecipe::getItemInputs),
                    TAG_INPUT_CODEC.listOf().optionalFieldOf("item_tags", List.of()).forGetter(FluidConverterRecipe::getItemTags),

                    FLUID_STACK_CODEC.listOf().optionalFieldOf("fluid_inputs", List.of()).forGetter(FluidConverterRecipe::getFluidInputs),
                    Codec.list(FLUID_TAG_CODEC).optionalFieldOf("fluid_input_tags", List.of()).forGetter(FluidConverterRecipe::getFluidInputTags),

                    ITEM_STACK_CODEC.listOf().optionalFieldOf("item_outputs", List.of()).forGetter(FluidConverterRecipe::getItemOutputs),
                    FLUID_STACK_CODEC.listOf().optionalFieldOf("fluid_outputs", List.of()).forGetter(FluidConverterRecipe::getFluidOutputs),

                    Codec.INT.fieldOf("processing_time").forGetter(FluidConverterRecipe::getProcessingTime),
                    Codec.INT.fieldOf("energy_per_tick").forGetter(FluidConverterRecipe::getEnergyPerTick)
            ).apply(instance, (itemInputs, tagInputs, fluidInputs, fluidInputTags, itemOutputs, fluidOutputs, time, energy) ->
                    new FluidConverterRecipe(null, itemInputs, tagInputs, fluidInputs, fluidInputTags, itemOutputs, fluidOutputs, time, energy))
    );

    @Override
    public MapCodec<FluidConverterRecipe> codec() {
        return CODEC;
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, FluidConverterRecipe> STREAM_CODEC =
            StreamCodec.of(
                    (buf, recipe) -> {
                        buf.writeVarInt(recipe.getItemInputs().size());
                        for (ItemStack s : recipe.getItemInputs()) {
                            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(s.getItem()));
                            buf.writeVarInt(s.getCount());
                        }

                        buf.writeVarInt(recipe.getItemTags().size());
                        for (TagInput ti : recipe.getItemTags()) {
                            buf.writeResourceLocation(ti.tag());
                            buf.writeVarInt(ti.count());
                        }

                        buf.writeVarInt(recipe.getFluidInputs().size());
                        for (FluidStack fs : recipe.getFluidInputs()) {
                            buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(fs.getFluid()));
                            buf.writeVarInt(fs.getAmount());
                        }

                        buf.writeVarInt(recipe.getFluidInputTags().size());
                        for (FluidConverterRecipe.FluidTagInput tag : recipe.getFluidInputTags()) {
                            buf.writeResourceLocation(tag.tag());
                            buf.writeVarInt(tag.amount());
                        }

                        buf.writeVarInt(recipe.getItemOutputs().size());
                        for (ItemStack s : recipe.getItemOutputs()) {
                            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(s.getItem()));
                            buf.writeVarInt(s.getCount());
                        }

                        buf.writeVarInt(recipe.getFluidOutputs().size());
                        for (FluidStack fs : recipe.getFluidOutputs()) {
                            buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(fs.getFluid()));
                            buf.writeVarInt(fs.getAmount());
                        }

                        buf.writeVarInt(recipe.getProcessingTime());
                        buf.writeVarInt(recipe.getEnergyPerTick());
                    },

                    buf -> {
                        int inItems = buf.readVarInt();
                        List<ItemStack> itemInputs = new ArrayList<>();
                        for (int i = 0; i < inItems; i++) {
                            Item item = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
                            int count = buf.readVarInt();
                            if (item != null) itemInputs.add(new ItemStack(item, count));
                        }

                        int tagCount = buf.readVarInt();
                        List<TagInput> itemTags = new ArrayList<>();
                        for (int i = 0; i < tagCount; i++) {
                            ResourceLocation tag = buf.readResourceLocation();
                            int count = buf.readVarInt();
                            itemTags.add(new TagInput(tag, count));
                        }

                        int inFluidCount = buf.readVarInt();
                        List<FluidStack> fluidInputs = new ArrayList<>();
                        for (int i = 0; i < inFluidCount; i++) {
                            var fluid = BuiltInRegistries.FLUID.get(buf.readResourceLocation());
                            int amt = buf.readVarInt();
                            if (fluid != null) fluidInputs.add(new FluidStack(fluid, amt));
                        }

                        int inTagCount = buf.readVarInt();
                        List<FluidConverterRecipe.FluidTagInput> fluidInputTags = new ArrayList<>();
                        for (int i = 0; i < inTagCount; i++) {
                            ResourceLocation tag = buf.readResourceLocation();
                            int amount = buf.readVarInt();
                            fluidInputTags.add(new FluidConverterRecipe.FluidTagInput(tag, amount));
                        }

                        int outItems = buf.readVarInt();
                        List<ItemStack> itemOutputs = new ArrayList<>();
                        for (int i = 0; i < outItems; i++) {
                            Item item = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
                            int count = buf.readVarInt();
                            if (item != null) itemOutputs.add(new ItemStack(item, count));
                        }

                        int outFluidCount = buf.readVarInt();
                        List<FluidStack> fluidOutputs = new ArrayList<>();
                        for (int i = 0; i < outFluidCount; i++) {
                            var fluid = BuiltInRegistries.FLUID.get(buf.readResourceLocation());
                            int amt = buf.readVarInt();
                            if (fluid != null) fluidOutputs.add(new FluidStack(fluid, amt));
                        }

                        int time = buf.readVarInt();
                        int energy = buf.readVarInt();

                        return new FluidConverterRecipe(null, itemInputs, itemTags, fluidInputs, fluidInputTags, itemOutputs, fluidOutputs, time, energy);
                    }
            );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FluidConverterRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}