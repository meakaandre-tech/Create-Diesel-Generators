package com.jesz.createdieselgenerators.fluid;

import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Stand-in for Create's SmartFluidTank on top of Create Fly's FluidTank.
 * All amounts are droplets (81 per millibucket).
 */
public class SmartFluidTank extends FluidTank {
    private final Consumer<FluidStack> updateCallback;
    private Predicate<FluidStack> validator = f -> true;

    public SmartFluidTank(int capacity, Consumer<FluidStack> updateCallback) {
        super(capacity);
        this.updateCallback = updateCallback;
    }

    public SmartFluidTank setValidator(Predicate<FluidStack> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public boolean isValid(int slot, FluidStack stack) {
        return validator.test(stack);
    }

    public boolean isFluidValid(FluidStack stack) {
        return validator.test(stack);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        onContentsChanged();
    }

    protected void onContentsChanged() {
        updateCallback.accept(getFluid());
    }

    @Override
    public void setFluid(FluidStack fluid) {
        super.setFluid(fluid);
        onContentsChanged();
    }

    public int getFluidAmount() {
        return getFluid().getAmount();
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSpace() {
        return Math.max(0, capacity - getFluid().getAmount());
    }

    public int fill(FluidStack stack, boolean simulate) {
        return FluidUtil.fill(this, stack, simulate);
    }

    public FluidStack drain(int amount, boolean simulate) {
        return FluidUtil.drain(this, amount, simulate);
    }

    public FluidStack drain(FluidStack stack, boolean simulate) {
        return FluidUtil.drain(this, stack, simulate);
    }

    public void writeTo(ValueOutput view, String key) {
        write(view.child(key));
    }

    public void readFrom(ValueInput view, String key) {
        read(view.childOrEmpty(key));
    }
}
