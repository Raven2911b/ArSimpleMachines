package com.raven.arsimplemachines.blockentity;

import ARLib.ARLibRegistry;
import ARLib.blockentities.EntityFluidInputBlock;
import ARLib.multiblockCore.EntityMultiblockMachineMaster;
import ARLib.multiblockCore.BlockMultiblockMaster;
import com.mojang.serialization.DataResult;
import com.raven.arsimplemachines.recipe.CategoryInput;
import com.raven.arsimplemachines.registry.ModBlockEntities;
import com.raven.arsimplemachines.registry.ModBlocks;
import com.raven.arsimplemachines.registry.ModRecipeTypes;
import com.raven.arsimplemachines.recipe.MachineRecipeInput;
import com.raven.arsimplemachines.recipe.MachineRecipeMatcher;
import com.raven.arsimplemachines.recipe.crystallizer.CrystallizerRecipe;

import com.raven.arsimplemachines.util.CategoryRegistry;
import com.raven.arsimplemachines.util.FullStackItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
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

import com.raven.arsimplemachines.menu.CrystallizerMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrystallizerControllerBlockEntity extends EntityMultiblockMachineMaster implements INetworkTagReceiver, MenuProvider {

    public static class RenderData {
        public float fluidLevelClient = 0f;
        public boolean running = false;
        public float recipeProgressClient = 0f;
        public ItemStack outputItem = ItemStack.EMPTY;
    }

    public RenderData renderData = new RenderData();
    public CrystallizerRecipe currentRecipe;

    public boolean recipeRunning = false;
    private int recipeProgress = 0;
    private int recipeMaxProgress = 0;
    private int clientEnergyStored = 0;
    private int clientEnergyMax = 0;
    private int clientFluidAmount = 0;
    private int clientFluidCapacity = 0;
    public int internalFluidAmount = 0;
    public int internalFluidCapacity = 0;

    public CrystallizerControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTALLIZER_CONTROLLER.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Crystallizer");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new CrystallizerMenu(windowId, inv, this.getBlockPos());
    }

    @Override
    public Object[][][] getStructure() {
        // 3 wide, 2 deep, 2 high — placeholder, you’ll refine block types later
        return new Object[][][]{
                {
                        {'S', 'S', 'S'},
                        {'S', 'S', 'S'}
                },
                {
                        {'O', 'C', 'I'},
                        {'V', 'E', 'F'}
                }
        };
    }

    public static final Map<Character, List<Block>> MAPPING = Map.of(
            'E', List.of(ARLibRegistry.BLOCK_ENERGY_INPUT_BLOCK.get()),
            'F', List.of(ARLibRegistry.BLOCK_FLUID_INPUT_BLOCK.get()),
            'V', List.of(ARLibRegistry.BLOCK_FLUID_OUTPUT_BLOCK.get()),
            'S', List.of(ARLibRegistry.BLOCK_STRUCTURE.get()),
            'I', List.of(ARLibRegistry.BLOCK_ITEM_INPUT_BLOCK.get()),
            'O', List.of(ARLibRegistry.BLOCK_ITEM_OUTPUT_BLOCK.get()),
            'C', List.of(ModBlocks.CRYSTALLIZER_CONTROLLER.get())
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

    // Cache block positions once when multiblock forms
    private Map<Block, List<BlockPos>> blockCache = new HashMap<>();
    private boolean cacheValid = false;

    private IEnergyStorage cachedEnergyStorage = null;
    private BlockPos cachedEnergyPos = null;

    @Override
    public void onLoad() {
        super.onLoad();

        // Delay one tick so the multiblock can form first
        if (!level.isClientSide) {
            level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
        }
    }

    @Override
    public void onStructureComplete() {
        cachedEnergyStorage = null;
        cachedEnergyPos = null;
        rebuildBlockCache();
        renderData.running = false;
        sendUpdatePacket(null);
    }

    @Override
    public void onStructureInvalid() {
        recipeRunning = false;
        renderData.running = false;
        blockCache.clear();
        cacheValid = false;
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

        if (!recipeRunning) {
            if (currentRecipe == null) {
                tryStartRecipe();
            }
            if (!recipeRunning || currentRecipe == null) {
                return;
            }
        }

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

        for (int t = 0; t < input.myTank.getTanks(); t++) {
            var fs = input.myTank.getFluidInTank(t);
            if (!fs.isEmpty()) {
                amount = fs.getAmount();
                capacity = input.myTank.getTankCapacity(t);
                break;
            }
        }

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

        // Return cached if same position
        if (cachedEnergyPos != null && cachedEnergyPos.equals(energyPos)) {
            return cachedEnergyStorage;
        }

        if (energyPos == null) return null;

        BlockEntity be = level.getBlockEntity(energyPos);
        if (be == null) return null;

        IEnergyStorage storage = level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                energyPos,
                level.getBlockState(energyPos),
                be,
                null
        );

        cachedEnergyPos = energyPos;
        cachedEnergyStorage = storage;
        return storage;
    }

    private boolean hasRequiredItems(CrystallizerRecipe recipe, List<IItemHandler> handlers) {
        Map<IItemHandler, Map<Integer, Integer>> usedCounts = new HashMap<>();

        for (ItemStack req : recipe.getItemInputs()) {
            int needed = req.getCount();
            int found = 0;

            for (IItemHandler handler : handlers) {
                Map<Integer, Integer> used = usedCounts.computeIfAbsent(handler, h -> new HashMap<>());

                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    if (stack.isEmpty()) continue;

                    if (stack.is(req.getItem())) {
                        int alreadyUsed = used.getOrDefault(slot, 0);
                        int available = stack.getCount() - alreadyUsed;
                        if (available > 0) {
                            int take = Math.min(available, needed - found);
                            found += take;
                            used.put(slot, alreadyUsed + take);
                            if (found >= needed) break;
                        }
                    }
                }
                if (found >= needed) break;
            }

            if (found < needed) {
                return false;
            }
        }

        Map<IItemHandler, Map<Integer, Integer>> catUsedCounts = new HashMap<>();

        for (CategoryInput cat : recipe.getItemCategories()) {
            String category = cat.category;
            int needed = cat.count;
            int matched = 0;

            for (IItemHandler handler : handlers) {
                Map<Integer, Integer> used = catUsedCounts.computeIfAbsent(handler, h -> new HashMap<>());

                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    if (stack.isEmpty()) continue;

                    if (!CategoryRegistry.matches(category, stack)) continue;

                    int alreadyUsed = used.getOrDefault(slot, 0);
                    int available = stack.getCount() - alreadyUsed;
                    if (available > 0) {
                        int take = Math.min(available, needed - matched);
                        matched += take;
                        used.put(slot, alreadyUsed + take);
                        if (matched >= needed) break;
                    }
                }

                if (matched >= needed) break;
            }

            if (matched < needed) {
                return false;
            }
        }

        return true;
    }

    private void consumeRequiredItems(CrystallizerRecipe recipe, List<IItemHandler> handlers) {
        for (ItemStack req : recipe.getItemInputs()) {
            int needed = req.getCount();

            for (IItemHandler handler : handlers) {
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    if (stack.isEmpty()) continue;

                    if (stack.is(req.getItem())) {
                        int take = Math.min(stack.getCount(), needed);
                        handler.extractItem(slot, take, false);
                        needed -= take;
                        if (needed <= 0) break;
                    }
                }
                if (needed <= 0) break;
            }
        }

        Map<IItemHandler, Map<Integer, Integer>> usedCounts = new HashMap<>();

        for (CategoryInput cat : recipe.getItemCategories()) {
            String category = cat.category;
            int needed = cat.count;
            int consumed = 0;

            for (IItemHandler handler : handlers) {
                Map<Integer, Integer> used = usedCounts.computeIfAbsent(handler, h -> new HashMap<>());

                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    if (stack.isEmpty()) continue;

                    if (!CategoryRegistry.matches(category, stack)) continue;

                    int alreadyUsed = used.getOrDefault(slot, 0);
                    int available = stack.getCount() - alreadyUsed;
                    if (available > 0) {
                        int take = Math.min(available, needed - consumed);
                        handler.extractItem(slot, take, false);
                        used.put(slot, alreadyUsed + take);
                        consumed += take;
                        if (consumed >= needed) break;
                    }
                }

                if (consumed >= needed) break;
            }
        }
    }

    private boolean hasRequiredFluids(CrystallizerRecipe recipe, List<IFluidHandler> handlers) {
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

    private void consumeRequiredFluids(CrystallizerRecipe recipe, List<IFluidHandler> handlers) {
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
        List<IItemHandler> itemInputs = findAllItemInputs();
        if (itemInputs.isEmpty()) return;

        List<IFluidHandler> fluidInputs = findAllFluidInputs();
        if (fluidInputs.isEmpty()) return;

        IEnergyStorage storage = getEnergyStorage();
        if (storage == null) return;

        MachineRecipeInput recipeInput = new MachineRecipeInput();

        for (IItemHandler handler : itemInputs) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    recipeInput.addItem(stack);
                }
            }
        }

        for (IFluidHandler fluidHandler : fluidInputs) {
            for (int t = 0; t < fluidHandler.getTanks(); t++) {
                FluidStack fs = fluidHandler.getFluidInTank(t);
                if (!fs.isEmpty()) {
                    recipeInput.addFluid(fs);
                }
            }
        }

        CrystallizerRecipe recipe = MachineRecipeMatcher.findMatch(
                level,
                ModRecipeTypes.CRYSTALLIZER_TYPE,
                recipeInput
        );

        if (recipe == null) return;

        if (recipe.getItemInputs().isEmpty() && recipe.getItemCategories().isEmpty()) return;

        if (storage.getEnergyStored() < recipe.getEnergyPerTick()) return;

        if (!hasRequiredFluids(recipe, fluidInputs)) return;

        if (!hasRequiredItems(recipe, itemInputs)) return;

        consumeRequiredFluids(recipe, fluidInputs);

        currentRecipe = recipe;
        recipeRunning = true;
        recipeProgress = 0;
        recipeMaxProgress = recipe.getProcessingTime();
        renderData.running = true;
        renderData.outputItem = recipe.getItemOutputs().get(0).copy();

        consumeRequiredItems(recipe, itemInputs);

        sendUpdatePacket(null);
    }

    private void finishRecipe() {
        CrystallizerRecipe finished = currentRecipe;

        recipeRunning = false;
        renderData.running = false;

        if (finished != null) {
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
        renderData.outputItem = ItemStack.EMPTY;

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
        List<IItemHandler> list = new ArrayList<>();
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
                list.add(input.myTank);
            }
        }
        return list;
    }

    private List<IFluidHandler> findAllFluidOutputs() {
        List<IFluidHandler> list = new ArrayList<>();
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



    private void rebuildBlockCache() {
        blockCache.clear();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    Block b = level.getBlockState(p).getBlock();
                    blockCache.computeIfAbsent(b, k -> new ArrayList<>()).add(p);
                }
            }
        }
        cacheValid = true;
    }

    private List<BlockPos> findAllBlocks(Block blockType) {
        if (!cacheValid) rebuildBlockCache();
        return blockCache.getOrDefault(blockType, List.of());
    }

    private BlockPos findSpecificBlock(Block blockType) {
        if (!cacheValid) rebuildBlockCache();
        List<BlockPos> list = blockCache.get(blockType);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    public void clientTick() {
        if (level == null || !level.isClientSide) return;

        if (renderData.running) {
            if (internalFluidCapacity > 0) {
                renderData.fluidLevelClient =
                        (float) internalFluidAmount / (float) internalFluidCapacity;
            } else {
                renderData.fluidLevelClient = 0f;
            }
        } else {
            renderData.fluidLevelClient = 0f;
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

        tag.putInt("energyStored", clientEnergyStored);
        tag.putInt("energyMax", clientEnergyMax);
        tag.putInt("recipeProgress", recipeProgress);
        tag.putInt("recipeMaxProgress", recipeMaxProgress);
        tag.putInt("fluidAmount", clientFluidAmount);
        tag.putInt("fluidCapacity", clientFluidCapacity);
        tag.putInt("internalFluidAmount", internalFluidAmount);
        tag.putInt("internalFluidCapacity", internalFluidCapacity);

        // ⭐ NEW: Sync output item using CODEC
        DataResult<Tag> encoded = ItemStack.CODEC.encodeStart(
                net.minecraft.nbt.NbtOps.INSTANCE,
                renderData.outputItem
        );

        encoded.result().ifPresent(outTag -> tag.put("outputItem", outTag));



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

        if (tag.contains("recipeProgress") && tag.contains("recipeMaxProgress")) {
            int rp = tag.getInt("recipeProgress");
            int rm = tag.getInt("recipeMaxProgress");

            if (rm > 0) {
                renderData.recipeProgressClient = (float) rp / (float) rm;
            } else {
                renderData.recipeProgressClient = 0f;
            }
        }

        if (tag.contains("energyStored")) clientEnergyStored = tag.getInt("energyStored");
        if (tag.contains("energyMax")) clientEnergyMax = tag.getInt("energyMax");
        if (tag.contains("recipeProgress")) recipeProgress = tag.getInt("recipeProgress");
        if (tag.contains("recipeMaxProgress")) recipeMaxProgress = tag.getInt("recipeMaxProgress");
        if (tag.contains("fluidAmount")) clientFluidAmount = tag.getInt("fluidAmount");
        if (tag.contains("fluidCapacity")) clientFluidCapacity = tag.getInt("fluidCapacity");
        if (tag.contains("internalFluidAmount")) internalFluidAmount = tag.getInt("internalFluidAmount");
        if (tag.contains("internalFluidCapacity")) internalFluidCapacity = tag.getInt("internalFluidCapacity");

        // ⭐ NEW: Read output item
        if (tag.contains("outputItem")) {
            DataResult<ItemStack> decoded = ItemStack.CODEC.parse(
                    net.minecraft.nbt.NbtOps.INSTANCE,
                    tag.get("outputItem")
            );

            renderData.outputItem = decoded.result().orElse(ItemStack.EMPTY);
        }


    }

    @Override
    public void readServer(CompoundTag tag, ServerPlayer sender) {
        // No server-side direct tag handling needed
    }
}
