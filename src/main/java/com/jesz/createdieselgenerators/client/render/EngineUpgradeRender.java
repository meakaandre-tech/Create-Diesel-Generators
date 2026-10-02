package com.jesz.createdieselgenerators.client.render;

import com.jesz.createdieselgenerators.client.CDGPartialModels;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

/** Models of the engine upgrades (silencer, turbocharger) attached to an engine. */
public class EngineUpgradeRender {

    @Nullable
    public static SuperByteBuffer get(BlockEntity be, EngineUpgrades upgrade) {
        if (upgrade == EngineUpgrades.SILENCER)
            return partial(be, CDGPartialModels.ENGINE_SILENCER, CDGPartialModels.ENGINE_SILENCER_VERTICAL,
                    CDGPartialModels.MODULAR_ENGINE_SILENCER, CDGPartialModels.HUGE_ENGINE_SILENCER);
        if (upgrade == EngineUpgrades.TURBOCHARGER)
            return partial(be, CDGPartialModels.ENGINE_TURBOCHARGER, CDGPartialModels.ENGINE_TURBOCHARGER_VERTICAL,
                    CDGPartialModels.MODULAR_TURBOCHARGER, CDGPartialModels.ENGINE_TURBOCHARGER);
        return null;
    }

    @Nullable
    private static SuperByteBuffer partial(BlockEntity be, PartialModel normalModel, PartialModel normalVerticalModel,
                                           PartialModel modularModel, PartialModel hugeModel) {
        if (be instanceof DieselEngineBlockEntity) {
            Direction facing = be.getBlockState().getValue(DieselEngineBlock.FACING);
            if (facing.getAxis() == Direction.Axis.Y) {
                return CachedBuffers.partial(normalVerticalModel, be.getBlockState())
                        .center()
                        .rotateYDegrees(facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 270 : 180)
                        .uncenter();
            } else {
                return CachedBuffers.partial(normalModel, be.getBlockState())
                        .center()
                        .rotateYDegrees(facing.toYRot())
                        .uncenter();
            }
        } else if (be instanceof ModularDieselEngineBlockEntity) {
            Direction facing = be.getBlockState().getValue(ModularDieselEngineBlock.FACING);
            return CachedBuffers.partial(modularModel, be.getBlockState())
                    .center()
                    .rotateYDegrees(facing.toYRot())
                    .uncenter();
        } else if (be instanceof HugeDieselEngineBlockEntity) {
            Direction facing = be.getBlockState().getValue(HugeDieselEngineBlock.FACING);
            if (facing.getAxis() == Direction.Axis.Y) {
                return CachedBuffers.partial(hugeModel, be.getBlockState())
                        .center().rotateZDegrees(90)
                        .rotateYDegrees(facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 270 : 90)
                        .uncenter();
            } else {
                return CachedBuffers.partial(hugeModel, be.getBlockState())
                        .center()
                        .rotateYDegrees(facing.getAxis() == Direction.Axis.X ? (facing.toYRot()) : (facing.toYRot()) + 180)
                        .uncenter();
            }
        }
        return null;
    }
}
