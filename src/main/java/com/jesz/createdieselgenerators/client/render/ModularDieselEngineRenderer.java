package com.jesz.createdieselgenerators.client.render;

import com.jesz.createdieselgenerators.client.CDGPartialModels;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import java.util.Objects;

import static com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock.FACING;

public class ModularDieselEngineRenderer extends KineticPartsRenderer<ModularDieselEngineBlockEntity> {
    private static final PartialModel[] PISTONS = {CDGPartialModels.MODULAR_ENGINE_PISTONS_0, CDGPartialModels.MODULAR_ENGINE_PISTONS_1,
            CDGPartialModels.MODULAR_ENGINE_PISTONS_2, CDGPartialModels.MODULAR_ENGINE_PISTONS_3, CDGPartialModels.MODULAR_ENGINE_PISTONS_4};

    public ModularDieselEngineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void collect(ModularDieselEngineBlockEntity be, State out, float partialTicks) {
        int angle = (int) (Math.abs(getAngleForBe(be, be.getBlockPos(), getRotationAxisOf(be)) * 180 / Math.PI) * 3 % 360) / 36;
        ModularDieselEngineBlockEntity controller = be.getControllerBE();
        SuperByteBuffer upgrade = EngineUpgradeRender.get(be, Objects.requireNonNullElse(controller, be).upgrade);
        if (upgrade != null)
            out.add(upgrade);
        out.add(CachedBuffers.partial(PISTONS[DieselEngineRenderer.pistonFrame(angle)], be.getBlockState()).center()
                .rotateYDegrees(be.getBlockState().getValue(FACING).toYRot()).uncenter());
    }
}
