package com.raven.arsimplemachines.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class SuitData {

    // Read the suit's minecraft:custom_data component
    public static CompoundTag get(ItemStack suit) {
        CustomData data = suit.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    // Write back to minecraft:custom_data
    public static void set(ItemStack suit, CompoundTag tag) {
        suit.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
