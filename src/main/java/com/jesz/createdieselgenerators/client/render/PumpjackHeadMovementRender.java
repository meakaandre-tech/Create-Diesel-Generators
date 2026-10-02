package com.jesz.createdieselgenerators.client.render;

import com.jesz.createdieselgenerators.client.CDGPartialModels;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackBearingBlockEntity;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackHoleBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import com.zurrtum.create.content.contraptions.bearing.BearingContraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

/** The rope hanging from a moving pumpjack head down to its hole. */
public class PumpjackHeadMovementRender implements MovementRenderBehaviour {
    @Override
    @Nullable
    public MovementRenderState getRenderState(Vec3 camera, Font textRenderer, MovementContext context,
                                              VirtualRenderWorld renderWorld, Pose transform, Matrix4f worldMatrix4f) {
        if (context.data == null || !(context.contraption instanceof BearingContraption bearingContraption)
                || !(context.contraption.entity instanceof ControlledContraptionEntity entity))
            return null;
        BlockPos hole = context.data.read("HolePos", BlockPos.CODEC).orElse(null);
        if (hole == null || !(context.world.getBlockEntity(hole) instanceof PumpjackHoleBlockEntity))
            return null;
        if (!(context.world.getBlockEntity(context.contraption.anchor.relative(bearingContraption.getFacing().getOpposite())) instanceof PumpjackBearingBlockEntity))
            return null;
        if (context.position == null)
            return null;

        float partialTicks = AnimationTickHolder.getPartialTicks();
        int light = LightCoordsUtil.getLightCoords(renderWorld, context.localPos);
        SuperByteBuffer cover = CachedBuffers.partial(CDGPartialModels.PUMPJACK_ROPE, context.state)
                .transform(transform)
                .translate(context.localPos);
        Vec3 motion = context.motion == null ? Vec3.ZERO : context.motion;
        Vec3 prevPos = context.position.subtract(motion);
        if (bearingContraption.getFacing().getOpposite().getAxis() == Direction.Axis.X) {
            double zDst = Mth.lerp(partialTicks, prevPos.z, context.position.z) - hole.getZ() - 0.5f;
            double yDst = Mth.lerp(partialTicks, prevPos.y, context.position.y) - hole.getY() - 0.8f;
            float distanceFromHole = (float) Math.sqrt(zDst * zDst + yDst * yDst);

            double angle = -entity.getAngle(partialTicks) - (180 * Math.atan2(yDst, zDst) / Math.PI) + 90;
            cover.translate(0.5, 0.5, 0.5)
                    .rotateXDegrees((float) angle)
                    .scale(1, distanceFromHole, 1);
        } else {
            double xDst = Mth.lerp(partialTicks, prevPos.x, context.position.x) - hole.getX() - 0.5;
            double yDst = Mth.lerp(partialTicks, prevPos.y, context.position.y) - hole.getY() - 0.8f;
            float distanceFromHole = (float) Math.sqrt(xDst * xDst + yDst * yDst);

            double angle = -entity.getAngle(partialTicks) + (180 * Math.atan2(yDst, xDst) / Math.PI) - 90;
            cover.translate(0.5, 0.5, 0.5)
                    .rotateZDegrees((float) angle)
                    .scale(1, distanceFromHole, 1);
        }
        cover.light(light).useLevelLight(context.world, worldMatrix4f);
        return cover.extractRenderState()::submit;
    }
}
