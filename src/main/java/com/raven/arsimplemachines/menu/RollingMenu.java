package com.raven.arsimplemachines.menu;

import com.raven.arsimplemachines.blockentity.RollingControllerBlockEntity;
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
import net.neoforged.neoforge.items.SlotItemHandler;

public class RollingMenu extends AbstractContainerMenu {

    private final RollingControllerBlockEntity blockEntity;
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

        RollingControllerBlockEntity be = null;
        if (playerInv.player.level().getBlockEntity(pos) instanceof RollingControllerBlockEntity r) {
            be = r;
        }
        this.blockEntity = be;

        // MACHINE SLOTS: always add 2 slots so slot count matches on both sides
        if (this.blockEntity != null) {
            this.addSlot(new SlotItemHandler(blockEntity.getInputHandler(), 0, 44, 35));   // input
            this.addSlot(new SlotItemHandler(blockEntity.getOutputHandler(), 0, 116, 35)); // output
        } else {
            // dummy slots bound to player inventory so they exist client-side
            this.addSlot(new Slot(playerInv, 0, 44, 35));
            this.addSlot(new Slot(playerInv, 1, 116, 35));
        }

        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
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
            // machine → player
            if (!this.moveItemStackTo(stack, 2, 38, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // player → machine input
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
                        84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInv) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col,
                    8 + col * 18,
                    142));
        }
    }

    // ---------------------------------------------------------
    //  GUI SYNC HELPERS
    // ---------------------------------------------------------
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
}
