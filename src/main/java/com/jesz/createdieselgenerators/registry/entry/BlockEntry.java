package com.jesz.createdieselgenerators.registry.entry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Small stand-in for Registrate's BlockEntry, so the rest of the mod keeps its call sites.
 */
public record BlockEntry<T extends Block>(T block) implements ItemLike {
    public T get() {
        return block;
    }

    @Override
    public Item asItem() {
        return block.asItem();
    }

    public ItemStack asStack() {
        return new ItemStack(block);
    }

    public ItemStack asStack(int count) {
        return new ItemStack(block, count);
    }

    public BlockState getDefaultState() {
        return block.defaultBlockState();
    }

    public boolean has(BlockState state) {
        return state.is(block);
    }

    public boolean is(Block other) {
        return block == other;
    }

    public boolean isIn(ItemStack stack) {
        return stack.is(block.asItem());
    }
}
