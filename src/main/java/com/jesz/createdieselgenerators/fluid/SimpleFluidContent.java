package com.jesz.createdieselgenerators.fluid;

import com.mojang.serialization.Codec;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Immutable fluid contents of an item, stored as a data component. Stand-in for NeoForge's SimpleFluidContent.
 * The amount is in droplets (81 per millibucket).
 */
public final class SimpleFluidContent {
    public static final SimpleFluidContent EMPTY = new SimpleFluidContent(FluidStack.EMPTY);
    public static final Codec<SimpleFluidContent> CODEC = FluidStack.OPTIONAL_CODEC.xmap(SimpleFluidContent::new, c -> c.stack);
    public static final StreamCodec<RegistryFriendlyByteBuf, SimpleFluidContent> STREAM_CODEC =
            FluidStack.OPTIONAL_PACKET_CODEC.map(SimpleFluidContent::new, c -> c.stack);

    private final FluidStack stack;

    private SimpleFluidContent(FluidStack stack) {
        this.stack = stack;
    }

    public static SimpleFluidContent copyOf(FluidStack stack) {
        return stack.isEmpty() ? EMPTY : new SimpleFluidContent(stack.copy());
    }

    public FluidStack copy() {
        return stack.copy();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int getAmount() {
        return stack.getAmount();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SimpleFluidContent other))
            return false;
        return stack.getAmount() == other.stack.getAmount() && FluidStack.areFluidsAndComponentsEqual(stack, other.stack);
    }

    @Override
    public int hashCode() {
        return stack.getAmount() * 31 + FluidStack.hashCode(stack);
    }
}
