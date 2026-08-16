package com.raven.arsimplemachines.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;

public class TagDebug {

    public static void dumpTags(ServerLevel level) {

        System.out.println("=== ARSimpleMachines TAG DEBUG START ===");

        var itemRegistry = level.registryAccess().registryOrThrow(Registries.ITEM);

        // Iterate all tag keys
        itemRegistry.getTagNames().forEach(tagKey -> {

            ResourceLocation id = tagKey.location();

            // Only show YOUR mod's tags
            if (!id.getNamespace().equals("arsimplemachines"))
                return;

            System.out.println("TAG: " + id);

            TagKey<Item> key = TagKey.create(Registries.ITEM, id);

            // NeoForge 1.21 returns Optional<HolderSet<Item>>
            var optSet = itemRegistry.getTag(key);

            if (optSet.isEmpty()) {
                System.out.println("  (EMPTY TAG)");
                return;
            }

            HolderSet<Item> set = optSet.get();

            set.stream().forEach(holder -> {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(holder.value());
                System.out.println("  - " + itemId);
            });
        });

        System.out.println("=== ARSimpleMachines TAG DEBUG END ===");
    }
}
