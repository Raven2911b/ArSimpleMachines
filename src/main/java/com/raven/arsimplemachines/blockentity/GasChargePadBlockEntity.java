package com.raven.arsimplemachines.blockentity;

import com.raven.arsimplemachines.menu.GasChargePadMenu;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import com.raven.arsimplemachines.util.SuitData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import java.util.List;
import net.neoforged.neoforge.capabilities.Capabilities;


public class GasChargePadBlockEntity extends BlockEntity implements MenuProvider {

    private static final int MAX_GAS = 10000;
   //private static final Logger LOGGER = LogUtils.getLogger();
   private final IFluidHandler fluidHandler = new IFluidHandler() {

       @Override
       public int getTanks() {
           return 1;
       }

       @Override
       public FluidStack getFluidInTank(int tank) {
           if (gasAmount <= 0 || gasType.isEmpty()) return FluidStack.EMPTY;

           Fluid fluid = level.registryAccess()
                   .registryOrThrow(Registries.FLUID)
                   .get(ResourceLocation.parse("adv_rocketry:" + gasType));

           return new FluidStack(fluid, gasAmount);
       }

       @Override
       public int getTankCapacity(int tank) {
           return MAX_GAS; // or whatever your pad max is
       }

       @Override
       public boolean isFluidValid(int tank, FluidStack stack) {
           String id = stack.getFluid().builtInRegistryHolder().key().location().getPath();
           return id.contains("oxygen") || id.contains("hydrogen") || id.contains("nitrogen");
       }


       @Override
       public int fill(FluidStack resource, FluidAction action) {
           if (resource.isEmpty()) return 0;

           String incoming = resource.getFluid().builtInRegistryHolder().key().location().getPath();

           if (!(incoming.contains("oxygen") || incoming.contains("hydrogen") || incoming.contains("nitrogen")))
               return 0;

           if (gasAmount == 0) {
               gasType = incoming; // or normalize to "oxygen"/"hydrogen"/"nitrogen"
           } else if (!gasType.equals(incoming)) {
               return 0;
           }

           int space = MAX_GAS - gasAmount;
           int toAdd = Math.min(space, resource.getAmount());

           if (action.execute()) {
               gasAmount += toAdd;
               setChanged();
           }

           return toAdd;
       }

       @Override
       public FluidStack drain(FluidStack resource, FluidAction action) {
           return FluidStack.EMPTY; // no extraction
       }

       @Override
       public FluidStack drain(int maxDrain, FluidAction action) {
           return FluidStack.EMPTY; // no extraction
       }
   };

