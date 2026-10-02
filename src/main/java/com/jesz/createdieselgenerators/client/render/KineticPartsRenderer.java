package com.jesz.createdieselgenerators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Kinetic renderer that always draws its rotating model (a shaft unless overridden) itself,
 * plus a list of extra parts. No Flywheel visual is registered for these block entities.
 */
public abstract class KineticPartsRenderer<T extends KineticBlockEntity> extends KineticBlockEntityRenderer<T, KineticPartsRenderer.State> {

    public KineticPartsRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T be, State state, float partialTicks, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        state.parts.clear();
        Level level = be.getLevel();
        state.level = level;
        state.support = false;
        updateBaseRenderState(be, state, level, crumblingOverlay);
        state.model = null;
        if (rendersRotatingModel(be)) {
            state.angle = getAngleForBe(be, state.blockPos, state.axis);
            state.model = getRotatedModel(be, state).cardinalLighting(state.cardinalLighting)
                    .rotateCentered(state.angle, state.direction).light(state.lightCoords).color(state.color)
                    .extractRenderState();
        }
        collect(be, state, partialTicks);
    }

    protected boolean rendersRotatingModel(T be) {
        return true;
    }

    protected abstract void collect(T be, State state, float partialTicks);

    @Override
    protected SuperByteBuffer getRotatedModel(T be, State state) {
        return CachedBuffers.block(KINETIC_BLOCK, shaft(state.axis));
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        for (Part part : state.parts)
            part.submit(matrices, queue);
    }

    public static class State extends KineticRenderState {
        public final List<Part> parts = new ArrayList<>();
        public @Nullable Level level;

        /** Lights the model at the block entity's position and queues it. */
        public void add(SuperByteBuffer buffer) {
            parts.add(buffer.cardinalLighting(level).light(lightCoords).extractRenderState()::submit);
        }
    }
}
