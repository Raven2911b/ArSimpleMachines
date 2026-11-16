package com.raven.arsimplemachines.hud;

import advRocketry.Items.ItemPortablePressureTank;
import advRocketry.SpaceSuit.ChestPlate;
import advRocketry.SpaceSuit.ISpaceSuitInventory;
import advRocketry.SpaceSuit.SpaceSuit;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class SpaceSuitHud {
    private static final int BAR_W = 64;
    private static final int BAR_H = 10;

    private SpaceSuitHud() {}

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!hasFullSuit(mc)) return;

        ItemStack chest = mc.player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof ChestPlate)) return;

        CompoundTag data = ((ChestPlate) chest.getItem()).getCachedDataUnsafe(chest);
        int oxygenValue = data.contains("oxygen") ? data.getInt("oxygen") : 0;
        int hydrogenValue = data.contains("hydrogen") ? data.getInt("hydrogen") : 0;

        int oxygenMax = getOxygenCapacity(chest, mc);
        int hydrogenMax = getHydrogenCapacity(chest, mc);

        int x = 10;
        int y = mc.getWindow().getGuiScaledHeight() - 40;

        drawBar(graphics, x + 6, y + 6, BAR_W, BAR_H, oxygenValue, oxygenMax, 0x7F5DD6FF, 0xFF5DD6FF);
        drawBar(graphics, x + 6, y + 18, BAR_W, BAR_H, hydrogenValue, hydrogenMax, 0x7F8EF4A1, 0xFF8EF4A1);
    }

    private static boolean hasFullSuit(Minecraft mc) {
        for (EquipmentSlot slot : new EquipmentSlot[] {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        }) {
            ItemStack stack = mc.player.getItemBySlot(slot);
            if (!(stack.getItem() instanceof SpaceSuit)) {
                return false;
            }
        }
        return true;
    }

    private static int getOxygenCapacity(ItemStack chest, Minecraft mc) {
        ItemStackHandler inv = ISpaceSuitInventory.loadInventory(chest, mc.player.registryAccess());
        if (inv == null) return 0;

        int total = 0;
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack tank = inv.getStackInSlot(i);
            if (tank.getItem() instanceof ItemPortablePressureTank pressureTank) {
                total += pressureTank.capacity;
            }
        }
        return total;
    }

    private static int getHydrogenCapacity(ItemStack chest, Minecraft mc) {
        ItemStackHandler inv = ISpaceSuitInventory.loadInventory(chest, mc.player.registryAccess());
        if (inv == null) return 0;

        ItemStack jetpack = inv.getStackInSlot(0);
        if (!(jetpack.getItem() instanceof advRocketry.SpaceSuit.Jetpack)) return 0;

        ItemStackHandler jetpackInv = ISpaceSuitInventory.loadInventory(jetpack, mc.player.registryAccess());
        if (jetpackInv == null) return 0;

        int total = 0;
        for (int i = 0; i < jetpackInv.getSlots(); i++) {
            ItemStack tank = jetpackInv.getStackInSlot(i);
            if (tank.getItem() instanceof ItemPortablePressureTank pressureTank) {
                total += pressureTank.capacity;
            }
        }
        return total;
    }

    private static void drawBar(GuiGraphics graphics, int x, int y, int w, int h, int value, int max, int fillColour, int accentColour) {
        int safeMax = Math.max(1, max);
        int fillW = max > 0 ? (int) ((value / (float) safeMax) * w) : 0;

        graphics.fill(x, y, x + w, y + h, 0x33000000);
        graphics.fill(x + 1, y + 1, x + 1 + fillW, y + h - 1, fillColour);
        graphics.fill(x + 1, y + 1, x + w - 1, y + 2, accentColour);
    }
}