package com.jesz.createdieselgenerators.client.scroll;

import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackCrankBlockEntity;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackCrankBlockEntity.CrankSize;
import com.jesz.createdieselgenerators.content.turret.ChemicalTurretBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.RotationDirectionScrollBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

/** Client halves of the scroll options (the value boxes players interact with). */
public class CDGScrollBehaviours {

    public static BlockEntityBehaviour<SmartBlockEntity> engine(DieselEngineBlockEntity be) {
        return new RotationDirectionScrollBehaviour(be,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), new DieselEngineValueBox());
    }

    public static BlockEntityBehaviour<SmartBlockEntity> modularEngine(ModularDieselEngineBlockEntity be) {
        return new RotationDirectionScrollBehaviour(be,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), new ModularDieselEngineValueBox());
    }

    public static BlockEntityBehaviour<SmartBlockEntity> hugeEngine(HugeDieselEngineBlockEntity be) {
        return new RotationDirectionScrollBehaviour(be,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), new HugeDieselEngineValueBox());
    }

    public static BlockEntityBehaviour<SmartBlockEntity> crank(PumpjackCrankBlockEntity be) {
        return new CrankSizeScrollBehaviour(be);
    }

    public static BlockEntityBehaviour<SmartBlockEntity> turretFilter(ChemicalTurretBlockEntity be) {
        return new FilteringBehaviour<>(be, new ChemicalTurretValueBox());
    }

    public static class ChemicalTurretValueBox extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 3, 16.05);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal();
        }
    }

    public static class CrankSizeScrollBehaviour extends ScrollOptionBehaviour<CrankSize> {
        public CrankSizeScrollBehaviour(SmartBlockEntity be) {
            super(CrankSizeIcon.class, CrankSizeIcon::from,
                    Component.translatable("createdieselgenerators.pumpjack_crank.crank_size"), be, new PumpjackCrankValueBox());
        }
    }

    public enum CrankSizeIcon implements INamedIconOptions {
        NORMAL(AllIcons.I_CLEAR), LARGE(AllIcons.I_PLACE);

        private final AllIcons icon;

        CrankSizeIcon(AllIcons icon) {
            this.icon = icon;
        }

        public static CrankSizeIcon from(CrankSize size) {
            return size == CrankSize.LARGE ? LARGE : NORMAL;
        }

        @Override
        public AllIcons getIcon() {
            return icon;
        }

        @Override
        public String getTranslationKey() {
            return "createdieselgenerators.tooltip.crank." + Lang.asId(name());
        }
    }

    public static class DieselEngineValueBox extends ValueBoxTransform.Sided {
        @Override
        protected boolean isSideActive(BlockState state, Direction side) {
            if (state.getValue(FACING) == Direction.UP)
                return side == Direction.WEST;
            if (state.getValue(FACING) == Direction.DOWN)
                return side == Direction.NORTH;
            return side == Direction.UP;
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            if (state.getValue(FACING) == Direction.UP)
                return VecHelper.voxelSpace(3, 8, 8);
            if (state.getValue(FACING) == Direction.DOWN)
                return VecHelper.voxelSpace(8, 8, 3);
            return VecHelper.voxelSpace(8, 13, 8);
        }

        @Override
        protected Vec3 getSouthLocation() {
            return Vec3.ZERO;
        }
    }

    public static class ModularDieselEngineValueBox extends ValueBoxTransform.Sided {
        @Override
        protected boolean isSideActive(BlockState state, Direction side) {
            return side == Direction.UP;
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            return VecHelper.voxelSpace(8, 16, 8);
        }

        @Override
        protected Vec3 getSouthLocation() {
            return Vec3.ZERO;
        }
    }

    public static class HugeDieselEngineValueBox extends ValueBoxTransform.Sided {
        @Override
        protected boolean isSideActive(BlockState state, Direction side) {
            return state.getValue(FACING).getAxis() != side.getAxis();
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            Vec3 location = new Vec3(0.5, 0.5, 1);
            location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
            location = VecHelper.rotateCentered(location, AngleHelper.verticalAngle(getSide()), Direction.Axis.X);
            return location;
        }

        @Override
        protected Vec3 getSouthLocation() {
            return Vec3.ZERO;
        }
    }

    public static class PumpjackCrankValueBox extends ValueBoxTransform.Sided {
        @Override
        protected boolean isSideActive(BlockState state, Direction side) {
            return side.getAxis() == state.getValue(HORIZONTAL_FACING).getClockWise(Direction.Axis.Y).getAxis();
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            Vec3 location = new Vec3(0.5, 0.5, 1);
            location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
            location = VecHelper.rotateCentered(location, AngleHelper.verticalAngle(getSide()), Direction.Axis.X);
            return location;
        }

        @Override
        protected Vec3 getSouthLocation() {
            return Vec3.ZERO;
        }
    }
}
