package com.jesz.createdieselgenerators.fluid;

import com.jesz.createdieselgenerators.content.tools.FueledToolItem;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventoryWrapper;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Fluid inventory of the mod's fluid-holding items (lighter, chemical sprayer, canister), backed by the
 * fluid_contents data component. Registered with Create Fly's item fluid inventories, so spouts,
 * item drains and the mod's own engines can fill and empty these items.
 */
public class ItemFluidInventory extends FluidItemInventoryWrapper {
    private ItemStack loadedFor;
    private FluidStack current = FluidStack.EMPTY;
    private int capacity;
    private Optional<Integer> max = Optional.empty();

    private void sync() {
        if (loadedFor == stack)
            return;
        loadedFor = stack;
        if (stack != null && stack.getItem() instanceof FueledToolItem item) {
            capacity = item.getCapacity(stack);
            max = Optional.of(Math.max(1, capacity));
            current = item.readFluid(stack);
            if (!current.isEmpty())
                setMaxSize(current, max);
        } else {
            capacity = 0;
            max = Optional.empty();
            current = FluidStack.EMPTY;
        }
    }

    private void save() {
        if (stack == null || !(stack.getItem() instanceof FueledToolItem item))
            return;
        item.writeFluid(stack, current.isEmpty() ? FluidStack.EMPTY : removeMaxSize(current.copy(), max));
    }

    @Override
    public int getMaxAmountPerStack() {
        sync();
        return capacity;
    }

    @Override
    public FluidStack getStack() {
        sync();
        return current;
    }

    @Override
    public void setStack(FluidStack fluid) {
        sync();
        current = fluid;
        if (!current.isEmpty() && max.isPresent())
            setMaxSize(current, max);
        save();
    }

    @Override
    public FluidStack onExtract(FluidStack fluid) {
        return max.isPresent() ? removeMaxSize(fluid, max) : fluid;
    }

    @Override
    public void markDirty() {
        save();
    }

    @Override
    public boolean isEmpty() {
        return getStack().isEmpty();
    }

    @Override
    public void close() {
        loadedFor = null;
        current = FluidStack.EMPTY;
        super.close();
    }
}
