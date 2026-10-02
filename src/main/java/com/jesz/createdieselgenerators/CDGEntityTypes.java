package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.tools.ChemicalSprayerProjectileEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public class CDGEntityTypes {
    private static final ResourceKey<EntityType<?>> SPRAYER_KEY = ResourceKey.create(Registries.ENTITY_TYPE, CreateDieselGenerators.rl("chemical_sprayer_projectile"));

    private static final EntityType<ChemicalSprayerProjectileEntity> SPRAYER_TYPE = Registry.register(BuiltInRegistries.ENTITY_TYPE, SPRAYER_KEY,
            EntityType.Builder.<ChemicalSprayerProjectileEntity>of(ChemicalSprayerProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(20).build(SPRAYER_KEY));

    public static final Supplier<EntityType<ChemicalSprayerProjectileEntity>> CHEMICAL_SPRAYER_PROJECTILE = () -> SPRAYER_TYPE;

    public static void register(){}
}
