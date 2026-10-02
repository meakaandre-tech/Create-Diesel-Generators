package com.jesz.createdieselgenerators.client.render;

import com.jesz.createdieselgenerators.client.CDGPartialModels;
import com.jesz.createdieselgenerators.content.distillation.DistillationTankBlockEntity;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DistillationTankRenderer extends PartsRenderer<DistillationTankBlockEntity> {
    protected final FluidStateModelSet fluidStateModelSet;

    public DistillationTankRenderer(BlockEntityRendererProvider.Context context) {
        fluidStateModelSet = context.blockModelResolver().modelManager.getFluidStateModelSet();
    }

    @Override
    protected void collect(DistillationTankBlockEntity be, State out, float partialTicks) {
        if (!be.isController())
            return;
        if (be.isBottom())
            collectBoiler(be, out, partialTicks);

        LerpedFloat fluidLevel = be.getFluidLevel();
        if (fluidLevel == null)
            return;

        float capHeight = 0.25f;
        float tankHullWidth = 1 / 16f + 1 / 128f;
        float minPuddleHeight = 1 / 16f;
        float totalHeight = be.getHeight() - 2 * capHeight - minPuddleHeight;

        float level = fluidLevel.getValue(partialTicks);
        if (level < 1 / (512f * totalHeight))
            return;
        float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);

        FluidStack fluidStack = be.tankInventory.getFluid();

        if (fluidStack.isEmpty())
            return;

        float xMin = tankHullWidth;
        float xMax = xMin + be.getWidth() - 2 * tankHullWidth;
        float yMin = totalHeight + capHeight + minPuddleHeight - clampedLevel;
        float yMax = yMin + clampedLevel;

        float zMin = tankHullWidth;
        float zMax = zMin + be.getWidth() - 2 * tankHullWidth;

        FluidRenderHelper.FluidRenderState fluid = FluidRenderHelper.extractFluidRenderState(
                out.level instanceof BlockAndTintGetter getter ? getter : null, out.blockPos, fluidStateModelSet,
                fluidStack.getFluid(), fluidStack.getComponentChanges(),
                xMin, yMin, zMin, xMax, yMax, zMax, out.lightCoords, false, true);
        float translate = clampedLevel - totalHeight;
        out.parts.add((matrices, queue) -> {
            matrices.pushPose();
            matrices.translate(0, translate, 0);
            fluid.submit(matrices, queue);
            matrices.popPose();
        });
    }

    protected void collectBoiler(DistillationTankBlockEntity be, State out, float partialTicks) {
        BlockState blockState = be.getBlockState();
        float width = be.getWidth();

        float dialPivotY = 6f / 16;
        float dialPivotZ = 8f / 16;
        // recipes are not known to the client; the server syncs the total duration instead
        float progress = Mth.clamp(be.processingDuration <= 0 || be.processingTime < 0 ? be.progress
                : (be.processingTime - partialTicks) / be.processingDuration, 0, 1);

        for (Direction d : Iterate.horizontalDirections) {
            out.add(CachedBuffers.partial(CDGPartialModels.DISTILLATION_GAUGE, blockState)
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
                    .rotateXDegrees(-145 * progress + 90)
                    .translate(0, -dialPivotY, -dialPivotZ));
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public boolean shouldRender(DistillationTankBlockEntity be, Vec3 cameraPos) {
        return be.isController() && super.shouldRender(be, cameraPos);
    }
}