    // "", "oxygen", "hydrogen", "nitrogen"
    private String gasType = "";
    private int gasAmount = 0;

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

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }


    // ------------------------------
    // TICK LOGIC
    // ------------------------------
    public static void tick(Level level, BlockPos pos, BlockState state, GasChargePadBlockEntity be) {
        if (level.isClientSide) return;

       // LOGGER.info("[PAD] Tick start at {}", pos);

        // ------------------------------
        // BUCKET → GAS LOGIC
        // ------------------------------
        ItemStack input = be.items.getStackInSlot(0);
        if (!input.isEmpty()) {

            String itemId = input.getItem().builtInRegistryHolder().key().location().toString();
         //   LOGGER.info("[PAD] Input slot contains {}", itemId);

            String incomingGas = "";
            if (itemId.contains("oxygen_bucket")) incomingGas = "oxygen";

            if (!incomingGas.isEmpty()) {

             //   LOGGER.info("[PAD] Incoming gas detected: {}", incomingGas);

                if (be.gasAmount == 0) {
                    be.gasType = incomingGas;
                 //   LOGGER.info("[PAD] Gas type set to {}", incomingGas);
                } else if (!be.gasType.equals(incomingGas)) {
                 //   LOGGER.warn("[PAD] Gas type mismatch: pad={}, incoming={}", be.gasType, incomingGas);
                    return;
                }

                int added = be.addGas(1000);
             //   LOGGER.info("[PAD] Added {} gas, new amount={}", added, be.gasAmount);

                if (added > 0) {
                    input.shrink(1);
                //    LOGGER.info("[PAD] Consumed bucket");

                    ItemStack out = be.items.getStackInSlot(1);
                    if (out.isEmpty()) {
                        be.items.setStackInSlot(1, new ItemStack(Items.BUCKET));
                    } else if (out.getItem() == Items.BUCKET && out.getCount() < out.getMaxStackSize()) {
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
          //  LOGGER.info("[PAD] No players on pad");
            return;
        }

        Player player = players.get(0);
     //   LOGGER.info("[PAD] Player {} detected on pad", player.getName().getString());

        // ------------------------------
        // SUIT DETECTION
        // ------------------------------
        ItemStack suit = player.getInventory().armor.get(2);
        if (suit.isEmpty()) {
        //    LOGGER.warn("[PAD] Player has no chestplate");
            return;
        }

        String suitId = suit.getItem().builtInRegistryHolder().key().location().toString();
      //  LOGGER.info("[PAD] Player chestplate: {}", suitId);

        if (!suitId.equals("adv_rocketry:space_chestplate")) {
        //    LOGGER.warn("[PAD] Chestplate is not a space suit");
            return;
        }

        // ------------------------------
        // LOAD SUIT DATA (minecraft:custom_data)
        // ------------------------------
        CompoundTag custom = SuitData.get(suit);
        HolderLookup.Provider provider = level.registryAccess();

        // Load C tag (oxygen + tank count)
        CompoundTag cTag = custom.getCompound("C");
        int oxygen = cTag.getInt("oxygen");
        int pressureTanks = cTag.getInt("pressureTanks");

      //  LOGGER.info("[PAD] Suit oxygen={}, tanks={}", oxygen, pressureTanks);

        // ------------------------------
        // LOAD INTERNAL INVENTORY
        // ------------------------------
        ItemStackHandler inv = new ItemStackHandler(2);

        if (custom.contains("inventory")) {
            inv.deserializeNBT(provider, custom.getCompound("inventory"));
       //     LOGGER.info("[PAD] Suit inventory loaded");
        } else {
        //    LOGGER.warn("[PAD] Suit inventory missing!");
        }

        // ------------------------------
        // COUNT TANKS
        // ------------------------------
        int countedTanks = 0;

        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack tank = inv.getStackInSlot(i);
            if (tank.isEmpty()) continue;

            String tankId = tank.getItem().builtInRegistryHolder().key().location().toString();
         //   LOGGER.info("[PAD] Slot {} contains {}", i, tankId);

            if (tankId.contains("portable_pressure_tank")) {
                countedTanks++;
            }
        }

       // LOGGER.info("[PAD] Counted {} tanks", countedTanks);

        // ------------------------------
        // FILL SUIT OXYGEN
        // ------------------------------

        int maxOxygen = countedTanks * 4000;
        int space = maxOxygen - oxygen;

        if (space > 0) {
            int transfer = Math.min(50, be.gasAmount);
            int added = Math.min(transfer, space);

            oxygen += added;
            be.gasAmount -= added;
            be.setChanged();

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
        return gasAmount;
    }

    public int getMaxGas() {
        return MAX_GAS;
    }

    public String getGasType() {
        return gasType;
    }

    public boolean isEmpty() {
        return gasAmount <= 0;
    }

    public boolean isFull() {
        return gasAmount >= MAX_GAS;
    }

    public int addGas(int amount) {
        if (amount <= 0) return 0;

        int space = MAX_GAS - gasAmount;
        int accepted = Math.min(space, amount);
        gasAmount += accepted;

        setChanged();
        return accepted;
    }

    public int removeGas(int amount) {
        if (amount <= 0 || gasAmount <= 0) return 0;

        int removed = Math.min(gasAmount, amount);
        gasAmount -= removed;

        if (gasAmount <= 0) {
            gasAmount = 0;
            gasType = "";
        }

        setChanged();
        return removed;
    }

    // ------------------------------
    // SAVE / LOAD
    // ------------------------------
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("GasAmount", gasAmount);
        tag.putString("GasType", gasType);
        tag.put("Items", items.serializeNBT(provider));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        gasAmount = tag.getInt("GasAmount");
        gasType = tag.getString("GasType");
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
