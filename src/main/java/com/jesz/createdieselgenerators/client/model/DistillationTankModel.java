package com.jesz.createdieselgenerators.client.model;

import com.jesz.createdieselgenerators.client.ct.DistillationTankCTBehavior;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import com.zurrtum.create.client.foundation.model.SimpleModelPart;
import com.zurrtum.create.client.infrastructure.model.CTModel;
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

/** Connected textures of the distillation tank; faces towards connected tanks are left out. */
public class DistillationTankModel extends CTModel {
    private static final ConnectedTextureBehaviour BEHAVIOUR = new DistillationTankCTBehavior();

    public DistillationTankModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked, BEHAVIOUR);
    }

    @Override
    public void addPartsWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random,
                                 List<BlockStateModelPart> parts) {
        int[] indices = createCTData(world, pos, state);
        boolean[] culledFaces = new boolean[4];
        for (Direction d : Iterate.horizontalDirections)
            culledFaces[d.get2DDataValue()] = ConnectivityHandler.isConnected(world, pos, pos.relative(d));

        List<BlockStateModelPart> modelParts = new ObjectArrayList<>();
        model.collectParts(random, modelParts);
        for (BlockStateModelPart part : modelParts) {
            QuadCollection.Builder builder = new QuadCollection.Builder();
            for (BakedQuad quad : part.getQuads(null))
                builder.addUnculledFace(replaceQuad(state, random, indices[quad.direction().get3DDataValue()], quad));
            for (Direction direction : Iterate.directions) {
                if (direction.getAxis().isHorizontal() && culledFaces[direction.get2DDataValue()])
                    continue;
                // as in the original model, faces are never culled against neighbouring blocks
                for (BakedQuad quad : part.getQuads(direction))
                    builder.addUnculledFace(replaceQuad(state, random, indices[quad.direction().get3DDataValue()], quad));
            }
            parts.add(new SimpleModelPart(builder.build(), part.useAmbientOcclusion(), part.particleMaterial()));
        }
    }
}
