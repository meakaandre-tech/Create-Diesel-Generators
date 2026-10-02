package com.jesz.createdieselgenerators.fluid;

import com.google.common.collect.MapMaker;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.transfer.FluidInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * Exposes the mod's fluid inventories to Fabric's transfer API, for pipes of other mods.
 * Create's own pipes go through FluidInventoryProvider on the block.
 */
public class CDGTransfer {
    private static final Map<FluidInventory, Storage<FluidVariant>[]> CACHE = new MapMaker().weakKeys().makeMap();

    @SuppressWarnings("unchecked")
    public static Storage<FluidVariant> storage(FluidInventory inventory, Direction side) {
        if (inventory == null)
            return null;
        Storage<FluidVariant>[] sides = CACHE.computeIfAbsent(inventory, i -> new Storage[7]);
        int index = side == null ? 6 : side.get3DDataValue();
        Storage<FluidVariant> storage = sides[index];
        if (storage == null)
            storage = sides[index] = FluidInventoryStorage.of(inventory, side);
        return storage;
    }

    public static <T extends BlockEntity> void registerFluidSide(BlockEntityType<T> type, BiFunction<T, Direction, FluidInventory> factory) {
        FluidStorage.SIDED.registerForBlockEntity((be, side) -> storage(factory.apply(be, side), side), type);
    }
}
