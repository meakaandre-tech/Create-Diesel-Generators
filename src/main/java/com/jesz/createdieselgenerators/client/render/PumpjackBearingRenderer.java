package com.jesz.createdieselgenerators.client.render;

import com.jesz.createdieselgenerators.content.pumpjack.PumpjackBearingBlockEntity;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** A mechanical bearing's top without the shaft half behind it. */
public class PumpjackBearingRenderer extends PartsRenderer<PumpjackBearingBlockEntity> {
    public PumpjackBearingRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void collect(PumpjackBearingBlockEntity be, State out, float partialTicks) {
        final Direction facing = be.getBlockState()
                .getValue(BlockStateProperties.FACING);
        PartialModel top =
                be.isWoodenTop() ? AllPartialModels.BEARING_TOP_WOODEN : AllPartialModels.BEARING_TOP;
        SuperByteBuffer superBuffer = CachedBuffers.partial(top, be.getBlockState());

        float interpolatedAngle = be.getInterpolatedAngle(partialTicks - 1);
        superBuffer.rotateCentered((float) (interpolatedAngle / 180 * Math.PI), facing.getAxis().getPositive());
        superBuffer.color(KineticBlockEntityRenderer.getTintColor(be));

        if (facing.getAxis()
                .isHorizontal())
            superBuffer.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(facing.getOpposite())), Direction.UP);
        superBuffer.rotateCentered(AngleHelper.rad(-90 - AngleHelper.verticalAngle(facing)), Direction.EAST);
        out.add(superBuffer);
    }
}
