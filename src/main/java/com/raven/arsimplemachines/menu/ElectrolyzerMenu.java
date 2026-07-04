package com.raven.arsimplemachines.menu;

import com.raven.arsimplemachines.blockentity.ElectrolyzerControllerBlockEntity;
import com.raven.arsimplemachines.registry.ModBlocks;
import com.raven.arsimplemachines.registry.ModMenuTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ElectrolyzerMenu extends AbstractContainerMenu {

    private final ElectrolyzerControllerBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final BlockPos pos;

    public ElectrolyzerControllerBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public ElectrolyzerMenu(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
        this(windowId, playerInv, resolvePos(playerInv, buf));
    }

    private static BlockPos resolvePos(Inventory inv, FriendlyByteBuf buf) {
        if (buf != null) {
            return buf.readBlockPos();
        }
        return inv.player.blockPosition();
    }

    public ElectrolyzerMenu(int windowId, Inventory playerInv, BlockPos pos) {
        super(ModMenuTypes.ELECTROLYZER_MENU.get(), windowId);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(playerInv.player.level(), pos);

        ElectrolyzerControllerBlockEntity be = null;
        if (playerInv.player.level().getBlockEntity(pos) instanceof ElectrolyzerControllerBlockEntity r) {
            be = r;
        }
        this.blockEntity = be;

        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.ELECTROLYZER_CONTROLLER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    // ---------------------------------------------------------
    //  PLAYER INVENTORY HELPERS
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    //  GUI SYNC HELPERS
    // ---------------------------------------------------------

    // Progress bar
    public int getProgress() {
        if (blockEntity == null) return 0;
        return blockEntity.getRecipeProgress();
    }

    public int getMaxProgress() {
        if (blockEntity == null) return 0;
        return blockEntity.getRecipeMaxProgress();
    }

    public int getProgressScaled(int pixels) {
        int max = getMaxProgress();
        if (max == 0) return 0;
        return getProgress() * pixels / max;
    }

    // ---------------------------------------------------------
    //  ENERGY
    // ---------------------------------------------------------
    public int getEnergyStored() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientEnergyStored();
    }

    public int getEnergyMax() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientEnergyMax();
    }

    public int getEnergyScaled(int pixels) {
        int max = getEnergyMax();
        if (max == 0) return 0;
        return getEnergyStored() * pixels / max;
    }

    // ---------------------------------------------------------
    //  INPUT TANK
    // ---------------------------------------------------------
    public int getInputAmount() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientInputAmount();
    }

    public int getInputCapacity() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientInputCapacity();
    }

    public int getInputScaled(int pixels) {
        int cap = getInputCapacity();
        if (cap == 0) return 0;
        return getInputAmount() * pixels / cap;
    }

    // ---------------------------------------------------------
    //  OUTPUT A
    // ---------------------------------------------------------
    public int getOutputAAmount() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientOutputAAmount();
    }

    public int getOutputACapacity() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientOutputACapacity();
    }

    public int getOutputAScaled(int pixels) {
        int cap = getOutputACapacity();
        if (cap == 0) return 0;
        return getOutputAAmount() * pixels / cap;
    }

    // ---------------------------------------------------------
    //  OUTPUT B
    // ---------------------------------------------------------
    public int getOutputBAmount() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientOutputBAmount();
    }

    public int getOutputBCapacity() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientOutputBCapacity();
    }

    public int getOutputBScaled(int pixels) {
        int cap = getOutputBCapacity();
        if (cap == 0) return 0;
        return getOutputBAmount() * pixels / cap;
    }

    // ---------------------------------------------------------
    //  STATUS MESSAGE
    // ---------------------------------------------------------
    public String getStatusMessage() {
        if (blockEntity == null) {
            return "No controller found";
        }

        if (getEnergyStored() < (getEnergyMax() * 0.10)) {
            return "Not enough energy";
        }

        if (getInputAmount() == 0) {
            return "Missing input fluid";
        }

        if (getOutputAAmount() >= getOutputACapacity()) {
            return "Output A full";
        }

        if (getOutputBAmount() >= getOutputBCapacity()) {
            return "Output B full";
        }

        if (getProgress() > 0 && getProgress() < getMaxProgress()) {
            return "Processing...";
        }

        return "Idle";
    }
}
