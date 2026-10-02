package com.jesz.createdieselgenerators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;

/** One extracted piece of a block entity's render state, drawn in block-local space. */
@FunctionalInterface
public interface Part {
    void submit(PoseStack matrices, SubmitNodeCollector queue);
}
