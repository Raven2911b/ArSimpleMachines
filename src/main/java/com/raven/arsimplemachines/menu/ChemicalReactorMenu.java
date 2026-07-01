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

        // Reactor has NO item slots — but GUI must have consistent slot count
        // So we add 0 machine slots and ONLY player inventory slots.

        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.CHEMICAL_REACTOR_CONTROLLER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // No machine slots → nothing to move
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

    // Power bar
    public int getPowerStored() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientEnergyStored();
    }

    public int getMaxPower() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientEnergyMax();
    }

    public int getPowerScaled(int pixels) {
        int max = getMaxPower();
        if (max == 0) return 0;
        return getPowerStored() * pixels / max;
    }

    // Hydrogen tank
    public int getHydrogenAmount() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientHydrogenAmount();
    }

    public int getHydrogenCapacity() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientHydrogenCapacity();
    }

    public int getHydrogenScaled(int pixels) {
        int cap = getHydrogenCapacity();
        if (cap == 0) return 0;
        return getHydrogenAmount() * pixels / cap;
    }

    // Oxygen tank
    public int getOxygenAmount() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientOxygenAmount();
    }

    public int getOxygenCapacity() {
        if (blockEntity == null) return 0;
        return blockEntity.getClientOxygenCapacity();
    }

    public int getOxygenScaled(int pixels) {
        int cap = getOxygenCapacity();
        if (cap == 0) return 0;
        return getOxygenAmount() * pixels / cap;
    }

    // Output tank
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
}
