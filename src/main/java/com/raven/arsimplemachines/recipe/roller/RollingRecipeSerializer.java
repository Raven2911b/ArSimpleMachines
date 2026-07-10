package com.raven.arsimplemachines.recipe.roller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.raven.arsimplemachines.recipe.CategoryInput;
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

public class RollingRecipeSerializer implements RecipeSerializer<RollingRecipe> {

    private static final Codec<ItemStack> ITEM_STACK_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("item").forGetter(s -> BuiltInRegistries.ITEM.getKey(s.getItem())),
                    Codec.INT.fieldOf("count").forGetter(ItemStack::getCount)
            ).apply(instance, (id, count) -> {
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item == null) {
                    return ItemStack.EMPTY; // skip ghost items
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

    private static final Codec<CategoryInput> CATEGORY_INPUT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("category").forGetter(ci -> ci.category),
                    Codec.INT.fieldOf("count").forGetter(ci -> ci.count)
            ).apply(instance, CategoryInput::new)
    );

    // ---------------------------------------------------------
    // JSON CODEC — ID IS ASSIGNED BY MINECRAFT, NOT HERE
    // ---------------------------------------------------------
    private static final MapCodec<RollingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ITEM_STACK_CODEC.listOf().optionalFieldOf("item_inputs", List.of()).forGetter(RollingRecipe::getItemInputs),
                    CATEGORY_INPUT_CODEC.listOf().optionalFieldOf("item_categories", List.of()).forGetter(RollingRecipe::getItemCategories),
                    FLUID_STACK_CODEC.listOf().optionalFieldOf("fluid_inputs", List.of()).forGetter(RollingRecipe::getFluidInputs),
                    ITEM_STACK_CODEC.listOf().optionalFieldOf("item_outputs", List.of()).forGetter(RollingRecipe::getItemOutputs),
                    FLUID_STACK_CODEC.listOf().optionalFieldOf("fluid_outputs", List.of()).forGetter(RollingRecipe::getFluidOutputs),
                    Codec.INT.fieldOf("processing_time").forGetter(RollingRecipe::getProcessingTime),
                    Codec.INT.fieldOf("energy_per_tick").forGetter(RollingRecipe::getEnergyPerTick)
            ).apply(instance, (itemInputs, categoryInputs, fluidInputs, itemOutputs, fluidOutputs, time, energy) ->
                    new RollingRecipe(
                            null,   // ✔ ID assigned later by Minecraft
                            itemInputs,
                            categoryInputs,
                            fluidInputs,
                            itemOutputs,
                            fluidOutputs,
                            time,
                            energy
                    )
            )
    );

    @Override
    public MapCodec<RollingRecipe> codec() {
        return CODEC;
    }

    // ---------------------------------------------------------
    // STREAM CODEC — WE WRITE/READ THE REAL ID
    // ---------------------------------------------------------
    private static final StreamCodec<RegistryFriendlyByteBuf, RollingRecipe> STREAM_CODEC =
            StreamCodec.of(

                    // WRITE
                    (buf, recipe) -> {

                        // ITEM INPUTS
                        buf.writeVarInt(recipe.getItemInputs().size());
                        for (ItemStack s : recipe.getItemInputs()) {
                            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(s.getItem()));
                            buf.writeVarInt(s.getCount());
                        }

                        // ITEM CATEGORIES
                        buf.writeVarInt(recipe.getItemCategories().size());
                        for (CategoryInput ci : recipe.getItemCategories()) {
                            buf.writeUtf(ci.category);
                            buf.writeVarInt(ci.count);
                        }

                        // FLUID INPUTS
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

                        // FLUID OUTPUTS
                        buf.writeVarInt(recipe.getFluidOutputs().size());
                        for (FluidStack fs : recipe.getFluidOutputs()) {
                            buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(fs.getFluid()));
                            buf.writeVarInt(fs.getAmount());
                        }

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

                        // ITEM CATEGORIES
                        int catCount = buf.readVarInt();
                        List<CategoryInput> itemCategories = new ArrayList<>();
                        for (int i = 0; i < catCount; i++) {
                            String category = buf.readUtf();
                            int count = buf.readVarInt();
                            itemCategories.add(new CategoryInput(category, count));
                        }

                        // FLUID INPUTS
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

                        // FLUID OUTPUTS
                        int outFluids = buf.readVarInt();
                        List<FluidStack> fluidOutputs = new ArrayList<>();
                        for (int i = 0; i < outFluids; i++) {
                            var fluid = BuiltInRegistries.FLUID.get(buf.readResourceLocation());
                            int amt = buf.readVarInt();
                            fluidOutputs.add(new FluidStack(fluid, amt));
                        }

                        int time = buf.readVarInt();
                        int energy = buf.readVarInt();

                        // ID IS ASSIGNED BY NEOFORGE — PASS NULL
                        return new RollingRecipe(
                                null,
                                itemInputs,
                                itemCategories,
                                fluidInputs,
                                itemOutputs,
                                fluidOutputs,
                                time,
                                energy
                        );
                    }
            );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RollingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
