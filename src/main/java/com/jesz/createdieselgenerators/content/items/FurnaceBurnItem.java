package com.jesz.createdieselgenerators.content.items;

import com.zurrtum.create.AllFuelTimes;
import net.minecraft.world.item.Item;

/** An item with a furnace burn time, registered through Create Fly's fuel table. */
public class FurnaceBurnItem extends Item {
    public final int burnTime;
    public FurnaceBurnItem(Properties properties, int burnTime) {
        super(properties);
        this.burnTime = burnTime;
        AllFuelTimes.ALL.put(this, burnTime);
    }
}
