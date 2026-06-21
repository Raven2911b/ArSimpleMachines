package com.raven.arsimplemachines.menu;

import com.raven.arsimplemachines.blockentity.GasChargePadBlockEntity;
import com.raven.arsimplemachines.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.neoforged.neoforge.items.SlotItemHandler;

public class GasChargePadMenu extends AbstractContainerMenu {

    private final GasChargePadBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    // Sync fluid amount + capacity
    private final DataSlot gasStored = DataSlot.standalone();
    private final DataSlot maxGas = DataSlot.standalone();

    // Sync fluid TYPE as characters
    private final DataSlot fluidLength = DataSlot.standalone();
    private final DataSlot[] fluidChars;

    public GasChargePadMenu(int windowId, Inventory inv, BlockPos pos) {
        super(ModMenuTypes.GAS_CHARGE_PAD_MENU.get(), windowId);

        this.access = ContainerLevelAccess.create(inv.player.level(), pos);
        this.blockEntity = (GasChargePadBlockEntity) inv.player.level().getBlockEntity(pos);

        // Max fluid ID length we allow (safe upper bound)
        int maxLen = 64;
        fluidChars = new DataSlot[maxLen];

        addDataSlot(gasStored);
        addDataSlot(maxGas);
        addDataSlot(fluidLength);

        for (int i = 0; i < maxLen; i++) {
            fluidChars[i] = DataSlot.standalone();
            addDataSlot(fluidChars[i]);
        }

        if (blockEntity != null) {
            gasStored.set(blockEntity.getGasStored());
            maxGas.set(blockEntity.getMaxGas());

            String type = blockEntity.getGasType();
            if (type == null) type = "";

            fluidLength.set(type.length());

            for (int i = 0; i < type.length(); i++) {
                fluidChars[i].set(type.charAt(i));
            }
        }

        // Input bucket slot
        this.addSlot(new FluidInputSlot(blockEntity.getItems(), 0, 56, 21));

        // Output bucket slot
        this.addSlot(new FluidOutputSlot(blockEntity.getItems(), 1, 56, 51));

        // Player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new net.minecraft.world.inventory.Slot(
                        inv,
                        col + row * 9 + 9,
                        8 + col * 18,
                        89 + row * 18
                ));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new net.minecraft.world.inventory.Slot(
                    inv,
                    col,
                    8 + col * 18,
                    147
            ));
        }
    }

    // Decode full fluid ID string
    public String getGasType() {
        int len = fluidLength.get();
        if (len <= 0) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append((char) fluidChars[i].get());
        }
        return sb.toString();
    }

    public int getGasStored() {
        return gasStored.get();
    }

    public int getMaxGas() {
        return maxGas.get();
    }

    public GasChargePadBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, blockEntity.getBlockState().getBlock());
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (blockEntity != null) {
            gasStored.set(blockEntity.getGasStored());
            maxGas.set(blockEntity.getMaxGas());

            String type = blockEntity.getGasType();
            if (type == null) type = "";

            fluidLength.set(type.length());

            for (int i = 0; i < type.length(); i++) {
                fluidChars[i].set(type.charAt(i));
            }
        }
    }
}
