package com.raven.arsimplemachines.menu;

import com.raven.arsimplemachines.blockentity.RollingControllerBlockEntity;
import com.raven.arsimplemachines.registry.ModBlocks;
import com.raven.arsimplemachines.registry.ModMenuTypes;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class RollingMenu extends AbstractContainerMenu {

    private RollingControllerBlockEntity blockEntity;   // now mutable
    private final ContainerLevelAccess access;
    private final BlockPos pos;

    public RollingMenu(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
        this(windowId, playerInv, resolvePos(playerInv, buf));
    }

    private static BlockPos resolvePos(Inventory inv, FriendlyByteBuf buf) {
        if (buf != null) {
            return buf.readBlockPos();
        }
        return inv.player.blockPosition();
    }

    public RollingMenu(int windowId, Inventory playerInv, BlockPos pos) {
        super(ModMenuTypes.ROLLING_MENU.get(), windowId);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(playerInv.player.level(), pos);

        // Try initial BE lookup (may be null on client)
        if (playerInv.player.level().getBlockEntity(pos) instanceof RollingControllerBlockEntity r) {
            this.blockEntity = r;
        } else {
            this.blockEntity = null;
        }

        // MACHINE SLOTS
        if (this.blockEntity != null) {
            this.addSlot(new SlotItemHandler(blockEntity.getInputHandler(), 0, 44, 35));
            this.addSlot(new SlotItemHandler(blockEntity.getOutputHandler(), 0, 126, 35));
        } else {
            // dummy slots so client doesn't crash
            this.addSlot(new Slot(playerInv, 0, 44, 35));
            this.addSlot(new Slot(playerInv, 1, 126, 35));
        }

        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
    }

    // ---------------------------------------------------------
    //  DYNAMIC BE RESOLUTION (CRITICAL FIX)
    // ---------------------------------------------------------
    public RollingControllerBlockEntity getBlockEntity() {

        // If we already have it, use it
        if (this.blockEntity != null) {
            return this.blockEntity;
        }

        // Try resolving again (client-side)
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.level != null) {
            var be = mc.level.getBlockEntity(pos);
            if (be instanceof RollingControllerBlockEntity r) {
                this.blockEntity = r;
                return r;
            }
        }

        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.ROLLING_CONTROLLER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack retStack = ItemStack.EMPTY;

        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return retStack;
        }

        ItemStack stack = slot.getItem();
        retStack = stack.copy();

        // 0–1: machine slots, 2–28: player inventory, 29–37: hotbar
        if (index == 0 || index == 1) {
            if (!this.moveItemStackTo(stack, 2, 38, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return retStack;
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
    //  GUI SYNC HELPERS (now using dynamic BE)
    // ---------------------------------------------------------
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

    public int getFluidAmount() {
        var be = getBlockEntity();
        return be == null ? 0 : be.getClientFluidAmount();
    }

    public int getFluidCapacity() {
        var be = getBlockEntity();
        return be == null ? 0 : be.getClientFluidCapacity();
    }

    public int getFluidScaled(int pixels) {
        int cap = getFluidCapacity();
        if (cap == 0) return 0;
        return getFluidAmount() * pixels / cap;
    }

    public int getPowerScaled(int pixels) {
        int max = getMaxPower();
        if (max == 0) return 0;
        return getPowerStored() * pixels / max;
    }
}
