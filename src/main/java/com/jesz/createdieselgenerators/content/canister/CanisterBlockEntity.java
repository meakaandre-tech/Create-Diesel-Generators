package com.jesz.createdieselgenerators.content.canister;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CDGDataComponents;
import com.jesz.createdieselgenerators.CDGFluids;
import com.jesz.createdieselgenerators.fluid.SimpleFluidContent;
import com.jesz.createdieselgenerators.fluid.SmartFluidTank;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;
import java.util.Optional;

public class CanisterBlockEntity extends SmartBlockEntity {
    // capacities in droplets; the config values are millibuckets
    final int baseCapacity = Math.abs(CDGConfig.CANISTER_CAPACITY.get()) * CDGFluids.MB;
    final int capacityAddition = CDGConfig.CANISTER_CAPACITY_ENCHANTMENT.get() * CDGFluids.MB;
    public SmartFluidTank tank = new SmartFluidTank(baseCapacity, f -> {
        if (hasLevel() && !level.isClientSide()) {
            setChanged();
            sendData();
        }
    });
    BlockState state;

    public int capacityEnchantLevel;

    private DataComponentPatch componentPatch = DataComponentPatch.EMPTY;

    public CanisterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.state = state;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    protected void write(ValueOutput compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putInt("CapacityEnchantment", capacityEnchantLevel);
        compound.store("Components", DataComponentPatch.CODEC, componentPatch);
        tank.writeTo(compound, "Tank");
    }

    @Override
    protected void read(ValueInput compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        capacityEnchantLevel = compound.getIntOr("CapacityEnchantment", 0);
        componentPatch = compound.read("Components", DataComponentPatch.CODEC).orElse(DataComponentPatch.EMPTY);
        tank.setCapacity(baseCapacity + capacityAddition * capacityEnchantLevel);
        tank.readFrom(compound, "Tank");
    }

    public void setCapacityEnchantLevel(int capacityEnchantLevel) {
        this.capacityEnchantLevel = capacityEnchantLevel;
        tank.setCapacity(baseCapacity + capacityAddition * capacityEnchantLevel);
    }

    public void setComponentPatch(DataComponentPatch componentPatch) {
        this.componentPatch = componentPatch;
        Optional<? extends SimpleFluidContent> content = componentPatch.get(CDGDataComponents.FLUID_CONTENTS);
        if (content == null || content.isEmpty())
            return;

        this.tank.setFluid(content.get().copy());
    }

    public DataComponentPatch getComponentPatch() {
        return componentPatch;
    }
}
