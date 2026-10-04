package com.jesz.createdieselgenerators.client.render;

import com.jesz.createdieselgenerators.CDGItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** How a basin's contents are drawn while it holds a mold: the mold lies flat with the other items on it. */
public class MoldBasinRender {
    /** Attached to Create's basin render state by BasinRenderStateMixin. */
    public interface Holder {
        @Nullable List<Entry> cdg$getMoldItems();

        void cdg$setMoldItems(@Nullable List<Entry> items);
    }

    public record Entry(ItemStackRenderState renderState, boolean mold, Vec3 offset) {
    }

    public static boolean hasMold(List<ItemStack> items) {
        for (ItemStack stack : items)
            if (CDGItems.MOLD.isIn(stack))
                return true;
        return false;
    }

    public static List<Entry> extract(ItemModelResolver resolver, @Nullable Level level, BlockPos pos, List<ItemStack> items) {
        List<Entry> entries = new ArrayList<>();
        RandomSource r = RandomSource.create(pos.hashCode());
        for (ItemStack stack : items) {
            boolean mold = CDGItems.MOLD.isIn(stack);
            ItemStackRenderState renderState = new ItemStackRenderState();
            renderState.displayContext = mold ? ItemDisplayContext.GROUND : ItemDisplayContext.FIXED;
            resolver.appendItemLayers(renderState, stack, renderState.displayContext, level, null, 0);
            entries.add(new Entry(renderState, mold, mold ? Vec3.ZERO : VecHelper.offsetRandomly(Vec3.ZERO, r, (float) 1 / 16)));
        }
        return entries;
    }

    public static void submit(List<Entry> entries, PoseStack ms, SubmitNodeCollector queue, int light) {
        for (Entry entry : entries) {
            ms.pushPose();
            if (entry.mold()) {
                ms.translate(0.5, 0.7, 0.5);
                ms.rotate(Axis.XP.rotationDegrees(90));
                ms.scale(1.75f, 1.75f, 1.75f);
                ms.translate(0, -0.125, 0);
            } else {
                ms.translate(0.5, 0.74, 0.5);
                ms.rotate(Axis.XP.rotationDegrees(90));
                ms.scale(0.5f, 0.5f, 0.5f);
                ms.translate(entry.offset());
            }
            entry.renderState().submit(ms, queue, light, OverlayTexture.NO_OVERLAY, 0);
            ms.popPose();
        }
    }
}
