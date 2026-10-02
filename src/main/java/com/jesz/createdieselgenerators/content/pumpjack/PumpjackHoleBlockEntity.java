package com.jesz.createdieselgenerators.content.pumpjack;

import com.jesz.createdieselgenerators.CDGFluids;
import com.jesz.createdieselgenerators.ClientHooks;
import com.jesz.createdieselgenerators.fluid.FluidUtil;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGTags;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.content.concrete.ConcreteEncasedFluidPipeBlock;
import com.jesz.createdieselgenerators.world.OilChunksSavedData;
import com.zurrtum.create.content.fluids.pipes.EncasedPipeBlock;
import com.zurrtum.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;

public class PumpjackHoleBlockEntity extends SmartBlockEntity {
    BlockState state;

    public SmartFluidTankBehaviour tank;
    public int headPos = 0;
    public int bearingPos = 0;
    public boolean started = false;
    public PumpjackHoleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.state = state;
    }

    @Override
    protected void write(ValueOutput tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("OilAmount", oilAmount);
        tag.putBoolean("Started", started);
    }

    public int oilAmount = 0;

    @Override
    protected void read(ValueInput tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        oilAmount = tag.getIntOr("OilAmount", 0);
        started = tag.getBooleanOr("Started", false);
    }

    byte tick = 0;
    public int pipeLength = 0;
    public boolean valid = false;
    @Override
    public void tick() {
        super.tick();
        tick++;
        if (tick >= 20) {
            int pipeLength = 0;
            tick = 0;
            boolean valid = false;
            for (int i = 0; i < getBlockPos().getY() - level.getMinY(); i++) {
                pipeLength++;
                BlockState bs = level.getBlockState(getBlockPos().below(i + 1));
                if (bs.getBlock() instanceof PipeBlock || bs.getBlock() instanceof EncasedPipeBlock || bs.getBlock() instanceof ConcreteEncasedFluidPipeBlock) {
                    if (!(bs.getValue(BlockStateProperties.UP) && bs.getValue(BlockStateProperties.DOWN)))
                        break;
                } else if(bs.getBlock() instanceof GlassFluidPipeBlock) {
                    if (!(bs.getValue(AXIS) == Direction.Axis.Y))
                        break;
                } else if (bs.is(CDGTags.PUMPJACK_PIPE)){
                    continue;
                } else if (bs.is(CDGTags.OIL_DEPOSIT)) {
                    valid = true;
                    break;
                } else
                    break;
            }
            int newPipeLength = valid ? pipeLength : 0;
            if (newPipeLength != this.pipeLength) {
                this.pipeLength = newPipeLength;
                invalidateRenderBoundingBox();
            }
            this.valid = valid;
        }
        if (level.isClientSide())
            ClientHooks.PUMPJACK_SOUND_TICK.accept(this);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(pipeLength);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, 8000 * CDGFluids.MB);
        behaviours.add(tank);
    }

    public void pumpjackRotation(boolean isCrankLarge) {

        List<Fluid> stackList = new ArrayList<>();
        for (Holder<Fluid> holder : BuiltInRegistries.FLUID.getTagOrEmpty(CDGTags.PUMPJACK_OUTPUT))
            stackList.add(holder.value());

        if (stackList.isEmpty())
            return;

        if (!level.isClientSide() && valid) {
            ChunkPos chunkPos = ChunkPos.containing(getBlockPos());
            oilAmount = OilChunksSavedData.getChunkOilAmount((ServerLevel) level, chunkPos);
            started = true;

            int subtractedAmount = Mth.clamp((int) (1000 * Math.abs((float) headPos / (float) bearingPos)) * (isCrankLarge ? 2 : 1), 0, oilAmount);

            if (subtractedAmount <= 0)
                return;
            // the oil chunk is counted in millibuckets, the tank in droplets
            int maxMb = Math.min(subtractedAmount, 8000);
            FluidStack oilStack = new FluidStack(stackList.get(0), maxMb * CDGFluids.MB);

            subtractedAmount = FluidUtil.fill(tank.getPrimaryHandler(), oilStack, false) / CDGFluids.MB;

            if (oilAmount == Integer.MAX_VALUE)
                return;

            oilAmount -= subtractedAmount;
            OilChunksSavedData.setChunkOilAmount((ServerLevel) level, chunkPos, oilAmount);
        }

        if (level.isClientSide() && oilAmount > 0)
            ClientHooks.PUMPJACK_POURING.spawn(level, worldPosition, new FluidStack(stackList.get(0), 1000 * CDGFluids.MB));
    }

    public @Nullable FluidInventory getFluidInventory(@Nullable Direction side) {
        if (side == null || (side.getAxis().isHorizontal() && getBlockState().getValue(
                side == Direction.NORTH ? NORTH :
                side == Direction.EAST ? EAST :
                side == Direction.WEST ? WEST : SOUTH
        )))
            return tank.getCapability();
        return null;
    }
}
