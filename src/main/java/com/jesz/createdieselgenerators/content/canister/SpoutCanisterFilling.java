package com.jesz.createdieselgenerators.content.canister;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.fluid.FluidUtil;
import com.zurrtum.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.zurrtum.create.content.fluids.spout.SpoutBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SpoutCanisterFilling implements BlockSpoutingBehaviour {
    @Override
    public int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
        if (!CDGConfig.CANISTER_SPOUT_FILLING.get())
            return 0;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CanisterBlockEntity canister){
            FluidStack inTank = canister.tank.getFluid();
            if(inTank.isEmpty() || FluidUtil.isSameFluidSameComponents(inTank, availableFluid))
                return canister.tank.fill(availableFluid, simulate);
        }
        return 0;
    }
}
