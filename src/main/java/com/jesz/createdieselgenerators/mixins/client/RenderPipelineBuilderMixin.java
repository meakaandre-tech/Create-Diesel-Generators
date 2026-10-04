package com.jesz.createdieselgenerators.mixins.client;

import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Since 26.3 a render pipeline has no colour target unless one is declared, and a render pass refuses a pipeline
 * whose colour target count differs from its attachments. Create Fly's opaque block-model pipelines
 * (ponder:pipeline/entity_block_solid, ..._cutout and the light/nether variants), which every model drawn through
 * a SuperByteBuffer uses, declare none and crash the frame. Give those the default colour target, as the game's own
 * opaque pipelines have. Does nothing once Create Fly declares the target itself.
 */
@Mixin(RenderPipeline.Builder.class)
public abstract class RenderPipelineBuilderMixin {
    @Shadow
    private Optional<Identifier> location;
    @Shadow
    private int activeColorTargetStateCount;

    @Shadow
    public abstract RenderPipeline.Builder withColorTargetState(ColorTargetState state);

    @Inject(method = "build()Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;", at = @At("HEAD"))
    private void cdg$defaultColorTarget(CallbackInfoReturnable<RenderPipeline> cir) {
        if (activeColorTargetStateCount != 0 || location.isEmpty())
            return;
        String namespace = location.get().getNamespace();
        if (namespace.equals("ponder") || namespace.equals("create"))
            withColorTargetState(ColorTargetState.DEFAULT);
    }
}
