package com.raven.arsimplemachines.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * A simple, deterministic replacement for tags.
 * Categories are fully controlled by your mod (and later KubeJS),
 * and do not rely on NeoForge's tag system.
 */
public class CategoryRegistry {

    private static final Map<String, Set<Item>> CATEGORY_ITEMS = new HashMap<>();

    /**
     * Register an item into a category.
     * Example: add("ingot/iron", Items.IRON_INGOT);
     */
    public static void add(String category, Item item) {

        // Skip null items
        if (item == null) {
            return;
        }

        // Skip dummy items (items from mods not installed)
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            return;
        }

        CATEGORY_ITEMS
                .computeIfAbsent(category, c -> new HashSet<>())
                .add(item);
    }



    /**
     * Check if a stack belongs to a category.
     */
    public static boolean matches(String category, ItemStack stack) {
        if (stack.isEmpty()) return false;
        Set<Item> set = CATEGORY_ITEMS.get(category);
        return set != null && set.contains(stack.getItem());
    }

    /**
     * Get all items registered to a category.
     */
    public static Set<Item> getItems(String category) {
        return CATEGORY_ITEMS.getOrDefault(category, Collections.emptySet());
    }

    /**
     * Debug helper: list all categories and their items.
     */
    public static Map<String, Set<Item>> dump() {
        return Collections.unmodifiableMap(CATEGORY_ITEMS);
    }
}
