package com.raven.arsimplemachines.menu;

import com.raven.arsimplemachines.blockentity.ElectricArcFurnaceControllerBlockEntity;
import com.raven.arsimplemachines.registry.ModBlocks;
import com.raven.arsimplemachines.registry.ModMenuTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ElectricArcFurnaceMenu extends AbstractContainerMenu {

    private ElectricArcFurnaceControllerBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final BlockPos pos;

    public ElectricArcFurnaceMenu(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
        this(windowId, playerInv, resolvePos(playerInv, buf));
    }

    private static BlockPos resolvePos(Inventory inv, FriendlyByteBuf buf) {
        if (buf != null) {
            return buf.readBlockPos();
        }
        return inv.player.blockPosition();
    }

    public ElectricArcFurnaceMenu(int windowId, Inventory playerInv, BlockPos pos) {
        super(ModMenuTypes.ELECTRIC_ARC_FURNACE_MENU.get(), windowId);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(playerInv.player.level(), pos);

        if (playerInv.player.level().getBlockEntity(pos) instanceof ElectricArcFurnaceControllerBlockEntity e) {
            this.blockEntity = e;
        } else {
            this.blockEntity = null;
        }

        // ❌ REMOVE MACHINE SLOTS — EAF uses external input/output blocks only
        // (no SlotItemHandler, no dummy slots)

        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
    }

    public ElectricArcFurnaceControllerBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack retStack = ItemStack.EMPTY;

        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        retStack = stack.copy();

        // Player inventory (0–35) → hotbar (36–44)
        if (index < 36) {
            if (!this.moveItemStackTo(stack, 36, 45, false)) return ItemStack.EMPTY;
        }
        // Hotbar → player inventory
        else {
            if (!this.moveItemStackTo(stack, 0, 36, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return retStack;
    }

    private void addPlayerInventory(Inventory playerInv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9,
                        8 + col * 18,
                        89 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInv) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col,
                    8 + col * 18,
                    147));
        }
    }

    public int getProgress() {
        var be = getBlockEntity();
        return be == null ? 0 : be.getRecipeProgress();
    }

    public int getMaxProgress() {
        var be = getBlockEntity();
        return be == null ? 0 : be.getRecipeMaxProgress();
    }

    public int getProgressScaled(int pixels) {
        int max = getMaxProgress();
        if (max == 0) return 0;
        return getProgress() * pixels / max;
    }

    public int getPowerStored() {
        var be = getBlockEntity();
        return be == null ? 0 : be.getClientEnergyStored();
    }

    public int getMaxPower() {
        var be = getBlockEntity();
        return be == null ? 0 : be.getClientEnergyMax();
    }

    public int getPowerScaled(int pixels) {
        int max = getMaxPower();
        if (max == 0) return 0;
        return getPowerStored() * pixels / max;
    }
    public void receiveTag(CompoundTag tag) {
        var be = getBlockEntity();
        if (be != null) {
            be.readClient(tag);
        }
    }
    public boolean isRecipeRunning() {
        int p = getProgress();
        int max = getMaxProgress();
        return p > 0 && p < max;
    }

}
