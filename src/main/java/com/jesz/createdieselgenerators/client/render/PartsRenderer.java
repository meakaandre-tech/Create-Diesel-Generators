package com.jesz.createdieselgenerators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Block entity renderer that collects a list of transformed models while extracting
 * and draws them all on submit; replaces the immediate-mode renderSafe of older versions.
 */
public abstract class PartsRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, PartsRenderer.State> {

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T be, State state, float partialTicks, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
        state.parts.clear();
        state.level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        collect(be, state, partialTicks);
    }

    protected abstract void collect(T be, State state, float partialTicks);

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        for (Part part : state.parts)
            part.submit(matrices, queue);
    }

    public static class State extends BlockEntityRenderState {
        public final List<Part> parts = new ArrayList<>();
        public @Nullable Level level;

        /** Lights the model at the block entity's position and queues it. */
        public void add(SuperByteBuffer buffer) {
            parts.add(buffer.cardinalLighting(level).light(lightCoords).extractRenderState()::submit);
        }
    }
}
