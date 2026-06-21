package com.raven.arsimplemachines.blockentity;

import com.raven.arsimplemachines.menu.GasChargePadMenu;
import com.raven.arsimplemachines.recipe.GasChargeRecipe;
import com.raven.arsimplemachines.recipe.GasChargeRecipeInput;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import com.raven.arsimplemachines.registry.ModRecipeTypes;
import com.raven.arsimplemachines.util.SuitData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;

public class GasChargePadBlockEntity extends BlockEntity implements MenuProvider {

    private static final int MAX_GAS = 10000;

    // JSON recipe system
    private GasChargeRecipe currentRecipe;
    private int processingTime = 0;
    private int maxProcessingTime = 0;

    // Single fluid tank
    private final FluidTank fluidTank = new FluidTank(MAX_GAS) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final ItemStackHandler items = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public ItemStackHandler getItems() {
        return items;
    }

    public GasChargePadBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GAS_CHARGE_PAD_BE.get(), pos, state);

    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    // NEW: used by ModCapabilities
    public FluidTank getFluidHandler() {
        return fluidTank;
    }
    // Used by GasChargePadMenu (hashCode only)
    public String getGasType() {
        FluidStack stack = fluidTank.getFluid();
        if (stack.isEmpty()) {
            return "";
        }
        // You can choose any ID scheme; here we use the fluid registry name
        return stack.getFluid().builtInRegistryHolder().key().location().toString();
    }


    private GasChargeRecipe findRecipe() {
        if (level == null) return null;

        FluidStack stack = fluidTank.getFluid();
        if (stack.isEmpty()) return null;

        GasChargeRecipeInput input = new GasChargeRecipeInput(stack.getFluid(), stack.getAmount());

        return level.getRecipeManager()
                .getRecipeFor(ModRecipeTypes.GAS_CHARGE_TYPE.get(), input, level)
                .map(holder -> holder.value())
                .orElse(null);
    }


    // ------------------------------
    // TICK LOGIC
    // ------------------------------
    public static void tick(Level level, BlockPos pos, BlockState state, GasChargePadBlockEntity be) {
        if (level.isClientSide) return;

        // ------------------------------
        // JSON RECIPE PROCESSING
        // ------------------------------
        if (be.currentRecipe == null) {
            be.currentRecipe = be.findRecipe();

            if (be.currentRecipe != null) {
                be.maxProcessingTime = be.currentRecipe.getProcessingTime();
                be.processingTime = 0;
            }
        }

        if (be.currentRecipe != null) {
            FluidStack tank = be.fluidTank.getFluid();

            if (tank.isEmpty() || tank.getAmount() < be.currentRecipe.getFluidAmount()) {
                be.currentRecipe = null;
                be.processingTime = 0;
            } else {
                be.processingTime++;

                if (be.processingTime >= be.maxProcessingTime) {
                    be.fluidTank.drain(be.currentRecipe.getFluidAmount(),
                            net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                    be.processingTime = 0;
                    be.currentRecipe = null;
                    be.setChanged();
                }
            }
        }
        // ------------------------------
        // BUCKET → TANK LOGIC (CLEAN)
        // ------------------------------
        ItemStack input = be.items.getStackInSlot(0);

        if (!input.isEmpty()) {

            var optionalFluid = net.neoforged.neoforge.fluids.FluidUtil.getFluidContained(input);

            if (optionalFluid.isPresent()) {
                FluidStack contained = optionalFluid.get();

                // Try to fill the tank directly
                int filled = be.fluidTank.fill(contained,
                        net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);

                if (filled > 0) {

                    // Remove the filled container
                    be.items.setStackInSlot(0, ItemStack.EMPTY);

                    // Output empty bucket
                    ItemStack empty = new ItemStack(Items.BUCKET);
                    ItemStack out = be.items.getStackInSlot(1);

                    if (out.isEmpty()) {
                        be.items.setStackInSlot(1, empty);
                    } else if (ItemStack.isSameItemSameComponents(out, empty)
                            && out.getCount() < out.getMaxStackSize()) {
                        out.grow(1);
                    }
                }
            }
        }

        // ------------------------------
        // PLAYER DETECTION
        // ------------------------------
        List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(0.5));
        if (players.isEmpty()) {
            return;
        }

        Player player = players.get(0);

        // ------------------------------
        // SUIT DETECTION
        // ------------------------------
        ItemStack suit = player.getInventory().armor.get(2);
        if (suit.isEmpty()) {
            return;
        }

        String suitId = suit.getItem().builtInRegistryHolder().key().location().toString();
        if (!suitId.equals("adv_rocketry:space_chestplate")) {
            return;
        }

        // ------------------------------
        // LOAD SUIT DATA
        // ------------------------------
        CompoundTag custom = SuitData.get(suit);
        HolderLookup.Provider provider = level.registryAccess();

        CompoundTag cTag = custom.getCompound("C");
        int oxygen = cTag.getInt("oxygen");
        int pressureTanks = cTag.getInt("pressureTanks");

        // ------------------------------
        // LOAD INTERNAL INVENTORY
        // ------------------------------
        ItemStackHandler inv = new ItemStackHandler(2);

        if (custom.contains("inventory")) {
            inv.deserializeNBT(provider, custom.getCompound("inventory"));
        }

        // ------------------------------
        // COUNT TANKS
        // ------------------------------
        int countedTanks = 0;

        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack tank = inv.getStackInSlot(i);
            if (tank.isEmpty()) continue;

            String tankId = tank.getItem().builtInRegistryHolder().key().location().toString();

            if (tankId.contains("portable_pressure_tank")) {
                countedTanks++;
            }
        }

        // ------------------------------
        // FILL SUIT OXYGEN FROM FLUID TANK
        // ------------------------------
        int maxOxygen = countedTanks * 4000;
        int space = maxOxygen - oxygen;

        if (space > 0 && !be.fluidTank.isEmpty()) {
            int transfer = Math.min(50, be.fluidTank.getFluidAmount());
            int added = Math.min(transfer, space);

            if (added > 0) {
                be.fluidTank.drain(added,
                        net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                oxygen += added;
                be.setChanged();
            }
        }

        // ------------------------------
        // SAVE SUIT DATA
        // ------------------------------
        cTag.putInt("oxygen", oxygen);
        cTag.putInt("pressureTanks", countedTanks);
        custom.put("C", cTag);

        custom.put("inventory", inv.serializeNBT(provider));

        SuitData.set(suit, custom);
    }

    // ------------------------------
    // GAS STORAGE LOGIC
    // ------------------------------
    public int getGasStored() {
        return fluidTank.getFluidAmount();
    }

    public int getMaxGas() {
        return MAX_GAS;
    }

    public boolean isEmpty() {
        return fluidTank.isEmpty();
    }

    public boolean isFull() {
        return fluidTank.getFluidAmount() >= MAX_GAS;
    }

    // ------------------------------
    // SAVE / LOAD
    // ------------------------------
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("Tank", fluidTank.writeToNBT(provider, new CompoundTag()));
        tag.put("Items", items.serializeNBT(provider));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        fluidTank.readFromNBT(provider, tag.getCompound("Tank"));
        items.deserializeNBT(provider, tag.getCompound("Items"));
    }

    // ------------------------------
    // MENU
    // ------------------------------
    @Override
    public Component getDisplayName() {
        return Component.literal("Gas Charge Pad");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new GasChargePadMenu(windowId, inv, this.getBlockPos());
    }
}
