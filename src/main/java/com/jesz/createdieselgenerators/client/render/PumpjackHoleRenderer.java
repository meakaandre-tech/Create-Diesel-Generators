package com.jesz.createdieselgenerators.client.render;

import com.jesz.createdieselgenerators.client.CDGPartialModels;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackHoleBlockEntity;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class PumpjackHoleRenderer extends PartsRenderer<PumpjackHoleBlockEntity> {
    public PumpjackHoleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void collect(PumpjackHoleBlockEntity be, State out, float partialTicks) {
        if (be.pipeLength <= 0)
            return;
        out.add(CachedBuffers.partial(CDGPartialModels.PUMPJACK_ROPE, be.getBlockState())
                .translate(0.5, 0, 0.5)
                .scale(1, be.pipeLength, 1));
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
