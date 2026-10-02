package com.jesz.createdieselgenerators.client.render;

import com.jesz.createdieselgenerators.client.CDGPartialModels;
import com.jesz.createdieselgenerators.content.turret.ChemicalTurretBlockEntity;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class ChemicalTurretRenderer extends KineticPartsRenderer<ChemicalTurretBlockEntity> {
    private final ItemModelResolver itemModelManager;

    public ChemicalTurretRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        itemModelManager = context.itemModelResolver();
    }

    @Override
    protected void collect(ChemicalTurretBlockEntity be, State out, float partialTicks) {
        BlockState state = be.getBlockState();

        // the entity filter in its slot
        Vec3 camera = Minecraft.getInstance().gameRenderer.mainCamera().position();
        double distance = be.isVirtual() ? -1 : camera.distanceToSqr(VecHelper.getCenterOf(be.getBlockPos()));
        FilteringRenderer.FilterRenderState filter = FilteringRenderer.getFilterRenderState(be, state, itemModelManager, distance);
        if (filter != null) {
            int light = out.lightCoords;
            out.parts.add((matrices, queue) -> filter.submit(state, queue, matrices, light));
        }

        float horizontalRotation = AngleHelper.angleLerp(partialTicks, be.oldHorizontalRotation, be.horizontalRotation);
        float verticalRotation = AngleHelper.angleLerp(partialTicks, be.oldVerticalRotation, be.verticalRotation);

        out.add(CachedBuffers.partial(CDGPartialModels.CHEMICAL_TURRET_CONNECTOR, state)
                .center()
                .rotateYDegrees(horizontalRotation)
                .uncenter());
        out.add(CachedBuffers.partial(CDGPartialModels.CHEMICAL_TURRET_BODY, state)
                .center()
                .rotateYDegrees(horizontalRotation + 180)
                .uncenter()
                .translate(0.5, 1.3125, 0.125)
                .rotateXDegrees(verticalRotation));
        if (be.lighterUpgrade)
            out.add(CachedBuffers.partial(CDGPartialModels.CHEMICAL_TURRET_LIGHTER, state)
                    .center()
                    .rotateYDegrees(horizontalRotation + 180)
                    .uncenter()
                    .translate(0.5, 1.3125, 0.125)
                    .rotateXDegrees(verticalRotation));
        out.add(CachedBuffers.partial(CDGPartialModels.CHEMICAL_TURRET_SMALL_COG, state)
                .center()
                .rotateYDegrees(horizontalRotation + 180)
                .uncenter()
                .translate(0.5, 1.3125, 0.125)
                .rotateXDegrees(verticalRotation)
                .rotateZDegrees(Mth.lerp(partialTicks, be.lastCogRotation, be.cogRotation)));
    }

    @Override
    protected SuperByteBuffer getRotatedModel(ChemicalTurretBlockEntity be, State state) {
        return CachedBuffers.partial(CDGPartialModels.CHEMICAL_TURRET_COG, state.blockState);
    }
}
