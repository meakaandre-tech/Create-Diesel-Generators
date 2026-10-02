package com.jesz.createdieselgenerators.client.render;

import com.jesz.createdieselgenerators.client.CDGPartialModels;
import com.jesz.createdieselgenerators.content.basin_lid.BasinLidBlockEntity;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

import static com.jesz.createdieselgenerators.content.basin_lid.BasinLidBlock.ON_A_BASIN;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class BasinLidRenderer extends PartsRenderer<BasinLidBlockEntity> {
    public BasinLidRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void collect(BasinLidBlockEntity be, State out, float partialTicks) {
        if (!be.getBlockState().getValue(ON_A_BASIN))
            return;
        Direction facing = be.getBlockState().getValue(HORIZONTAL_FACING);
        out.add(CachedBuffers.partial(CDGPartialModels.SMALL_GAUGE_DIAL, be.getBlockState())
                .center()
                .rotateYDegrees(-facing.toYRot() + 180)
                .translate(0.5625f, -0.375, 1.0625)
                .uncenter()
                .rotateZDegrees(be.progress * -90 + 90));
    }
}
