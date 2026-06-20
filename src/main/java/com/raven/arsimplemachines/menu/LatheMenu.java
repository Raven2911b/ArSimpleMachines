package com.raven.arsimplemachines.menu;

import com.raven.arsimplemachines.blockentity.LatheControllerBlockEntity;
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
import net.neoforged.neoforge.items.SlotItemHandler;

public class LatheMenu extends AbstractContainerMenu {

    private final LatheControllerBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final BlockPos pos;

    // CLIENT → SERVER constructor
    public LatheMenu(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
        this(windowId, playerInv, buf != null ? buf.readBlockPos() : playerInv.player.blockPosition());
    }

    // SERVER → CLIENT constructor
    public LatheMenu(int windowId, Inventory playerInv, BlockPos pos) {
        super(ModMenuTypes.LATHE_MENU.get(), windowId);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(playerInv.player.level(), pos);

        LatheControllerBlockEntity be = null;
        BlockEntity raw = playerInv.player.level().getBlockEntity(pos);
        if (raw instanceof LatheControllerBlockEntity l) {
            be = l;
        }
        this.blockEntity = be;

        // Machine slots (input/output)
        if (blockEntity != null) {
            this.addSlot(new SlotItemHandler(blockEntity.getInputHandler(), 0, 44, 35));
            this.addSlot(new SlotItemHandler(blockEntity.getOutputHandler(), 0, 116, 35));
        } else {
            // Dummy fallback slots
            this.addSlot(new Slot(playerInv, 0, 44, 35));
            this.addSlot(new Slot(playerInv, 1, 116, 35));
        }

        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.LATHE_CONTROLLER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack retStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            retStack = stack.copy();

            // Machine slots → player inventory
            if (index == 0 || index == 1) {
                if (!this.moveItemStackTo(stack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Player inventory → machine input
            else {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }

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

    // -------------------------
    // Progress Bar Helpers
    // -------------------------

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

    // -------------------------
    // Energy Bar Helpers (optional)
    // -------------------------

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

}
