package com.raven.arsimplemachines.menu;

import com.raven.arsimplemachines.blockentity.FluidConverterBlockEntity;
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
import net.minecraft.world.level.block.entity.BlockEntity;

public class FluidConverterMenu extends AbstractContainerMenu {

    private final FluidConverterBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final BlockPos pos;

    public FluidConverterMenu(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
        this(windowId, playerInv, buf != null ? buf.readBlockPos() : playerInv.player.blockPosition());
    }

    public FluidConverterMenu(int windowId, Inventory playerInv, BlockPos pos) {
        super(ModMenuTypes.FLUID_CONVERTER_MENU.get(), windowId);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(playerInv.player.level(), pos);

        FluidConverterBlockEntity be = null;
        BlockEntity raw = playerInv.player.level().getBlockEntity(pos);
        if (raw instanceof FluidConverterBlockEntity f) {
            be = f;
        }
        this.blockEntity = be;

        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.FLUID_CONVERTER_CONTROLLER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private void addPlayerInventory(Inventory playerInv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 89 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInv) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 147));
        }
    }

    public int getProgress() {
        return blockEntity != null ? blockEntity.getRecipeProgress() : 0;
    }

    public int getMaxProgress() {
        return blockEntity != null ? blockEntity.getRecipeMaxProgress() : 0;
    }

    public int getProgressScaled(int pixels) {
        int max = getMaxProgress();
        if (max == 0) return 0;
        return getProgress() * pixels / max;
    }

    public int getPowerStored() {
        return blockEntity != null ? blockEntity.getClientEnergyStored() : 0;
    }

    public int getMaxPower() {
        return blockEntity != null ? blockEntity.getClientEnergyMax() : 0;
    }

    public int getPowerScaled(int pixels) {
        int stored = getPowerStored();
        int max = getMaxPower();
        if (max == 0) return 0;
        return stored * pixels / max;
    }

    public int getInputFluidStored() {
        int stored = blockEntity != null ? blockEntity.getClientInputFluidStored() : 0;
        int cap = getInputFluidCapacity();
        return cap <= 0 ? 0 : Math.min(stored, cap);
    }

    public int getInputFluidCapacity() {
        return blockEntity != null ? blockEntity.getClientInputFluidCapacity() : 0;
    }

    public String getInputFluidName() {
        return blockEntity != null ? blockEntity.getClientInputFluidName() : "";
    }

    public String getOutputFluidName() {
        return blockEntity != null ? blockEntity.getClientOutputFluidName() : "";
    }

    public int getInputFluidScaled(int pixels) {
        int cap = getInputFluidCapacity();
        if (cap <= 0) return 0;
        int scaled = getInputFluidStored() * pixels / cap;
        return scaled <= 0 && getInputFluidStored() > 0 ? 1 : scaled;
    }

    public int getOutputFluidStored() {
        int stored = blockEntity != null ? blockEntity.getClientOutputFluidStored() : 0;
        int cap = getOutputFluidCapacity();
        return cap <= 0 ? 0 : Math.min(stored, cap);
    }

    public int getOutputFluidCapacity() {
        return blockEntity != null ? blockEntity.getClientOutputFluidCapacity() : 0;
    }

    public int getOutputFluidScaled(int pixels) {
        int cap = getOutputFluidCapacity();
        if (cap <= 0) return 0;
        int scaled = getOutputFluidStored() * pixels / cap;
        return scaled <= 0 && getOutputFluidStored() > 0 ? 1 : scaled;
    }

    public boolean isRecipeRunning() {
        int p = getProgress();
        int max = getMaxProgress();
        return p > 0 && p < max;
    }
}