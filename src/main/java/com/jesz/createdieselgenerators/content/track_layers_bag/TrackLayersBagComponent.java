package com.jesz.createdieselgenerators.content.track_layers_bag;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/** Tooltip data of a Track Layer's Bag; drawn by the client's TrackLayersBagClientComponent. */
public record TrackLayersBagComponent(ItemStack stack) implements TooltipComponent {
}
