package com.jesz.createdieselgenerators.client.ct;

import com.jesz.createdieselgenerators.client.CDGSpriteShifts;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock;
import com.jesz.createdieselgenerators.content.distillation.DistillationTankBlock;
import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelBlock;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.client.foundation.block.connected.AllCTTypes;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.CTType;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import static com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelBlock.AXIS;
import static com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelBlock.OIL_BARREL_COLOR;

public class BulkFermenterCTBehavior extends ConnectedTextureBehaviour.Base {

    @Override
    public @Nullable CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        if(direction.getAxis().isVertical())
            return CDGSpriteShifts.BULK_FERMENTER_TOP;
        return CDGSpriteShifts.BULK_FERMENTER;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos, Direction face, Direction primaryOffset, Direction secondaryOffset) {
        return other.getBlock() instanceof BulkFermenterBlock && ConnectivityHandler.isConnected(reader, pos, otherPos);

    }
}
