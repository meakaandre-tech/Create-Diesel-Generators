package com.jesz.createdieselgenerators.client.render;

import com.jesz.createdieselgenerators.client.CDGPartialModels;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterBlockEntity;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BulkFermenterRenderer extends PartsRenderer<BulkFermenterBlockEntity> {
    public BulkFermenterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void collect(BulkFermenterBlockEntity be, State out, float partialTicks) {
        if (!be.isController())
            return;
        BlockState blockState = be.getBlockState();
        float width = be.getWidth();

        float dialPivotY = 6f / 16;
        float dialPivotZ = 8f / 16;
        // recipes are not known to the client; the server syncs the total duration instead
        float progress = be.processingDuration <= 0 || be.processingTime < 0 ? 0 :
                (float) Mth.clamp(Mth.lerp(partialTicks, be.processingTime + Math.sqrt(be.getWidth() * be.getHeight()), be.processingTime) / be.processingDuration, 0, 1);

        for (Direction d : Iterate.horizontalDirections) {
            out.add(CachedBuffers.partial(CDGPartialModels.BULK_FERMENTER_GAUGE, blockState)
                    .translate(width / 2f, 0.5, width / 2f)
                    .rotateYDegrees(d.toYRot())
                    .uncenter()
                    .translate(width / 2f - 6 / 16f, 0, 0));
            out.add(CachedBuffers.partial(AllPartialModels.BOILER_GAUGE_DIAL, blockState)
                    .translate(width / 2f, 0.5, width / 2f)
                    .rotateYDegrees(d.toYRot())
                    .uncenter()
                    .translate(width / 2f - 6 / 16f, 0, 0)
                    .translate(0, dialPivotY, dialPivotZ)
                    .rotateXDegrees(-180 * progress + 90)
                    .translate(0, -dialPivotY, -dialPivotZ));
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public boolean shouldRender(BulkFermenterBlockEntity be, Vec3 cameraPos) {
        return be.isController() && super.shouldRender(be, cameraPos);
    }
}
