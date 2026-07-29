package com.raven.arsimplemachines.menu;

import com.raven.arsimplemachines.blockentity.CrystallizerControllerBlockEntity;
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

public class CrystallizerMenu extends AbstractContainerMenu {

    private CrystallizerControllerBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final BlockPos pos;

    public CrystallizerMenu(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
        this(windowId, playerInv, resolvePos(playerInv, buf));
    }

    private static BlockPos resolvePos(Inventory inv, FriendlyByteBuf buf) {
        if (buf != null) {
            return buf.readBlockPos();
        }
        return inv.player.blockPosition();
    }

    public CrystallizerMenu(int windowId, Inventory playerInv, BlockPos pos) {
        super(ModMenuTypes.CRYSTALLIZER_MENU.get(), windowId);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(playerInv.player.level(), pos);

        if (playerInv.player.level().getBlockEntity(pos) instanceof CrystallizerControllerBlockEntity c) {
            this.blockEntity = c;
        } else {
            this.blockEntity = null;
        }

        // MACHINE SLOTS: 2 inputs, 1 output
        if (this.blockEntity != null) {
            var input = blockEntity.getInputHandler();
            var output = blockEntity.getOutputHandler();

            // Input slot 0
            this.addSlot(new SlotItemHandler(input, 0, 44, 35));

            // Input slot 1 (second item input)
            this.addSlot(new SlotItemHandler(input, 1, 62, 35));

            // Output slot
            this.addSlot(new SlotItemHandler(output, 0, 116, 35));

        } else {
            // Dummy slots
            this.addSlot(new Slot(playerInv, 0, 44, 35));
            this.addSlot(new Slot(playerInv, 1, 62, 35));
            this.addSlot(new Slot(playerInv, 2, 116, 35));
        }

        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
    }

    // ---------------------------------------------------------
    //  DYNAMIC BE RESOLUTION
    // ---------------------------------------------------------
    public CrystallizerControllerBlockEntity getBlockEntity() {

        if (this.blockEntity != null) {
            return this.blockEntity;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.level != null) {
            var be = mc.level.getBlockEntity(pos);
            if (be instanceof CrystallizerControllerBlockEntity c) {
                this.blockEntity = c;
                return c;
            }
        }

        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.CRYSTALLIZER_CONTROLLER.get());
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

        // 0–2: machine slots, 3–29: player inventory, 30–38: hotbar
        if (index <= 2) {
            if (!this.moveItemStackTo(stack, 3, 39, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(stack, 0, 2, false)) {
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
    //  GUI SYNC HELPERS
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
