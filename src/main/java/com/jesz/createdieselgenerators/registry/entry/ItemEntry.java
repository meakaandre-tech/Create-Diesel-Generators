package com.jesz.createdieselgenerators.registry.entry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Small stand-in for Registrate's ItemEntry.
 */
public record ItemEntry<T extends Item>(T item) implements ItemLike {
    public T get() {
        return item;
    }

    @Override
    public Item asItem() {
        return item;
    }

    public ItemStack asStack() {
        return new ItemStack(item);
    }

    public ItemStack asStack(int count) {
        return new ItemStack(item, count);
    }

    public boolean is(Item other) {
        return item == other;
    }

    public boolean isIn(ItemStack stack) {
        return stack.is(item);
    }
}
