package com.jesz.createdieselgenerators.content.burner;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.zurrtum.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BurnerBlock extends HorizontalAxisKineticBlock implements IBE<BurnerBlockEntity>, FluidInventoryProvider<BurnerBlockEntity> {
    public static EnumProperty<BlazeBurnerBlock.HeatLevel> HEAT_LEVEL = BlazeBurnerBlock.HEAT_LEVEL;
    public static BooleanProperty LIT = BlockStateProperties.LIT;

    public BurnerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.NONE)
                .setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HEAT_LEVEL, LIT);
        super.createBlockStateDefinition(builder);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean intersects) {
        if (state.getValue(LIT) && entity instanceof LivingEntity && level instanceof ServerLevel serverLevel) {
            entity.hurtServer(serverLevel, level.damageSources().campfire(), 1);
        }

        super.entityInside(state, level, pos, entity, applier, intersects);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return box(1, 0, 1, 15, 12, 15);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean moved) {
        if (level.getBlockEntity(pos) instanceof BurnerBlockEntity be)
            be.redstonePower = level.getBestNeighborSignal(pos);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof BurnerBlockEntity be)
            return be.redstoneOutput;

        return 0;
    }

    @Override
    public Class<BurnerBlockEntity> getBlockEntityClass() {
        return BurnerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BurnerBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.BURNER.get();
    }

    @Override
    public @Nullable FluidInventory getFluidInventory(LevelAccessor world, BlockPos pos, BlockState state, BurnerBlockEntity blockEntity, @Nullable Direction context) {
        if (context != Direction.UP)
            return blockEntity.tank;
        return null;
    }
}
