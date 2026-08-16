package com.raven.arsimplemachines.recipe.crystallizer;

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

public class CrystallizerRecipeSerializer implements RecipeSerializer<CrystallizerRecipe> {

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

    private static final Codec<FluidStack> FLUID_STACK_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("fluid").forGetter(s -> BuiltInRegistries.FLUID.getKey(s.getFluid())),
                    Codec.INT.fieldOf("amount").forGetter(FluidStack::getAmount)
            ).apply(instance, (id, amount) ->
                    new FluidStack(BuiltInRegistries.FLUID.get(id), amount)
            )
    );

    private static final Codec<TagInput> TAG_INPUT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("tag").forGetter(TagInput::tag),
                    Codec.INT.fieldOf("count").forGetter(TagInput::count)
            ).apply(instance, TagInput::new)
    );

    private static final MapCodec<CrystallizerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ITEM_STACK_CODEC.listOf().optionalFieldOf("item_inputs", List.of()).forGetter(CrystallizerRecipe::getItemInputs),
                    TAG_INPUT_CODEC.listOf().optionalFieldOf("item_tags", List.of()).forGetter(CrystallizerRecipe::getItemTags),
                    FLUID_STACK_CODEC.listOf().optionalFieldOf("fluid_inputs", List.of()).forGetter(CrystallizerRecipe::getFluidInputs),
                    ITEM_STACK_CODEC.listOf().optionalFieldOf("item_outputs", List.of()).forGetter(CrystallizerRecipe::getItemOutputs),
                    FLUID_STACK_CODEC.listOf().optionalFieldOf("fluid_outputs", List.of()).forGetter(CrystallizerRecipe::getFluidOutputs),
                    Codec.INT.fieldOf("processing_time").forGetter(CrystallizerRecipe::getProcessingTime),
                    Codec.INT.fieldOf("energy_per_tick").forGetter(CrystallizerRecipe::getEnergyPerTick)
            ).apply(instance, (itemInputs, tagInputs, fluidInputs, itemOutputs, fluidOutputs, time, energy) ->
                    new CrystallizerRecipe(
                            null,
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
    public MapCodec<CrystallizerRecipe> codec() {
        return CODEC;
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, CrystallizerRecipe> STREAM_CODEC =
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
                            if (item != null) {
                                itemInputs.add(new ItemStack(item, count));
                            }
                        }

                        int tagCount = buf.readVarInt();
                        List<TagInput> itemTags = new ArrayList<>();
                        for (int i = 0; i < tagCount; i++) {
                            ResourceLocation tag = buf.readResourceLocation();
                            int count = buf.readVarInt();
                            itemTags.add(new TagInput(tag, count));
                        }

                        int inFluids = buf.readVarInt();
                        List<FluidStack> fluidInputs = new ArrayList<>();
                        for (int i = 0; i < inFluids; i++) {
                            var fluid = BuiltInRegistries.FLUID.get(buf.readResourceLocation());
                            int amt = buf.readVarInt();
                            fluidInputs.add(new FluidStack(fluid, amt));
                        }

                        int outItems = buf.readVarInt();
                        List<ItemStack> itemOutputs = new ArrayList<>();
                        for (int i = 0; i < outItems; i++) {
                            Item item = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
                            int count = buf.readVarInt();
                            if (item != null) {
                                itemOutputs.add(new ItemStack(item, count));
                            }
                        }

                        int outFluids = buf.readVarInt();
                        List<FluidStack> fluidOutputs = new ArrayList<>();
                        for (int i = 0; i < outFluids; i++) {
                            var fluid = BuiltInRegistries.FLUID.get(buf.readResourceLocation());
                            int amt = buf.readVarInt();
                            fluidOutputs.add(new FluidStack(fluid, amt));
                        }

                        int time = buf.readVarInt();
                        int energy = buf.readVarInt();

                        return new CrystallizerRecipe(
                                null,
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
    public StreamCodec<RegistryFriendlyByteBuf, CrystallizerRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
