package com.raven.arsimplemachines.blockentity;

import ARLib.ARLibRegistry;
import ARLib.blockentities.EntityFluidInputBlock;
import ARLib.multiblockCore.EntityMultiblockMachineMaster;
import ARLib.multiblockCore.BlockMultiblockMaster;
import com.raven.arsimplemachines.recipe.CategoryInput;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import com.raven.arsimplemachines.registry.ModBlocks;
import com.raven.arsimplemachines.registry.ModRecipeTypes;
import com.raven.arsimplemachines.recipe.MachineRecipeInput;
import com.raven.arsimplemachines.recipe.MachineRecipeMatcher;
import com.raven.arsimplemachines.recipe.roller.RollingRecipe;

import com.raven.arsimplemachines.util.CategoryRegistry;
import com.raven.arsimplemachines.util.FullStackItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import ARLib.blockentities.EntityItemInputBlock;
import ARLib.blockentities.EntityItemOutputBlock;
import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import com.raven.arsimplemachines.menu.RollingMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RollingControllerBlockEntity extends EntityMultiblockMachineMaster implements INetworkTagReceiver, MenuProvider {

    public static class RenderData {
        public float rollerSpinClient = 0f;
        public float rollerSpin = 0f;
        public float pressOffset = 0f;
        public float ingotOffset = 0f;
        public float plateOffset = 0f;
        public boolean running = false;
    }

    public RenderData renderData = new RenderData();
    public RollingRecipe currentRecipe;

    public boolean recipeRunning = false;
    private int recipeProgress = 0;
    private int recipeMaxProgress = 0;
    private int clientEnergyStored = 0;
    private int clientEnergyMax = 0;
    private int clientFluidAmount = 0;
    private int clientFluidCapacity = 0;
    public int internalFluidAmount = 0;
    public int internalFluidCapacity = 0;

    public RollingControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROLLING_CONTROLLER.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Rolling Machine");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new RollingMenu(windowId, inv, this.getBlockPos());
    }

    @Override
    public Object[][][] getStructure() {
        return new Object[][][]{
                {
                        {'C', null, null, null},
                        {'I', 'S', 'S', null},
                        {'E', 'S', 'S', null}
                },
                {
                        {'F', 'R', 'R', null},
                        {null, 'X', 'X', 'S'},
                        {null, 'S', 'S', 'O'}
                }
        };
    }

    public static final Map<Character, List<Block>> MAPPING = Map.of(
            'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
            'F', List.of(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get()),
            'S', List.of(ARLibRegistry.BLOCK_STRUCTURE.get()),
            'I', List.of(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get()),
            'O', List.of(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get()),
            'X', List.of(ARLibRegistry.BLOCK_COIL_COPPER.get()),
            'R', List.of(ARLibRegistry.BLOCK_MOTOR.get()),
            'C', List.of(ModBlocks.ROLLING_CONTROLLER.get())
    );

    @Override
    public HashMap<Character, List<Block>> getCharMapping() {
        return new HashMap<>(MAPPING);
    }

    @Override
    public Vec3i getControllerOffset(Object[][][] structure) {
        for (int y = 0; y < structure.length; y++)
            for (int z = 0; z < structure[y].length; z++)
                for (int x = 0; x < structure[y][z].length; x++)
                    if (structure[y][z][x] instanceof Character ch && (ch == 'c' || ch == 'C'))
                        return new Vec3i(x, y, z);

        return new Vec3i(structure[0][0].length / 2, structure.length / 2, structure[0].length / 2);
    }

    @Override
    public void onStructureComplete() {
        renderData.running = false;
        sendUpdatePacket(null);
    }

    @Override
    public void onStructureInvalid() {
        recipeRunning = false;
        renderData.running = false;
        sendUpdatePacket(null);
    }

    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 2,
                worldPosition.getY() - 1,
                worldPosition.getZ() - 2,
                worldPosition.getX() + 3,
                worldPosition.getY() + 3,
                worldPosition.getZ() + 3
        );
    }

    public boolean shouldRenderOffScreen() {
        return true;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        syncFluidEarly();

        if (!getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) {
            resetMachineState();
            return;
        }

        syncEnergy();

        // ---------------------------------------------------------
        // START RECIPE ONLY IF NOT RUNNING
        // ---------------------------------------------------------
        if (!recipeRunning) {

            // Only attempt to start a recipe when no recipe is active
            if (currentRecipe == null) {
                tryStartRecipe();
            }

            // If still not running, exit
            if (!recipeRunning || currentRecipe == null) {
                return;
            }
        }

        // ---------------------------------------------------------
        // DO NOT CHECK INPUTS AGAIN DURING RUNNING
        // ---------------------------------------------------------

        IEnergyStorage storage = getEnergyStorage();
        if (storage == null) {
            resetMachineState();
            return;
        }

        int energyPerTick = currentRecipe.getEnergyPerTick();
        if (storage.getEnergyStored() < energyPerTick) {
            sendUpdatePacket(null);
            return;
        }

        storage.extractEnergy(energyPerTick, false);
        recipeProgress++;
        sendUpdatePacket(null);

        if (recipeProgress >= recipeMaxProgress) {
            finishRecipe();
        }

        syncFluidLate();
    }


    private void syncFluidEarly() {
        BlockPos fluidPos = findSpecificBlock(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get());
        if (fluidPos == null) return;

        BlockEntity fluidBE = level.getBlockEntity(fluidPos);
        if (!(fluidBE instanceof EntityFluidInputBlock input)) return;

        int amount = 0;
        int capacity = 0;

        // FluidTank implements IFluidHandler
        for (int t = 0; t < input.myTank.getTanks(); t++) {
            var fs = input.myTank.getFluidInTank(t);
            if (!fs.isEmpty()) {
                amount = fs.getAmount();
                capacity = input.myTank.getTankCapacity(t);
                break;
            }
        }

        // fallback if empty but tank exists
        if (capacity == 0 && input.myTank.getTanks() > 0) {
            capacity = input.myTank.getTankCapacity(0);
        }

        clientFluidAmount = amount;
        clientFluidCapacity = capacity;
        internalFluidAmount = amount;
        internalFluidCapacity = capacity;

        sendUpdatePacket(null);
    }


    private void syncFluidLate() {
        syncFluidEarly();
    }

    private void syncEnergy() {
        IEnergyStorage storage = getEnergyStorage();
        if (storage == null) return;

        clientEnergyStored = storage.getEnergyStored();
        clientEnergyMax = storage.getMaxEnergyStored();
        sendUpdatePacket(null);
    }

    private void resetMachineState() {
        recipeRunning = false;
        currentRecipe = null;
        recipeProgress = 0;
        recipeMaxProgress = 0;
        renderData.running = false;
        sendUpdatePacket(null);
    }

    private IEnergyStorage getEnergyStorage() {
        BlockPos energyPos = findSpecificBlock(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get());
        if (energyPos == null) return null;

        BlockEntity be = level.getBlockEntity(energyPos);
        if (be == null) return null;

        return level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                energyPos,
                level.getBlockState(energyPos),
                be,
                null
        );
    }
    private boolean hasRequiredItems(RollingRecipe recipe, List<IItemHandler> handlers) {
        System.out.println("=== Rolling Machine Debug ===");
        System.out.println("Checking required items for recipe: " + recipe.getId());

        // ---------------------------------------------------------
        // 1. DIRECT ITEM INPUTS
        // ---------------------------------------------------------
        for (ItemStack req : recipe.getItemInputs()) {
            int needed = req.getCount();
            int found = 0;
            System.out.println("Direct item required: " + req + " needed=" + needed);

            for (IItemHandler handler : handlers) {
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    System.out.println("  Checking handler " + handler + " slot " + slot + ": " + stack);

                    if (stack.isEmpty()) continue;

                    if (stack.is(req.getItem())) {
                        found += stack.getCount();
                        System.out.println("    MATCH direct item: +" + stack.getCount() + " found=" + found);
                        if (found >= needed) break;
                    }
                }
                if (found >= needed) break;
            }

            if (found < needed) {
                System.out.println("FAILED direct item requirement: " + req);
                return false;
            }
        }

        // ---------------------------------------------------------
        // 2. CATEGORY INPUTS (count-based)
        // ---------------------------------------------------------
        Map<IItemHandler, Map<Integer, Integer>> usedCounts = new HashMap<>();

        for (CategoryInput cat : recipe.getItemCategories()) {

            String category = cat.category;
            int needed = cat.count;
            int matched = 0;

            System.out.println("Category required: " + category + " x" + needed);

            for (IItemHandler handler : handlers) {

                Map<Integer, Integer> used = usedCounts.computeIfAbsent(handler, h -> new HashMap<>());

                for (int slot = 0; slot < handler.getSlots(); slot++) {

                    ItemStack stack = handler.getStackInSlot(slot);
                    System.out.println("  Checking handler " + handler + " slot " + slot + ": " + stack);

                    if (stack.isEmpty()) continue;

                    if (!CategoryRegistry.matches(category, stack)) {
                        System.out.println("    NOT MATCH category " + category);
                        continue;
                    }

                    int alreadyUsed = used.getOrDefault(slot, 0);
                    int available = stack.getCount() - alreadyUsed;

                    System.out.println("    Slot " + slot + " has " + stack.getCount() +
                            " items, already used=" + alreadyUsed +
                            " available=" + available);

                    if (available > 0) {
                        int take = Math.min(available, needed - matched);
                        matched += take;
                        used.put(slot, alreadyUsed + take);

                        System.out.println("    MATCHED category " + category + " using " + take + " items from slot " + slot);

                        if (matched >= needed) break;
                    }
                }

                if (matched >= needed) break;
            }

            if (matched < needed) {
                System.out.println("FAILED to find category: " + category);
                return false;
            }
        }

        System.out.println("All required items found!");
        return true;
    }

    private void consumeRequiredItems(RollingRecipe recipe, List<IItemHandler> handlers) {
        System.out.println("=== Rolling Machine Debug: Consuming Items ===");
        System.out.println("Consuming items for recipe: " + recipe.getId());

        // ---------------------------------------------------------
        // 1. DIRECT ITEM INPUTS
        // ---------------------------------------------------------
        for (ItemStack req : recipe.getItemInputs()) {
            int needed = req.getCount();
            System.out.println("Direct item consumption: " + req + " needed=" + needed);

            for (IItemHandler handler : handlers) {
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    System.out.println("  Checking handler " + handler + " slot " + slot + ": " + stack);

                    if (stack.isEmpty()) continue;

                    if (stack.is(req.getItem())) {
                        int take = Math.min(stack.getCount(), needed);
                        System.out.println("    CONSUME direct item: take=" + take + " from slot " + slot);
                        handler.extractItem(slot, take, false);
                        needed -= take;

                        if (needed <= 0) break;
                    }
                }
                if (needed <= 0) break;
            }

            if (needed > 0) {
                System.out.println("FAILED to consume direct item: " + req);
            }
        }

        // ---------------------------------------------------------
        // 2. CATEGORY INPUTS (count-based)
        // ---------------------------------------------------------
        Map<IItemHandler, Map<Integer, Integer>> usedCounts = new HashMap<>();

        for (CategoryInput cat : recipe.getItemCategories()) {

            String category = cat.category;
            int needed = cat.count;
            int consumed = 0;

            System.out.println("Category consumption: " + category + " x" + needed);

            for (IItemHandler handler : handlers) {

                Map<Integer, Integer> used = usedCounts.computeIfAbsent(handler, h -> new HashMap<>());

                for (int slot = 0; slot < handler.getSlots(); slot++) {

                    ItemStack stack = handler.getStackInSlot(slot);
                    System.out.println("  Checking handler " + handler + " slot " + slot + ": " + stack);

                    if (stack.isEmpty()) continue;

                    if (!CategoryRegistry.matches(category, stack)) {
                        System.out.println("    NOT MATCH category " + category);
                        continue;
                    }

                    int alreadyUsed = used.getOrDefault(slot, 0);
                    int available = stack.getCount() - alreadyUsed;

                    System.out.println("    Slot " + slot + " has " + stack.getCount() +
                            " items, already used=" + alreadyUsed +
                            " available=" + available);

                    if (available > 0) {
                        int take = Math.min(available, needed - consumed);

                        handler.extractItem(slot, take, false);
                        used.put(slot, alreadyUsed + take);

                        consumed += take;

                        System.out.println("    CONSUMED " + take + " items for category " + category + " from slot " + slot);

                        if (consumed >= needed) break;
                    }
                }

                if (consumed >= needed) break;
            }

            if (consumed < needed) {
                System.out.println("FAILED to consume category: " + category);
            }
        }

        System.out.println("Finished consuming items.");
    }


    private boolean hasRequiredFluids(RollingRecipe recipe, List<IFluidHandler> handlers) {
        for (FluidStack req : recipe.getFluidInputs()) {
            int needed = req.getAmount();
            int found = 0;

            for (IFluidHandler handler : handlers) {
                for (int t = 0; t < handler.getTanks(); t++) {
                    FluidStack fs = handler.getFluidInTank(t);
                    if (!fs.isEmpty() && fs.isFluidEqual(req)) {
                        found += fs.getAmount();
                        if (found >= needed) break;
                    }
                }
                if (found >= needed) break;
            }

            if (found < needed) return false;
        }

        return true;
    }

    private void consumeRequiredFluids(RollingRecipe recipe, List<IFluidHandler> handlers) {
        for (FluidStack req : recipe.getFluidInputs()) {
            int needed = req.getAmount();

            for (IFluidHandler handler : handlers) {
                for (int t = 0; t < handler.getTanks(); t++) {
                    FluidStack fs = handler.getFluidInTank(t);
                    if (!fs.isEmpty() && fs.isFluidEqual(req)) {
                        int take = Math.min(fs.getAmount(), needed);
                        handler.drain(new FluidStack(fs.getFluid(), take), IFluidHandler.FluidAction.EXECUTE);
                        needed -= take;
                        if (needed <= 0) break;
                    }
                }
                if (needed <= 0) break;
            }
        }
    }



    private void tryStartRecipe() {

        // Gather ALL item input blocks
        List<IItemHandler> itemInputs = findAllItemInputs();
        if (itemInputs.isEmpty()) return;

        // Gather ALL fluid input blocks
        List<IFluidHandler> fluidInputs = findAllFluidInputs();
        if (fluidInputs.isEmpty()) return;

        IEnergyStorage storage = getEnergyStorage();
        if (storage == null) return;

        // Build unified recipe input
        MachineRecipeInput recipeInput = new MachineRecipeInput();

        // Add ALL item stacks
        for (IItemHandler handler : itemInputs) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    recipeInput.addItem(stack);
                }
            }
        }

        // Add ALL fluids
        for (IFluidHandler fluidHandler : fluidInputs) {
            for (int t = 0; t < fluidHandler.getTanks(); t++) {
                FluidStack fs = fluidHandler.getFluidInTank(t);
                if (!fs.isEmpty()) {
                    recipeInput.addFluid(fs);
                }
            }
        }

        // Find matching recipe
        RollingRecipe recipe = MachineRecipeMatcher.findMatch(
                level,
                ModRecipeTypes.ROLLING_TYPE,
                recipeInput
        );

        if (recipe == null) return;

        // Allow category-only OR item-only OR mixed recipes
        if (recipe.getItemInputs().isEmpty() && recipe.getItemCategories().isEmpty()) return;

        // Check energy
        if (storage.getEnergyStored() < recipe.getEnergyPerTick()) return;

        // Check fluid requirements
        if (!hasRequiredFluids(recipe, fluidInputs)) return;

        // Check item + category requirements
        if (!hasRequiredItems(recipe, itemInputs)) return;

        // Consume fluids
        consumeRequiredFluids(recipe, fluidInputs);

        // Start recipe
        currentRecipe = recipe;
        recipeRunning = true;
        recipeProgress = 0;
        recipeMaxProgress = recipe.getProcessingTime();
        renderData.running = true;

        // Consume items + categories ONCE
        consumeRequiredItems(recipe, itemInputs);

        sendUpdatePacket(null);
    }


    private void finishRecipe() {
        RollingRecipe finished = currentRecipe;

        recipeRunning = false;
        renderData.running = false;

        if (finished != null) {

            // ---------------------------------------------------------
            // ITEM OUTPUTS
            // ---------------------------------------------------------
            List<IItemHandler> itemOutputs = findAllItemOutputs();
            for (ItemStack out : finished.getItemOutputs()) {
                ItemStack remainder = out.copy();

                for (IItemHandler handler : itemOutputs) {
                    for (int slot = 0; slot < handler.getSlots(); slot++) {
                        remainder = handler.insertItem(slot, remainder, false);
                        if (remainder.isEmpty()) break;
                    }
                    if (remainder.isEmpty()) break;
                }
            }

            // ---------------------------------------------------------
            // FLUID OUTPUTS
            // ---------------------------------------------------------
            List<IFluidHandler> fluidOutputs = findAllFluidOutputs();
            for (FluidStack fs : finished.getFluidOutputs()) {
                int remaining = fs.getAmount();

                for (IFluidHandler handler : fluidOutputs) {
                    remaining -= handler.fill(
                            new FluidStack(fs.getFluid(), remaining),
                            IFluidHandler.FluidAction.EXECUTE
                    );
                    if (remaining <= 0) break;
                }
            }
        }

        currentRecipe = null;
        recipeProgress = 0;
        recipeMaxProgress = 0;

        sendUpdatePacket(null);
    }


    public List<IItemHandler> findAllItemInputs() {
        List<IItemHandler> list = new ArrayList<>();

        for (BlockPos pos : findAllBlocks(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get())) {
            BlockEntity be = level.getBlockEntity(pos);

            IItemHandler cap = level.getCapability(
                    Capabilities.ItemHandler.BLOCK,
                    pos,
                    level.getBlockState(pos),
                    be,
                    null
            );

            if (cap != null) {
                list.add(cap);
            }
        }

        return list;
    }


    private List<IItemHandler> findAllItemOutputs() {
        List<IItemHandler> list = new java.util.ArrayList<>();
        for (BlockPos pos : findAllBlocks(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EntityItemOutputBlock out)
                list.add(out.inventory);
        }
        return list;
    }

    public List<IFluidHandler> findAllFluidInputs() {
        List<IFluidHandler> list = new ArrayList<>();
        for (BlockPos pos : findAllBlocks(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EntityFluidInputBlock input) {
                list.add(input.myTank); // FluidTank implements IFluidHandler
            }
        }
        return list;
    }


    private List<IFluidHandler> findAllFluidOutputs() {
        List<IFluidHandler> list = new java.util.ArrayList<>();
        for (BlockPos pos : findAllBlocks(ARLibRegistry.BLOCK_FLUID_OUTPUT_BLOCK.get())) {
            BlockEntity be = level.getBlockEntity(pos);
            var cap = level.getCapability(
                    Capabilities.FluidHandler.BLOCK,
                    pos,
                    level.getBlockState(pos),
                    be,
                    null
            );
            if (cap != null) list.add(cap);
        }
        return list;
    }

    private List<BlockPos> findAllBlocks(Block blockType) {
        List<BlockPos> list = new java.util.ArrayList<>();
        for (int dx = -4; dx <= 4; dx++)
            for (int dy = -2; dy <= 2; dy++)
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    if (level.getBlockState(p).getBlock() == blockType)
                        list.add(p);
                }
        return list;
    }

    private BlockPos findSpecificBlock(Block blockType) {
        for (int dx = -4; dx <= 4; dx++)
            for (int dy = -2; dy <= 2; dy++)
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    if (level.getBlockState(p).getBlock() == blockType)
                        return p;
                }
        return null;
    }

    public void clientTick() {
        if (level == null || !level.isClientSide) return;

        if (renderData.running) {
            renderData.rollerSpinClient = (renderData.rollerSpinClient + 4f) % 360f;
            renderData.pressOffset = (float) Math.sin(level.getGameTime() * 0.08f) * 0.20f;
            renderData.ingotOffset += 0.03f;

            boolean ingotEnteredRollers = renderData.ingotOffset > 1.0f;

            if (ingotEnteredRollers && renderData.plateOffset == 0f) {
                renderData.plateOffset = 0.001f;
            }

            if (renderData.ingotOffset > 1.6f) {
                renderData.ingotOffset = 0f;
            }

            if (renderData.plateOffset > 0f) {
                renderData.plateOffset += 0.03f;
                if (renderData.plateOffset > 1.0f) {
                    renderData.plateOffset = 0f;
                }
            }

        } else {
            renderData.rollerSpin = 0f;
            renderData.pressOffset = 0f;
            renderData.ingotOffset = 0f;
            renderData.plateOffset = 0f;
        }
    }

    public int getRecipeProgress() {
        return recipeProgress;
    }

    public int getRecipeMaxProgress() {
        return recipeMaxProgress;
    }

    public IItemHandler getInputHandler() {
        BlockPos inputPos = findSpecificBlock(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get());
        if (inputPos == null) return null;

        BlockEntity be = level.getBlockEntity(inputPos);
        if (be instanceof EntityItemInputBlock input)
            return input.inventory;

        return null;
    }

    public IItemHandler getOutputHandler() {
        BlockPos outputPos = findSpecificBlock(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get());
        if (outputPos == null) return null;

        BlockEntity be = level.getBlockEntity(outputPos);
        if (be instanceof EntityItemOutputBlock out)
            return out.inventory;

        return null;
    }

    public int getClientEnergyStored() {
        return clientEnergyStored;
    }

    public int getClientEnergyMax() {
        return clientEnergyMax;
    }

    public int getClientFluidAmount() { return clientFluidAmount; }
    public int getClientFluidCapacity() { return clientFluidCapacity; }

    public void sendUpdatePacket(ServerPlayer specificPlayer) {
        if (level == null || level.isClientSide) return;

        CompoundTag tag = new CompoundTag();
        tag.putBoolean("running", renderData.running);
        tag.putBoolean("recipeRunning", recipeRunning);

        tag.putFloat("pressOffset", renderData.pressOffset);
        tag.putInt("energyStored", clientEnergyStored);
        tag.putInt("energyMax", clientEnergyMax);
        tag.putInt("recipeProgress", recipeProgress);
        tag.putInt("recipeMaxProgress", recipeMaxProgress);
        tag.putInt("fluidAmount", clientFluidAmount);
        tag.putInt("fluidCapacity", clientFluidCapacity);
        tag.putInt("internalFluidAmount", internalFluidAmount);
        tag.putInt("internalFluidCapacity", internalFluidCapacity);

        PacketBlockEntity packet = PacketBlockEntity.getBlockEntityPacket(this, tag);

        if (specificPlayer != null)
            PacketDistributor.sendToPlayer(specificPlayer, packet);
        else
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(worldPosition), packet);
    }

    @Override
    public void readClient(CompoundTag tag) {
        if (tag.contains("running")) renderData.running = tag.getBoolean("running");
        if (tag.contains("recipeRunning")) recipeRunning = tag.getBoolean("recipeRunning");

        if (tag.contains("pressOffset")) renderData.pressOffset = tag.getFloat("pressOffset");
        if (tag.contains("energyStored")) clientEnergyStored = tag.getInt("energyStored");
        if (tag.contains("energyMax")) clientEnergyMax = tag.getInt("energyMax");
        if (tag.contains("recipeProgress")) recipeProgress = tag.getInt("recipeProgress");
        if (tag.contains("recipeMaxProgress")) recipeMaxProgress = tag.getInt("recipeMaxProgress");
        if (tag.contains("fluidAmount")) clientFluidAmount = tag.getInt("fluidAmount");
        if (tag.contains("fluidCapacity")) clientFluidCapacity = tag.getInt("fluidCapacity");
        if (tag.contains("internalFluidAmount")) internalFluidAmount = tag.getInt("internalFluidAmount");
        if (tag.contains("internalFluidCapacity")) internalFluidCapacity = tag.getInt("internalFluidCapacity");
    }

    @Override
    public void readServer(CompoundTag tag, ServerPlayer sender) {
        // No server-side direct tag handling needed
    }
}
