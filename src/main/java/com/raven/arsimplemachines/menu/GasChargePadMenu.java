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
    private final DataSlot gasTypeHash = DataSlot.standalone();

    // Synced values
    private final DataSlot gasStored = DataSlot.standalone();
    private final DataSlot maxGas = DataSlot.standalone();


    public GasChargePadMenu(int windowId, Inventory inv, BlockPos pos) {
        super(ModMenuTypes.GAS_CHARGE_PAD_MENU.get(), windowId);

        this.access = ContainerLevelAccess.create(inv.player.level(), pos);
        this.blockEntity = (GasChargePadBlockEntity) inv.player.level().getBlockEntity(pos);

        // Sync values from server → client
        addDataSlot(gasStored);
        addDataSlot(maxGas);
        addDataSlot(gasTypeHash);

        if (blockEntity != null) {
            gasStored.set(blockEntity.getGasStored());
            maxGas.set(blockEntity.getMaxGas());
            gasTypeHash.set(blockEntity.getGasType().hashCode());
        }

// Input bucket slot (top icon)
        this.addSlot(new SlotItemHandler(blockEntity.getItems(), 0, 56, 21));

// Output bucket slot (bottom icon)
        this.addSlot(new SlotItemHandler(blockEntity.getItems(), 1, 56, 51));

        // --- Player Inventory (3 rows of 9) ---
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

        // --- Player Hotbar (1 row of 9) ---
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new net.minecraft.world.inventory.Slot(
                    inv,
                    col,
                    8 + col * 18,
                    147
            ));
        }
    }
    public String getGasType() {
        int hash = gasTypeHash.get();

        if (hash == "oxygen".hashCode()) return "oxygen";
        if (hash == "hydrogen".hashCode()) return "hydrogen";
        if (hash == "nitrogen".hashCode()) return "nitrogen";

        return "";
    }

    // Expose synced values to the Screen
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
            gasTypeHash.set(blockEntity.getGasType().hashCode());
        }
    }
}
