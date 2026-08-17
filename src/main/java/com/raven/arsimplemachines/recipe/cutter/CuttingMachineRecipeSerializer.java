package com.raven.arsimplemachines.recipe.cutter;

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

import java.util.ArrayList;
import java.util.List;

public class CuttingMachineRecipeSerializer implements RecipeSerializer<CuttingMachineRecipe> {

    // ---------------------------------------------------------
    // ITEM STACK CODEC (same as Rolling)
    // ---------------------------------------------------------
    private static final Codec<ItemStack> ITEM_STACK_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("item").forGetter(s -> BuiltInRegistries.ITEM.getKey(s.getItem())),
                    Codec.INT.fieldOf("count").forGetter(ItemStack::getCount)
            ).apply(instance, (id, count) -> {
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item == null) return ItemStack.EMPTY;
                return new ItemStack(item, count);
            })
    );

    // ---------------------------------------------------------
    // TAG INPUT CODEC (same as Rolling)
    // ---------------------------------------------------------
    private static final Codec<TagInput> TAG_INPUT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("tag").forGetter(TagInput::tag),
                    Codec.INT.fieldOf("count").forGetter(TagInput::count)
            ).apply(instance, TagInput::new)
    );

    // ---------------------------------------------------------
    // MAIN JSON CODEC (same structure as Rolling)
    // ---------------------------------------------------------
    private static final MapCodec<CuttingMachineRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    // Cutting Machine does NOT use direct item inputs → always empty list
                    ITEM_STACK_CODEC.listOf().optionalFieldOf("item_inputs", List.of()).forGetter(CuttingMachineRecipe::getItemInputs),

                    // Cutting Machine uses TAG INPUTS ONLY
                    TAG_INPUT_CODEC.listOf().optionalFieldOf("item_tags", List.of()).forGetter(CuttingMachineRecipe::getItemTags),

                    // Cutting Machine does NOT use fluid inputs → always empty list
                    Codec.list(Codec.INT).optionalFieldOf("fluid_inputs", List.of()).xmap(
                            list -> List.of(), // ignore fluids entirely
                            fluids -> List.of()
                    ).forGetter(r -> List.of()),

                    // Item outputs
                    ITEM_STACK_CODEC.listOf().optionalFieldOf("item_outputs", List.of()).forGetter(CuttingMachineRecipe::getItemOutputs),

                    // Cutting Machine does NOT output fluids → always empty list
                    Codec.list(Codec.INT).optionalFieldOf("fluid_outputs", List.of()).xmap(
                            list -> List.of(),
                            fluids -> List.of()
                    ).forGetter(r -> List.of()),

                    Codec.INT.fieldOf("processing_time").forGetter(CuttingMachineRecipe::getProcessingTime),
                    Codec.INT.fieldOf("energy_per_tick").forGetter(CuttingMachineRecipe::getEnergyPerTick)
            ).apply(instance, (itemInputs, tagInputs, fluidInputs, itemOutputs, fluidOutputs, time, energy) ->
                    new CuttingMachineRecipe(
                            null,
                            itemInputs,
                            tagInputs,
                            List.of(),      // fluidInputs ignored
                            itemOutputs,
                            List.of(),      // fluidOutputs ignored
                            time,
                            energy
                    )
            )
    );

    @Override
    public MapCodec<CuttingMachineRecipe> codec() {
        return CODEC;
    }

    // ---------------------------------------------------------
    // STREAM CODEC (network sync) — same structure as Rolling
    // ---------------------------------------------------------
    private static final StreamCodec<RegistryFriendlyByteBuf, CuttingMachineRecipe> STREAM_CODEC =
            StreamCodec.of(

                    // ENCODE
                    (buf, recipe) -> {

                        // item_inputs (always empty for Cutting Machine)
                        buf.writeVarInt(recipe.getItemInputs().size());
                        for (ItemStack s : recipe.getItemInputs()) {
                            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(s.getItem()));
                            buf.writeVarInt(s.getCount());
                        }

                        // tag inputs
                        buf.writeVarInt(recipe.getItemTags().size());
                        for (TagInput ti : recipe.getItemTags()) {
                            buf.writeResourceLocation(ti.tag());
                            buf.writeVarInt(ti.count());
                        }

                        // fluid_inputs (always empty)
                        buf.writeVarInt(0);

                        // item_outputs
                        buf.writeVarInt(recipe.getItemOutputs().size());
                        for (ItemStack s : recipe.getItemOutputs()) {
                            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(s.getItem()));
                            buf.writeVarInt(s.getCount());
                        }

                        // fluid_outputs (always empty)
                        buf.writeVarInt(0);

                        buf.writeVarInt(recipe.getProcessingTime());
                        buf.writeVarInt(recipe.getEnergyPerTick());
                    },

                    // DECODE
                    buf -> {

                        // item_inputs
                        int inItems = buf.readVarInt();
                        List<ItemStack> itemInputs = new ArrayList<>();
                        for (int i = 0; i < inItems; i++) {
                            Item item = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
                            int count = buf.readVarInt();
                            if (item != null) itemInputs.add(new ItemStack(item, count));
                        }

                        // tag inputs
                        int tagCount = buf.readVarInt();
                        List<TagInput> itemTags = new ArrayList<>();
                        for (int i = 0; i < tagCount; i++) {
                            ResourceLocation tag = buf.readResourceLocation();
                            int count = buf.readVarInt();
                            itemTags.add(new TagInput(tag, count));
                        }

                        // fluid_inputs ignored
                        buf.readVarInt();

                        // item_outputs
                        int outItems = buf.readVarInt();
                        List<ItemStack> itemOutputs = new ArrayList<>();
                        for (int i = 0; i < outItems; i++) {
                            Item item = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
                            int count = buf.readVarInt();
                            if (item != null) itemOutputs.add(new ItemStack(item, count));
                        }

                        // fluid_outputs ignored
                        buf.readVarInt();

                        int time = buf.readVarInt();
                        int energy = buf.readVarInt();

                        return new CuttingMachineRecipe(
                                null,
                                itemInputs,
                                itemTags,
                                List.of(),
                                itemOutputs,
                                List.of(),
                                time,
                                energy
                        );
                    }
            );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CuttingMachineRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
