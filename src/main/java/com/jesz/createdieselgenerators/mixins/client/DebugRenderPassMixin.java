package com.jesz.createdieselgenerators.mixins.client;

import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.frontend.FrontendRenderPass;
import com.mojang.renderpearl.frontend.FrontendRenderPipeline;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** TEMPORARY debugging aid. */
@Mixin(FrontendRenderPass.class)
public class DebugRenderPassMixin {
    @Shadow @Final private List<?> colorAttachments;

    @Inject(method = "setPipeline", at = @At("HEAD"))
    private void cdg$debug(CompiledRenderPipeline pipeline, CallbackInfo ci) {
        if (pipeline instanceof FrontendRenderPipeline p && p.colorTargetStates().size() != colorAttachments.size())
            System.out.println("CDGDEBUG pipeline " + p.name() + " targets " + p.colorTargetStates() + " attachments " + colorAttachments.size());
    }
}
