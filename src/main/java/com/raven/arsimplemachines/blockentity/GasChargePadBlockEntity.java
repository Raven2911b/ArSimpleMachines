package com.raven.arsimplemachines.blockentity;

import advRocketry.Utils.ItemUtils;
import com.raven.arsimplemachines.menu.GasChargePadMenu;
import com.raven.arsimplemachines.recipe.gaspad.GasChargeRecipe;
import com.raven.arsimplemachines.recipe.gaspad.GasChargeRecipeInput;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import com.raven.arsimplemachines.registry.ModRecipeTypes;

import advRocketry.SpaceSuit.ISpaceSuitInventory;
import advRocketry.Items.ItemPortablePressureTank;
import advRocketry.Registry.Fluids;


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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
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
        public boolean isFluidValid(FluidStack stack) {
            String id = stack.getFluid().builtInRegistryHolder().key().location().toString();
            return id.contains("oxygen") || id.contains("hydrogen") || id.contains("nitrogen");
        }


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
                .map(RecipeHolder::value)
                .orElse(null);
    }
    private boolean hasRecipeFor(FluidStack stack) {
        if (level == null || stack.isEmpty()) return false;

        // Ignore amount — only check fluid type
        GasChargeRecipeInput input =
                new GasChargeRecipeInput(stack.getFluid(), Integer.MAX_VALUE);

        return level.getRecipeManager()
                .getRecipeFor(ModRecipeTypes.GAS_CHARGE_TYPE.get(), input, level)
                .isPresent();
    }
    // ------------------------------
