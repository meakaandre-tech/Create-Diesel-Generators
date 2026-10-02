package com.jesz.createdieselgenerators.client.render;

import com.jesz.createdieselgenerators.client.CDGPartialModels;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

import static com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock.FACING;

public class DieselEngineRenderer extends KineticPartsRenderer<DieselEngineBlockEntity> {
    private static final PartialModel[] HORIZONTAL = {CDGPartialModels.ENGINE_PISTONS_0, CDGPartialModels.ENGINE_PISTONS_1,
            CDGPartialModels.ENGINE_PISTONS_2, CDGPartialModels.ENGINE_PISTONS_3, CDGPartialModels.ENGINE_PISTONS_4};
    private static final PartialModel[] VERTICAL = {CDGPartialModels.ENGINE_PISTONS_VERTICAL_0, CDGPartialModels.ENGINE_PISTONS_VERTICAL_1,
            CDGPartialModels.ENGINE_PISTONS_VERTICAL_2, CDGPartialModels.ENGINE_PISTONS_VERTICAL_3, CDGPartialModels.ENGINE_PISTONS_VERTICAL_4};

    public DieselEngineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    /** Frame of the piston animation for an angle step: 10 -> 0, 9 -> 1 ... 6,5 -> 4 ... 2 -> 1, else 0. */
    public static int pistonFrame(int angle) {
        return switch (angle) {
            case 9, 2 -> 1;
            case 8, 3 -> 2;
            case 7, 4 -> 3;
            case 6, 5 -> 4;
            default -> 0;
        };
    }

    @Override
    protected void collect(DieselEngineBlockEntity be, State out, float partialTicks) {
        int angle = (int) (Math.abs(getAngleForBe(be, be.getBlockPos(), getRotationAxisOf(be)) * 180 / Math.PI) * 3 % 360) / 36;
        SuperByteBuffer upgrade = EngineUpgradeRender.get(be, be.upgrade);
        if (upgrade != null)
            out.add(upgrade);
        Direction facing = be.getBlockState().getValue(FACING);
        if (facing.getAxis().isHorizontal()) {
            out.add(CachedBuffers.partial(HORIZONTAL[pistonFrame(angle)], be.getBlockState()).center()
                    .rotateYDegrees(facing.toYRot()).uncenter());
        } else {
            out.add(CachedBuffers.partial(VERTICAL[pistonFrame(angle)], be.getBlockState()).center()
                    .rotateYDegrees(facing == Direction.DOWN ? 180 : 270).rotateZDegrees(facing == Direction.DOWN ? 180 : 0).uncenter());
        }
    }
}
