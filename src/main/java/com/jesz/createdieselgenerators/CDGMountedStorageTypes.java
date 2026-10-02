package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelMountedStorageType;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class CDGMountedStorageTypes {
    public static final OilBarrelMountedStorageType OIL_BARREL = Registry.register(
            CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE,
            ResourceKey.create(CreateRegistryKeys.MOUNTED_FLUID_STORAGE_TYPE, CreateDieselGenerators.rl("oil_barrel")),
            new OilBarrelMountedStorageType());

    public static void register() {}
}
