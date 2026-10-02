package com.jesz.createdieselgenerators.registry.entry;

import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * Small stand-in for Registrate's FluidEntry, built on Create Fly's FlowableFluid.
 * get() returns the flowing fluid and getSource() the still one, as Registrate did.
 */
public class FluidEntry {
    public final int dropOff;
    public final int tickRate;
    public final int slopeFindDistance;
    public FlowableFluid flowing;
    public FlowableFluid still;
    public BucketItem bucket;
    public LiquidBlock block;

    public FluidEntry(int dropOff, int tickRate, int slopeFindDistance) {
        this.dropOff = dropOff;
        this.tickRate = tickRate;
        this.slopeFindDistance = slopeFindDistance;
        this.flowing = new Flowing(this);
        this.still = createStill();
    }

    protected FlowableFluid createStill() {
        return new Still(this);
    }

    public FlowableFluid get() {
        return flowing;
    }

    public FlowableFluid getSource() {
        return still;
    }

    public BucketItem getBucket() {
        return bucket;
    }

    public LiquidBlock getBlock() {
        return block;
    }

    public boolean is(Fluid fluid) {
        return fluid == flowing || fluid == still;
    }

    public abstract static class Base extends FlowableFluid {
        // set by the constructor of the entry; never read while the fluid itself is being constructed
        protected FluidEntry entry;

        protected Base(FluidEntry entry) {
            this.entry = entry;
        }

        @Override
        public Fluid getFlowing() {
            return entry.flowing;
        }

        @Override
        public Fluid getSource() {
            return entry.still;
        }

        @Override
        public Item getBucket() {
            return entry.bucket != null ? entry.bucket : Items.AIR;
        }

        @Override
        public BlockState createLegacyBlock(FluidState state) {
            if (entry.block == null)
                return Blocks.AIR.defaultBlockState();
            return entry.block.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
        }

        @Override
        public boolean isSame(Fluid fluid) {
            return fluid == entry.still || fluid == entry.flowing;
        }

        @Override
        public int getDropOff(LevelReader world) {
            return entry.dropOff;
        }

        @Override
        public int getSlopeFindDistance(LevelReader world) {
            return entry.slopeFindDistance;
        }

        @Override
        public int getTickDelay(LevelReader world) {
            return entry.tickRate;
        }

        @Override
        protected boolean canConvertToSource(ServerLevel world) {
            return false;
        }

        @Override
        public boolean canBeReplacedWith(FluidState state, BlockGetter world, BlockPos pos, Fluid fluid, Direction direction) {
            return direction == Direction.DOWN && !isSame(fluid);
        }

        @Override
        protected float getExplosionResistance() {
            return 100f;
        }

        @Override
        protected void entityInside(Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier) {
        }
    }

    public static class Flowing extends Base {
        public Flowing(FluidEntry entry) {
            super(entry);
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Still extends Base {
        public Still(FluidEntry entry) {
            super(entry);
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
