package com.jesz.createdieselgenerators.mixins.client;

import com.jesz.createdieselgenerators.client.render.MoldBasinRender;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.processing.basin.BasinRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinInventory;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/** A basin holding a mold shows the mold lying flat with its ingredients on top, instead of the usual contents. */
@Mixin(BasinRenderer.class)
public abstract class BasinRendererMixin {
    @Shadow
    @Final
    protected ItemModelResolver itemModelManager;

    @Inject(method = "extractRenderState(Lcom/zurrtum/create/content/processing/basin/BasinBlockEntity;Lcom/zurrtum/create/client/content/processing/basin/BasinRenderer$BasinRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
            at = @At("HEAD"), cancellable = true)
    private void cdg$extractMold(BasinBlockEntity be, BasinRenderer.BasinRenderState state, float tickProgress, Vec3 cameraPos,
                                 ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, CallbackInfo ci) {
        BasinInventory inv = be.itemCapability;
        if (inv == null)
            return;
        List<ItemStack> items = new ArrayList<>();
        for (int slot = 0, size = inv.getContainerSize(); slot < size; slot++) {
            ItemStack stack = inv.getItem(slot);
            if (!stack.isEmpty())
                items.add(stack);
        }
        if (!MoldBasinRender.hasMold(items))
            return;

        BlockPos blockPos = be.getBlockPos();
        BlockState blockState = be.getBlockState();
        state.blockPos = blockPos;
        state.blockState = blockState;
        state.blockEntityType = be.getType();
        state.lightCoords = SmartBlockEntityRenderer.getLightCoords(be.getLevel(), blockPos);
        state.filter = FilteringRenderer.getFilterRenderState(be, blockState, itemModelManager,
                be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(blockPos)));
        ((MoldBasinRender.Holder) state).cdg$setMoldItems(MoldBasinRender.extract(itemModelManager, be.getLevel(), blockPos, items));
        ci.cancel();
    }

    @Inject(method = "submit(Lcom/zurrtum/create/client/content/processing/basin/BasinRenderer$BasinRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"), cancellable = true)
    private void cdg$submitMold(BasinRenderer.BasinRenderState state, PoseStack matrices, SubmitNodeCollector queue,
                                CameraRenderState cameraState, CallbackInfo ci) {
        List<MoldBasinRender.Entry> items = ((MoldBasinRender.Holder) state).cdg$getMoldItems();
        if (items == null)
            return;
        if (state.filter != null)
            state.filter.submit(state.blockState, queue, matrices, state.lightCoords);
        MoldBasinRender.submit(items, matrices, queue, state.lightCoords);
        ci.cancel();
    }
}
