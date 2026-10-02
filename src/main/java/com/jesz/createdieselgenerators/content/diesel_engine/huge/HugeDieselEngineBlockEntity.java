package com.jesz.createdieselgenerators.content.diesel_engine.huge;

import com.jesz.createdieselgenerators.CDGFluids;
import com.jesz.createdieselgenerators.ClientHooks;
import com.jesz.createdieselgenerators.fluid.FluidUtil;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollOptionBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.jesz.createdieselgenerators.content.diesel_engine.IEngine;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.zurrtum.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.zurrtum.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock.FACING;

public class HugeDieselEngineBlockEntity extends SmartBlockEntity implements IEngine {
    public ServerScrollOptionBehaviour<WindmillBearingBlockEntity.RotationDirection> movementDirection;
    public EngineUpgrades upgrade = EngineUpgrades.EMPTY;
    public SmartFluidTankBehaviour tank;
    WeakReference<PoweredEngineShaftBlockEntity> target = new WeakReference<>(null);
    public int analogSignal = 0;
    private boolean signalChanged = false;
    private float fuelDebt = 0f;
    public boolean overStressed = false;
    private FuelType cachedFuelType = FuelType.EMPTY;
    private FluidStack lastCachedFluid = FluidStack.EMPTY;
    private float cachedFuelSpeed = 0f;
    private float cachedFuelCapacity = 0f;
    private float cachedBurnRate = 0f;

    public HugeDieselEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void write(ValueOutput tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putString("Upgrade", upgrade.getId().toString());
        tag.putInt("AnalogSignal", analogSignal);
        tag.putBoolean("OverStressed", overStressed);
    }

    @Override
    protected void read(ValueInput tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        upgrade = EngineUpgrades.get(Identifier.parse(tag.getStringOr("Upgrade", EngineUpgrades.EMPTY.getId().toString())));
        analogSignal = tag.getIntOr("AnalogSignal", 0);
        fuelDebt = 0f;
        signalChanged = true;
        overStressed = tag.getBooleanOr("OverStressed", false);
        invalidateFuelCache();
    }

    @Override
    public void remove() {
        PoweredEngineShaftBlockEntity shaft = getShaft();
        if (shaft != null)
            shaft.removeGenerator(worldPosition);
        super.remove();
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(2);
    }

    @Override
    public void tick() {
        super.tick();

        PoweredEngineShaftBlockEntity shaft = getShaft();
        boolean wasOverStressed = overStressed;
        overStressed = shaft != null && shaft.isOverStressed();
        if (wasOverStressed && !overStressed)
            signalChanged = true;

        if (shaft != null && enabled() && getThrottle() > 0) {
            float throttle = getThrottle();
            shaft.update(worldPosition,
                    movementDirection.getValue() == 0 ? 1 : -1,
                    upgrade.getCapacity(getFuelCapacity(), this),
                    cachedFuelSpeed * throttle);
        } else if (shaft != null && getThrottle() == 0f) {
            shaft.removeGenerator(worldPosition);
        }

        if (signalChanged) {
            signalChanged = false;
            setChanged();
            sendData();
        }

        if (overStressed)
            return;

        if (shaft == null)
            return;

        if (enabled() && getThrottle() > 0) {
            if (shaft.movementDirection != 0 && shaft.movementDirection !=
                    (movementDirection.get() == WindmillBearingBlockEntity.RotationDirection.CLOCKWISE ? 1 : -1)) {
                shaft.removeGenerator(worldPosition);
                onDirectionChanged(movementDirection.getValue());
                return;
            }

            fuelDebt += cachedBurnRate * getFuelThrottle();
            while (fuelDebt >= 1f) {
                FluidUtil.drain(tank.getPrimaryHandler(), CDGFluids.MB, false);
                fuelDebt -= 1f;
            }

            if (level.isClientSide())
                ClientHooks.ENGINE_SOUND_TICK.accept(this);
        } else {
            shaft.removeGenerator(worldPosition);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        if (upgrade != EngineUpgrades.EMPTY && level != null)
            Block.popResource(level, pos, upgrade.getItem());
    }

    public PoweredEngineShaftBlockEntity getShaft() {
        PoweredEngineShaftBlockEntity shaft = target.get();
        if (shaft == null || shaft.isRemoved()) {
            if (shaft != null) {
                target = new WeakReference<>(null);
            }
            BlockEntity anyShaftAt = level.getBlockEntity(worldPosition.relative(getBlockState().getValue(FACING), 2));
            if (anyShaftAt instanceof PoweredEngineShaftBlockEntity ps) {
                target = new WeakReference<>(shaft = ps);
            }
        }
        return shaft;
    }
    
    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        movementDirection = new ServerScrollOptionBehaviour<>(WindmillBearingBlockEntity.RotationDirection.class, this);
        movementDirection.withCallback(this::onDirectionChanged);

        behaviours.add(movementDirection);
        tank = SmartFluidTankBehaviour.single(this, 100 * CDGFluids.MB);
        behaviours.add(tank);
    }

    private void onDirectionChanged(int v) {
        PoweredEngineShaftBlockEntity shaft = getShaft();
        if(shaft == null)
            return;
        for (Pair<BlockPos, Couple<Float>> engine : shaft.engines)
            if(level.getBlockEntity(engine.getFirst()) instanceof HugeDieselEngineBlockEntity be)
                be.movementDirection.setValue(v);
    }

    public @Nullable FluidInventory getFluidInventory(@Nullable Direction side) {
        if (side == null || side.getAxis() != getBlockState().getValue(FACING).getAxis())
            return tank.getPrimaryHandler();
        return null;
    }

    @Override
    public int getAnalogSignal() {
        return analogSignal;
    }

    @Override
    public SmartBlockEntity self() {
        return this;
    }

    @Override
    public FluidStack fs() {
        return tank.getPrimaryHandler().getFluid();
    }

    @Override
    public EngineUpgrades getUpgrade() {
        return upgrade;
    }

    @Override
    public void setUpgrade(EngineUpgrades upgrade) {
        this.upgrade = upgrade;
    }

    @Override public FuelType getCachedFuelType() { return cachedFuelType; }
    @Override public void setCachedFuelType(FuelType t) { cachedFuelType = t; }
    @Override public FluidStack getLastCachedFluid() { return lastCachedFluid; }
    @Override public void setLastCachedFluid(FluidStack f) { lastCachedFluid = f; }
    @Override public float getCachedFuelSpeed() { return cachedFuelSpeed; }
    @Override public void setCachedFuelSpeed(float s) { cachedFuelSpeed = s; }
    @Override public float getCachedFuelCapacity() { return cachedFuelCapacity; }
    @Override public void setCachedFuelCapacity(float c) { cachedFuelCapacity = c; }
    @Override public float getCachedBurnRate() { return cachedBurnRate; }
    @Override public void setCachedBurnRate(float r) { cachedBurnRate = r; }

    public void setAnalogSignal(int newSignal) { analogSignal = newSignal; }
    public void setSignalChanged(boolean newSignal) { signalChanged = newSignal; }
}
