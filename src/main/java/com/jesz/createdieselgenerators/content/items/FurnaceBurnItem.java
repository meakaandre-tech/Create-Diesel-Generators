package com.jesz.createdieselgenerators.content.items;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

/**
 * An item with a furnace burn time. The time itself is data:
 * data/createdieselgenerators/context_int_provider/cooking/&lt;name&gt;.json, attached as the cooking fuel component.
 */
public class FurnaceBurnItem extends Item {
    public FurnaceBurnItem(Properties properties, String burnTime) {
        super(properties.cookingFuel(ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, CreateDieselGenerators.rl("cooking/" + burnTime))));
    }
}
