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

public class ModularDieselEngineCTBehavior extends ConnectedTextureBehaviour {
    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, TextureAtlasSprite sprite) {
        return CDGSpriteShifts.MODULAR_DIESEL_ENGINE;
    }

    @Override
    public CTType getDataType(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction direction) {
        return AllCTTypes.CROSS;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter level, BlockPos pos, BlockPos otherPos, Direction face, Direction primaryOffset, Direction secondaryOffset) {
        if (!(state.getBlock() instanceof ModularDieselEngineBlock && other.getBlock() instanceof ModularDieselEngineBlock))
            return false;
        return ConnectivityHandler.isConnected(level, pos, otherPos);
    }
}
