package com.jesz.createdieselgenerators.client.model;

import com.google.common.base.Suppliers;
import com.jesz.createdieselgenerators.CDGDataComponents;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper;
import com.zurrtum.create.infrastructure.component.SandPaperItemComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper.submitQuads;

/**
 * Hammer and wire cutters: while an item is being processed, the tool swings (or snaps) and the item is drawn with it.
 * The transforms are the ones of the old item renderers, moved from centred into model space.
 */
public class ProcessingToolModel implements ItemModel {
    public static final Identifier ID = CreateDieselGenerators.rl("model/processing_tool");

    private final boolean hammer;
    private final List<BakedQuad> quads;
    private final ModelRenderProperties settings;
    private final Supplier<Vector3fc[]> extents;
    private final List<BakedQuad> openQuads;
    private final Supplier<Vector3fc[]> openExtents;

    public ProcessingToolModel(boolean hammer, List<BakedQuad> quads, ModelRenderProperties settings, List<BakedQuad> openQuads) {
        this.hammer = hammer;
        this.quads = quads;
        this.settings = settings;
        extents = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(this.quads));
        this.openQuads = openQuads;
        openExtents = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(this.openQuads));
    }

    private static Matrix4f centred() {
        return new Matrix4f().translate(0.5f, 0.5f, 0.5f);
    }

    /** Finishes a transform for one of this model's own quad lists. */
    private static void applyToOwn(LayerRenderState layer, Matrix4f transform) {
        layer.localTransform.mulLocal(transform.translate(-0.5f, -0.5f, -0.5f));
    }

    private LayerRenderState tool(ItemStackRenderState state, ItemStack stack, ItemDisplayContext displayContext, boolean open) {
        LayerRenderState layer = submitQuads(state, settings, displayContext, open ? openQuads : quads);
        layer.setExtents(open ? openExtents : extents);
        if (stack.hasFoil()) {
            state.appendModelIdentityElement(FoilType.STANDARD);
            layer.setFoilType(FoilType.STANDARD);
        }
        return layer;
    }

    /** Draws the processed item in the tool's display transform, with the given centred-space transform. */
    private void item(ItemStackRenderState state, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext,
                      ItemDisplayContext itemContext, @Nullable ClientLevel world, @Nullable ItemOwner user, int seed, Matrix4f transform) {
        int i = state.activeLayerCount;
        resolver.appendItemLayers(state, item, itemContext, world, user, seed);
        int size = state.activeLayerCount;
        ItemTransform outer = settings.transforms().getTransform(displayContext);
        boolean applyLeftHandFix = displayContext.leftHand();
        for (; i < size; i++) {
            LayerRenderState layer = state.layers[i];
            ItemTransform itemTransform = layer.itemTransform;
            layer.itemTransform = outer;
            layer.localTransform.mulLocal(ItemModelRenderHelper.getPose(applyLeftHandFix, itemTransform)).mulLocal(transform);
        }
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext,
                       @Nullable ClientLevel world, @Nullable ItemOwner user, int seed) {
        state.appendModelIdentityElement(this);
        SandPaperItemComponent processing = stack.get(CDGDataComponents.PROCESSING_ITEM);
        if (processing == null || Minecraft.getInstance().player == null) {
            tool(state, stack, displayContext, false);
            return;
        }
        state.setAnimated();
        ItemStack processingItem = processing.item();
        if (hammer)
            updateHammer(state, stack, processingItem, resolver, displayContext, world, user, seed);
        else
            updateWireCutters(state, stack, processingItem, resolver, displayContext, world, user, seed);
    }

    private void updateHammer(ItemStackRenderState state, ItemStack stack, ItemStack processingItem, ItemModelResolver resolver,
                              ItemDisplayContext displayContext, @Nullable ClientLevel world, @Nullable ItemOwner user, int seed) {
        float time = ((AnimationTickHolder.getTicks() + AnimationTickHolder.getPartialTicks()) % 10) / 10;
        time -= 0.5f;
        float swing = Math.abs(time * time * time);
        if (!displayContext.firstPerson()) {
            boolean thirdPerson = displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                    || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            if (!thirdPerson) {
                item(state, processingItem, resolver, displayContext, ItemDisplayContext.NONE, world, user, seed, centred());
                applyToOwn(tool(state, stack, displayContext, false), centred()
                        .translate(0.5f, -0.2f, 0)
                        .scale(0.75f, 0.75f, 1.1f)
                        .translate(-0.5f, -0.5f, -0.5f)
                        .rotate(Axis.ZP.rotationDegrees(swing * 300))
                        .translate(0.5f, 0.5f, 0.5f));
            } else {
                item(state, processingItem, resolver, displayContext, ItemDisplayContext.NONE, world, user, seed, centred()
                        .translate(-0.2f, 0.4f, 0)
                        .scale(0.75f)
                        .rotate(Axis.YP.rotationDegrees(77)));
                applyToOwn(tool(state, stack, displayContext, false), centred()
                        .rotate(Axis.YP.rotationDegrees(90))
                        .translate(0, 0, -0.7f)
                        .scale(0.75f, 0.75f, 1.1f)
                        .rotate(Axis.YP.rotationDegrees(77))
                        .translate(-0.5f, -0.5f, -0.5f)
                        .rotate(Axis.ZP.rotationDegrees(swing * -180 + 80))
                        .translate(0.5f, 0.5f, 0.5f));
            }
        } else {
            boolean flip = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            applyToOwn(tool(state, stack, displayContext, false), centred()
                    .translate(0, 0, flip ? -1 : 1)
                    .translate(0, 0, -0.6f)
                    .rotate(Axis.YP.rotationDegrees(45))
                    .translate(-0.5f, -0.5f, -0.5f)
                    .rotate(Axis.ZP.rotationDegrees(swing * 400))
                    .translate(0.5f, 0.5f, 0.5f));
            item(state, processingItem, resolver, displayContext, ItemDisplayContext.NONE, world, user, seed, centred()
                    .translate(0, 0, flip ? -1 : 1)
                    .translate(-0.5f, 0.4f, 0)
                    .translate((float) (Math.cos((time) * -Math.PI) / -10), 0, 0)
                    .rotate(Axis.YP.rotationDegrees(-45)));
        }
    }

    private void updateWireCutters(ItemStackRenderState state, ItemStack stack, ItemStack processingItem, ItemModelResolver resolver,
                                   ItemDisplayContext displayContext, @Nullable ClientLevel world, @Nullable ItemOwner user, int seed) {
        float time = ((AnimationTickHolder.getTicks() + AnimationTickHolder.getPartialTicks()) % 10) / 10;
        item(state, processingItem, resolver, displayContext, ItemDisplayContext.GUI, world, user, seed, centred()
                .translate(0.1f, 0.2f, 0)
                .rotate(Axis.ZP.rotationDegrees((float) ((AnimationTickHolder.getTicks() + 5) / 10) * -30)));
        applyToOwn(tool(state, stack, displayContext, time <= 0.5), centred()
                .translate(0, 0, 0.1f)
                .rotate(Axis.YP.rotationDegrees(32)));
    }

    public record Unbaked(Identifier model, Optional<Identifier> openModel, String tool) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model),
                Identifier.CODEC.optionalFieldOf("open_model").forGetter(Unbaked::openModel),
                Codec.STRING.fieldOf("tool").forGetter(Unbaked::tool)
        ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(model);
            openModel.ifPresent(resolver::markDependency);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel resolved = baker.getModel(model);
            TextureSlots textures = resolved.getTopTextureSlots();
            List<BakedQuad> quads = resolved.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, resolved, textures);
            List<BakedQuad> openQuads = quads;
            if (openModel.isPresent()) {
                ResolvedModel open = baker.getModel(openModel.get());
                openQuads = open.bakeTopGeometry(open.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY).getAll();
            }
            return new ProcessingToolModel("hammer".equals(tool), quads, settings, openQuads);
        }
    }
}
