//package com.raven.arsimplemachines.util;
//
//import net.minecraft.core.registries.Registries;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.tags.TagKey;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.level.material.Fluid;
//import net.minecraft.core.HolderSet;
//import net.minecraft.core.registries.BuiltInRegistries;
//
//public class TagDebug {
//
//    public static void dumpTags(ServerLevel level) {
//
//        System.out.println("=== ARSimpleMachines TAG DEBUG START ===");
//
//        // ---------------------------------------------------------
//        // ITEM TAGS
//        // ---------------------------------------------------------
//        var itemRegistry = level.registryAccess().registryOrThrow(Registries.ITEM);
//
//        itemRegistry.getTagNames().forEach(tagKey -> {
//
//            ResourceLocation id = tagKey.location();
//
//            if (!id.getNamespace().equals("arsimplemachines"))
//                return;
//
//            System.out.println("ITEM TAG: " + id);
//
//            TagKey<Item> key = TagKey.create(Registries.ITEM, id);
//
//            var optSet = itemRegistry.getTag(key);
//
//            if (optSet.isEmpty()) {
//                System.out.println("  (EMPTY TAG)");
//                return;
//            }
//
//            HolderSet<Item> set = optSet.get();
//
//            set.stream().forEach(holder -> {
//                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(holder.value());
//                System.out.println("  - " + itemId);
//            });
//        });
//
//        // ---------------------------------------------------------
//        // FLUID TAGS
//        // ---------------------------------------------------------
//        var fluidRegistry = level.registryAccess().registryOrThrow(Registries.FLUID);
//
//        fluidRegistry.getTagNames().forEach(tagKey -> {
//
//            ResourceLocation id = tagKey.location();
//
//            if (!id.getNamespace().equals("arsimplemachines"))
//                return;
//
//            System.out.println("FLUID TAG: " + id);
//
//            TagKey<Fluid> key = TagKey.create(Registries.FLUID, id);
//
//            var optSet = fluidRegistry.getTag(key);
//
//            if (optSet.isEmpty()) {
//                System.out.println("  (EMPTY TAG)");
//                return;
//            }
//
//            HolderSet<Fluid> set = optSet.get();
//
//            set.stream().forEach(holder -> {
//                ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(holder.value());
//                System.out.println("  - " + fluidId);
//            });
//        });
//
//        System.out.println("=== ARSimpleMachines TAG DEBUG END ===");
//    }
//}
