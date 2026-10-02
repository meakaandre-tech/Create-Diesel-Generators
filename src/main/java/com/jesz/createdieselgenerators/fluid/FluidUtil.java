package com.jesz.createdieselgenerators.fluid;

import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

/**
 * NeoForge-style fill/drain calls for Create Fly's FluidInventory.
 */
public class FluidUtil {
    /** @return the amount that was (or would be) accepted */
    public static int fill(FluidInventory inventory, FluidStack stack, boolean simulate) {
        if (inventory == null || stack.isEmpty())
            return 0;
        if (simulate)
            return inventory.countSpace(stack);
        return inventory.insert(stack);
    }

    /** @return the fluid that was (or would be) removed */
    public static FluidStack drain(FluidInventory inventory, int amount, boolean simulate) {
        if (inventory == null || amount <= 0)
            return FluidStack.EMPTY;
        if (simulate) {
            FluidStack found = inventory.countAny(amount);
            return found.isEmpty() ? FluidStack.EMPTY : found.copy();
        }
        return inventory.extractAny(amount);
    }

    /** @return the fluid that was (or would be) removed */
    public static FluidStack drain(FluidInventory inventory, FluidStack stack, boolean simulate) {
        if (inventory == null || stack.isEmpty())
            return FluidStack.EMPTY;
        int amount = simulate ? inventory.count(stack) : inventory.extract(stack);
        if (amount <= 0)
            return FluidStack.EMPTY;
        return stack.copyWithAmount(amount);
    }

    public static FluidStack getFluidInTank(FluidInventory inventory, int tank) {
        if (inventory == null || tank >= inventory.size())
            return FluidStack.EMPTY;
        return inventory.getStack(tank);
    }

    public static boolean isSameFluid(FluidStack a, FluidStack b) {
        return a.getFluid() == b.getFluid();
    }

    public static boolean isSameFluidSameComponents(FluidStack a, FluidStack b) {
        return FluidStack.areFluidsAndComponentsEqualIgnoreCapacity(a, b);
    }
}
