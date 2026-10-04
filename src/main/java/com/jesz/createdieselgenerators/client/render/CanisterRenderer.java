package com.jesz.createdieselgenerators.client.render;

import com.jesz.createdieselgenerators.content.canister.CanisterBlockEntity;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/** Draws an enchanted canister a second time with the enchantment glint. */
public class CanisterRenderer extends PartsRenderer<CanisterBlockEntity> {
    public CanisterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void collect(CanisterBlockEntity be, State out, float partialTicks) {
        if (be.capacityEnchantLevel == 0)
            return;
        out.add(CachedBuffers.block(be.getBlockState()));
        SuperByteBufferRenderState glint = CachedBuffers.block(be.getBlockState())
                .light(out.lightCoords).extractRenderState();
        out.parts.add((matrices, queue) -> glint.submit(RenderTypes.patternedShieldGlint(), matrices, queue));
    }
}
