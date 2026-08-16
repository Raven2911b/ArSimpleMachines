package com.raven.arsimplemachines.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record TagInput(ResourceLocation tag, int count) {

    public static final Codec<TagInput> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("tag").forGetter(TagInput::tag),
                    Codec.INT.fieldOf("count").forGetter(TagInput::count)
            ).apply(instance, TagInput::new)
    );
}
