package com.jesz.createdieselgenerators.client.model;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.model.SimpleModelPart;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

import static com.jesz.createdieselgenerators.content.sheetmetal.SheetMetalPanelBlock.FACING;
import static com.jesz.createdieselgenerators.content.sheetmetal.SheetMetalPanelBlock.ROLL;

/** Leaves out the edge faces of sheet metal panels that continue into a neighbouring panel. */
public class SheetMetalPanelModel extends WrapperBlockStateModel {
    public SheetMetalPanelModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    public void addPartsWithInfo(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                                 List<BlockStateModelPart> parts) {
        boolean[] culledAxes = new boolean[3];

        for (Direction d : Iterate.directions) {
            if (d.getAxis() == state.getValue(FACING).getAxis())
                continue;
            BlockState otherState = level.getBlockState(pos.relative(d));
            if (!CDGBlocks.SHEET_METAL_PANEL.has(otherState))
                continue;
            if (otherState.getValue(FACING) != state.getValue(FACING))
                continue;
            if (otherState.getValue(ROLL) != state.getValue(ROLL))
                continue;
            if (!state.getValue(ROLL) && state.getValue(FACING).getAxis().isHorizontal() && d.getAxis().isHorizontal())
                continue;
            if (state.getValue(ROLL) && state.getValue(FACING).getAxis().isHorizontal() && d.getAxis().isVertical())
                continue;
            if (state.getValue(FACING).getAxis().isVertical() && (d.getAxis() == Direction.Axis.Z && state.getValue(ROLL) || (d.getAxis() == Direction.Axis.X && !state.getValue(ROLL))))
                continue;
            culledAxes[d.getAxis().ordinal()] = true;
        }

        List<BlockStateModelPart> modelParts = new ObjectArrayList<>();
        model.collectParts(random, modelParts);
        for (BlockStateModelPart part : modelParts) {
            QuadCollection.Builder builder = new QuadCollection.Builder();
            for (BakedQuad quad : part.getQuads(null))
                if (!culledAxes[quad.direction().getAxis().ordinal()])
                    builder.addUnculledFace(quad);
            for (Direction direction : Iterate.directions)
                for (BakedQuad quad : part.getQuads(direction))
                    if (!culledAxes[quad.direction().getAxis().ordinal()])
                        builder.addCulledFace(direction, quad);
            parts.add(new SimpleModelPart(builder.build(), part.useAmbientOcclusion(), part.particleMaterial()));
        }
    }
}
