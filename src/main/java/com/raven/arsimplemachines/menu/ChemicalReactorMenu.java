package com.raven.arsimplemachines.menu;

import com.raven.arsimplemachines.blockentity.ChemicalReactorControllerBlockEntity;
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

public class ChemicalReactorMenu extends AbstractContainerMenu {

    private final ChemicalReactorControllerBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final BlockPos pos;

    public ChemicalReactorControllerBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public ChemicalReactorMenu(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
        this(windowId, playerInv, resolvePos(playerInv, buf));
    }

    private static BlockPos resolvePos(Inventory inv, FriendlyByteBuf buf) {
        if (buf != null) {
            return buf.readBlockPos();
        }
        return inv.player.blockPosition();
    }

    public ChemicalReactorMenu(int windowId, Inventory playerInv, BlockPos pos) {
        super(ModMenuTypes.CHEMICAL_REACTOR_MENU.get(), windowId);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(playerInv.player.level(), pos);

        ChemicalReactorControllerBlockEntity be = null;
        if (playerInv.player.level().getBlockEntity(pos) instanceof ChemicalReactorControllerBlockEntity r) {
            be = r;
        }
        this.blockEntity = be;

        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.CHEMICAL_REACTOR_CONTROLLER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    // ---------------------------------------------------------
    // PLAYER INVENTORY HELPERS
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
    // GUI SYNC HELPERS
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
    // ENERGY BLOCK A
    // ---------------------------------------------------------
    public int getEnergyStoredA() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientEnergyStoredA();
    }

    public int getEnergyMaxA() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientEnergyMaxA();
    }

    public int getEnergyScaledA(int pixels) {
        int max = getEnergyMaxA();
        if (max == 0) return 0;
        return getEnergyStoredA() * pixels / max;
    }

    // ---------------------------------------------------------
    // ENERGY BLOCK B
    // ---------------------------------------------------------
    public int getEnergyStoredB() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientEnergyStoredB();
    }

    public int getEnergyMaxB() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientEnergyMaxB();
    }

    public int getEnergyScaledB(int pixels) {
        int max = getEnergyMaxB();
        if (max == 0) return 0;
        return getEnergyStoredB() * pixels / max;
    }

    // ---------------------------------------------------------
    // INPUT TANK A
    // ---------------------------------------------------------
    public String getInputAName() {
        if (blockEntity == null) return "";
        return blockEntity.getClientInputAName();
    }

    public int getInputAAmount() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientInputAAmount();
    }

    public int getInputACapacity() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientInputACapacity();
    }

    public int getInputAScaled(int pixels) {
        int cap = getInputACapacity();
        if (cap == 0) return 0;
        return getInputAAmount() * pixels / cap;
    }

    // ---------------------------------------------------------
    // INPUT TANK B
    // ---------------------------------------------------------
    public String getInputBName() {
        if (blockEntity == null) return "";
        return blockEntity.getClientInputBName();
    }

    public int getInputBAmount() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientInputBAmount();
    }

    public int getInputBCapacity() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientInputBCapacity();
    }

    public int getInputBScaled(int pixels) {
        int cap = getInputBCapacity();
        if (cap == 0) return 0;
        return getInputBAmount() * pixels / cap;
    }

    // ---------------------------------------------------------
    // OUTPUT TANK
    // ---------------------------------------------------------
    public String getOutputName() {
        if (blockEntity == null) return "";
        return blockEntity.getClientOutputName();
    }

    public int getOutputAmount() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientOutputAmount();
    }

    public int getOutputCapacity() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientOutputCapacity();
    }

    public int getOutputScaled(int pixels) {
        int cap = getOutputCapacity();
        if (cap == 0) return 0;
        return getOutputAmount() * pixels / cap;
    }

    // ---------------------------------------------------------
    // STATUS MESSAGE
    // ---------------------------------------------------------
    public String getStatusMessage() {
        if (blockEntity == null) {
            return "No controller found";
        }

        // Not enough energy (either block A or B)
        if (getEnergyStoredA() < (getEnergyMaxA() * 0.10) ||
                getEnergyStoredB() < (getEnergyMaxB() * 0.10)) {
            return "Not enough energy";
        }

        // Missing input A
        if (getInputAAmount() == 0) {
            return "Missing input fluid A";
        }

        // Missing input B
        if (getInputBAmount() == 0) {
            return "Missing input fluid B";
        }

        // Output tank full
        if (getOutputAmount() >= getOutputCapacity()) {
            return "Output tank full";
        }

        // Processing
        if (getProgress() > 0 && getProgress() < getMaxProgress()) {
            return "Processing...";
        }

        // Idle
        return "Idle";
    }
}
