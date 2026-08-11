package com.raven.arsimplemachines.recipe.precision;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.ArrayList;
import java.util.List;

public class PrecisionAssemblerRecipeInput implements RecipeInput {

    private final List<ItemStack> stacks = new ArrayList<>();

    public PrecisionAssemblerRecipeInput(List<ItemStack> inputStacks) {
        for (ItemStack s : inputStacks) {
            if (!s.isEmpty()) {
                stacks.add(s.copy());
            }
        }
    }

    public void addItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            stacks.add(stack.copy());
        }
    }

    public List<ItemStack> getItems() {
        return stacks;
    }

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    public ItemStack getItem(int index) {
        if (index < 0 || index >= stacks.size()) {
            return ItemStack.EMPTY;
        }
        return stacks.get(index);
    }

    /**
     * Check if this wrapper contains all required input items.
     * Used by PrecisionAssemblerRecipe.matches().
     */
    public boolean containsAll(List<ItemStack> required) {
        List<ItemStack> available = new ArrayList<>();
        for (ItemStack s : stacks) {
            available.add(s.copy());
        }

        for (ItemStack req : required) {
            int needed = req.getCount();
            ItemStack matchItem = req;

            for (ItemStack avail : available) {
                if (avail.is(matchItem.getItem())) {
                    int take = Math.min(avail.getCount(), needed);
                    avail.shrink(take);
                    needed -= take;
                    if (needed <= 0) break;
                }
            }

            if (needed > 0) {
                return false; // missing required items
            }
        }

        return true;
    }
}
