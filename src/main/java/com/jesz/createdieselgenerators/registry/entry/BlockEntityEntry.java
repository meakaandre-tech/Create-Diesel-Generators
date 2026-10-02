package com.jesz.createdieselgenerators.registry.entry;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Small stand-in for Registrate's BlockEntityEntry.
 */
public final class BlockEntityEntry<T extends BlockEntity> {
    private BlockEntityType<T> type;

    public void set(BlockEntityType<T> type) {
        this.type = type;
    }

    public BlockEntityType<T> get() {
        return type;
    }

    public boolean is(BlockEntity blockEntity) {
        return blockEntity != null && blockEntity.getType() == type;
    }
}
