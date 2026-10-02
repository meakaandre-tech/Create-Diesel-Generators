package com.jesz.createdieselgenerators.client.render;

import com.jesz.createdieselgenerators.client.CDGPartialModels;
import com.jesz.createdieselgenerators.content.burner.BurnerBlockEntity;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.content.kinetics.base.HorizontalAxisKineticBlock;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class BurnerRenderer extends KineticPartsRenderer<BurnerBlockEntity> {
    public BurnerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void collect(BurnerBlockEntity be, State out, float partialTicks) {
        BlockState state = be.getBlockState();
        float rotation = Mth.lerp(Mth.lerp(partialTicks, be.prevValveState, be.valveState), -45, 45);
        out.add(CachedBuffers.partial(CDGPartialModels.SMALL_GAUGE_DIAL, state)
                .center().rotateYDegrees(state.getValue(HorizontalAxisKineticBlock.HORIZONTAL_AXIS) == Direction.Axis.X ? 90 : 0).uncenter()
                .translate(0.25, 0.25, 0.5)
                .rotateXDegrees(rotation));
        out.add(CachedBuffers.partial(CDGPartialModels.SMALL_GAUGE_DIAL, state)
                .center().rotateYDegrees(state.getValue(HorizontalAxisKineticBlock.HORIZONTAL_AXIS) == Direction.Axis.X ? 90 : 0).uncenter()
                .translate(0.75, 0.25, 0.5)
                .rotateXDegrees(-rotation));
    }
}