// TICK LOGIC
// ------------------------------
    public static void tick(Level level, BlockPos pos, BlockState state, GasChargePadBlockEntity be) {
        if (level.isClientSide) return;

        // ---------------------------------------------------------
        // BUCKET → TANK (fill tank from gas bucket)
        // ---------------------------------------------------------
        ItemStack input = be.items.getStackInSlot(0);

        if (!input.isEmpty()) {
            var optionalFluid = net.neoforged.neoforge.fluids.FluidUtil.getFluidContained(input);

            if (optionalFluid.isPresent()) {
                FluidStack contained = optionalFluid.get();

                int filled = be.fluidTank.fill(contained,
                        net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);

                if (filled > 0) {
                    be.items.setStackInSlot(0, ItemStack.EMPTY);

                    ItemStack empty = new ItemStack(Items.BUCKET);
                    ItemStack out = be.items.getStackInSlot(1);

                    if (out.isEmpty()) {
                        be.items.setStackInSlot(1, empty);
                    } else if (ItemStack.isSameItemSameComponents(out, empty)
                            && out.getCount() < out.getMaxStackSize()) {
                        out.grow(1);
                    }

                    be.setChanged();
                }
            }
        }

        // ---------------------------------------------------------
        // TANK → BUCKET (fill empty bucket from tank)
        // ---------------------------------------------------------
        if (!input.isEmpty() && input.getItem() == Items.BUCKET) {
            FluidStack tankFluid = be.fluidTank.getFluid();

            if (!tankFluid.isEmpty() && tankFluid.getAmount() >= 1000) {

                ItemStack filledBucket = net.neoforged.neoforge.fluids.FluidUtil.getFilledBucket(tankFluid);

                if (!filledBucket.isEmpty()) {
                    be.fluidTank.drain(1000,
                            net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);

                    be.items.setStackInSlot(0, ItemStack.EMPTY);

                    ItemStack out = be.items.getStackInSlot(1);

                    if (out.isEmpty()) {
                        be.items.setStackInSlot(1, filledBucket);
                    } else if (ItemStack.isSameItemSameComponents(out, filledBucket)
                            && out.getCount() < out.getMaxStackSize()) {
                        out.grow(1);
                    }

                    be.setChanged();
                }
            }
        }

        // ---------------------------------------------------------
        // PLAYER DETECTION — must be standing ON the pad
        // ---------------------------------------------------------
        AABB box = new AABB(
                pos.getX() + 0.1, pos.getY(), pos.getZ() + 0.1,
                pos.getX() + 0.9, pos.getY() + 1, pos.getZ() + 0.9
        );

        List<Player> players = level.getEntitiesOfClass(Player.class, box);
        if (players.isEmpty()) return;

        Player player = players.get(0);

        // ---------------------------------------------------------
        // SUIT DETECTION — must be wearing AdvRocketry chestplate
        // ---------------------------------------------------------
        ItemStack suit = player.getInventory().armor.get(2);
        if (suit.isEmpty()) return;

        String suitId = suit.getItem().builtInRegistryHolder().key().location().toString();
        if (!suitId.equals("adv_rocketry:space_chestplate")) return;

        HolderLookup.Provider provider = level.registryAccess();

        // ---------------------------------------------------------
        // LOAD SUIT INVENTORY (AR API)
        // ---------------------------------------------------------
        ItemStackHandler suitInv = advRocketry.SpaceSuit.ISpaceSuitInventory.loadInventory(suit, provider);
        if (suitInv == null) return;

        // Count oxygen tanks and total oxygen
        int pressureTanks = 0;
        int oxygen = 0;

        for (int i = 0; i < suitInv.getSlots(); i++) {
            ItemStack tank = suitInv.getStackInSlot(i);
            if (tank.isEmpty()) continue;

            if (tank.getItem() instanceof advRocketry.Items.ItemPortablePressureTank) {
                pressureTanks++;
                var handler = tank.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
                if (handler != null) {
                    FluidStack fluidInTank = handler.getFluidInTank(0);
                    if (fluidInTank.getFluid().equals(advRocketry.Registry.Fluids.OXYGEN.get())) {
                        oxygen += fluidInTank.getAmount();
                    }
                }
            }
        }

        // ---------------------------------------------------------
        // FILL SUIT OXYGEN FROM PAD (into tanks)
        // ---------------------------------------------------------
        if (!be.fluidTank.isEmpty()
                && be.fluidTank.getFluid().getFluid().equals(advRocketry.Registry.Fluids.OXYGEN.get())
                && pressureTanks > 0) {

            int maxOxygen = pressureTanks * 4000;
            int space = maxOxygen - oxygen;

            if (space > 0) {
                int transfer = Math.min(50, Math.min(space, be.fluidTank.getFluidAmount()));

                if (transfer > 0) {
                    int remainingToFill = transfer;

                    // distribute oxygen into tanks
                    for (int i = 0; i < suitInv.getSlots() && remainingToFill > 0; i++) {
                        ItemStack tank = suitInv.getStackInSlot(i);
                        if (tank.isEmpty()) continue;

                        if (!(tank.getItem() instanceof advRocketry.Items.ItemPortablePressureTank)) continue;

                        var handler = tank.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
                        if (handler == null) continue;

                        FluidStack contained = handler.getFluidInTank(0);

                        boolean isOxygenTank =
                                contained.isEmpty() ||
                                        contained.getFluid().equals(advRocketry.Registry.Fluids.OXYGEN.get());

                        if (!isOxygenTank) continue;

                        FluidStack toFill = new FluidStack(advRocketry.Registry.Fluids.OXYGEN.get(), remainingToFill);
                        int filled = handler.fill(toFill,
                                net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);

                        if (filled > 0) {
                            remainingToFill -= filled;
                            be.fluidTank.drain(filled,
                                    net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                            be.setChanged();
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------
        // SAVE SUIT INVENTORY + CACHED DATA (AR API)
        // ---------------------------------------------------------
        advRocketry.SpaceSuit.ISpaceSuitInventory.saveInventory(suitInv, suit, provider);

        // ---------------------------------------------------------
        // FIND JETPACK IN SUIT INVENTORY
        // ---------------------------------------------------------
        boolean hasJetpack = false;
        ItemStack jetpackStack = ItemStack.EMPTY;
        int jetpackSlotIndex = -1;

        for (int i = 0; i < suitInv.getSlots(); i++) {
            ItemStack stack = suitInv.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            String id = stack.getItem().builtInRegistryHolder().key().location().toString();
            if (id.equals("adv_rocketry:jetpack")) {
                hasJetpack = true;
                jetpackStack = stack;
                jetpackSlotIndex = i;
                break;
            }
        }

        // ---------------------------------------------------------
        // FILL JETPACK HYDROGEN (portable tanks inside jetpack inventory)
        // ---------------------------------------------------------
        if (hasJetpack && !be.fluidTank.isEmpty()
                && be.fluidTank.getFluid().getFluid().equals(advRocketry.Registry.Fluids.HYDROGEN.get())) {

            // Load jetpack inventory (NOT suit inventory!)
            ItemStackHandler jetInv = advRocketry.SpaceSuit.ISpaceSuitInventory.loadInventory(jetpackStack, provider);
            if (jetInv == null) return;

            int remaining = Math.min(50, be.fluidTank.getFluidAmount());
            if (remaining <= 0) return;

            for (int i = 0; i < jetInv.getSlots() && remaining > 0; i++) {
                ItemStack tank = jetInv.getStackInSlot(i);
                if (tank.isEmpty()) continue;

                if (!(tank.getItem() instanceof advRocketry.Items.ItemPortablePressureTank)) continue;

                var handler = tank.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
                if (handler == null) continue;

                FluidStack contained = handler.getFluidInTank(0);

                boolean isHydrogenTank =
                        contained.isEmpty() ||
                                contained.getFluid().equals(advRocketry.Registry.Fluids.HYDROGEN.get());

                if (!isHydrogenTank) continue;

                FluidStack toFill = new FluidStack(advRocketry.Registry.Fluids.HYDROGEN.get(), remaining);
                int filled = handler.fill(toFill,
                        net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);

                if (filled > 0) {
                    remaining -= filled;
                    be.fluidTank.drain(filled,
                            net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                    be.setChanged();
                }
            }

            // Save jetpack inventory back into the jetpack item
            advRocketry.SpaceSuit.ISpaceSuitInventory.saveInventory(jetInv, jetpackStack, provider);

            // Restore modified jetpack back into suit inventory
            suitInv.setStackInSlot(jetpackSlotIndex, jetpackStack);

            // Save suit inventory with updated jetpack
            advRocketry.SpaceSuit.ISpaceSuitInventory.saveInventory(suitInv, suit, provider);
        }
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
