package com.jesz.createdieselgenerators.client.model;

import com.google.common.base.Suppliers;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
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
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import static com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper.submitQuads;

/** The chemical sprayer with its spinning cog; the cog speeds up while the sprayer is being used. */
public class ChemicalSprayerModel implements ItemModel {
    public static final Identifier ID = CreateDieselGenerators.rl("model/chemical_sprayer");
    public static final Identifier COG_ID = CreateDieselGenerators.rl("item/chemical_sprayer/cog");

    private final List<BakedQuad> itemQuads;
    private final ModelRenderProperties itemSettings;
    private final Supplier<Vector3fc[]> itemExtents;
    private final List<BakedQuad> cogQuads;
    private final Supplier<Vector3fc[]> cogExtents;

    public ChemicalSprayerModel(List<BakedQuad> itemQuads, ModelRenderProperties itemSettings, List<BakedQuad> cogQuads) {
        this.itemQuads = itemQuads;
        this.itemSettings = itemSettings;
        itemExtents = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(this.itemQuads));
        this.cogQuads = cogQuads;
        cogExtents = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(cogQuads));
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext,
                       @Nullable ClientLevel world, @Nullable ItemOwner user, int seed) {
        state.appendModelIdentityElement(this);
        state.setAnimated();
        LayerRenderState itemLayer = submitQuads(state, itemSettings, displayContext, itemQuads);
        itemLayer.setExtents(itemExtents);
        LayerRenderState cogLayer = submitQuads(state, itemSettings, displayContext, cogQuads);
        cogLayer.setExtents(cogExtents);

        LocalPlayer player = Minecraft.getInstance().player;
        float worldTime = AnimationTickHolder.getRenderTime() / 10;
        boolean using = player != null && player.isUsingItem() && player.getItemInHand(player.getUsedItemHand()) == stack;
        float angle = worldTime * (using ? -200 : -25);
        angle %= 360;

        // same steps as the old renderer, moved from centred into model space
        cogLayer.localTransform.translate(0.5f, 0.5f, 0.5f)
                .rotate(Axis.ZP.rotationDegrees(angle))
                .translate(0.5f, 0.5f, 0.53125f)
                .translate(-0.5f, -0.5f, -0.5f);

        if (stack.hasFoil()) {
            state.appendModelIdentityElement(FoilType.STANDARD);
            itemLayer.setFoilType(FoilType.STANDARD);
            cogLayer.setFoilType(FoilType.STANDARD);
        }
    }

    public record Unbaked(Identifier model) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model)).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(model);
            resolver.markDependency(COG_ID);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel item = baker.getModel(model);
            TextureSlots itemTextures = item.getTopTextureSlots();
            List<BakedQuad> itemQuads = item.bakeTopGeometry(itemTextures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties itemSettings = ModelRenderProperties.fromResolvedModel(baker, item, itemTextures);
            return new ChemicalSprayerModel(itemQuads, itemSettings, BakedModelHelper.bakeQuads(baker, COG_ID));
        }
    }
}
