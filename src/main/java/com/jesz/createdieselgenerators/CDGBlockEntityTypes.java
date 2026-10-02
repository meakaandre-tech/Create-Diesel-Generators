package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.basin_lid.BasinLidBlockEntity;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterBlockEntity;
import com.jesz.createdieselgenerators.content.burner.BurnerBlockEntity;
import com.jesz.createdieselgenerators.content.canister.CanisterBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.PoweredEngineShaftBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.distillation.DistillationTankBlockEntity;
import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelBlockEntity;
import com.jesz.createdieselgenerators.content.pumpjack.*;
import com.jesz.createdieselgenerators.content.turret.ChemicalTurretBlockEntity;
import com.jesz.createdieselgenerators.fluid.CDGTransfer;
import com.jesz.createdieselgenerators.registry.entry.BlockEntityEntry;
import com.jesz.createdieselgenerators.registry.entry.BlockEntry;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.content.fluids.pipes.FluidPipeBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

public class CDGBlockEntityTypes {

    @FunctionalInterface
    private interface Factory<T extends BlockEntity> {
        T create(BlockEntityType<?> type, BlockPos pos, BlockState state);
    }

    public static final BlockEntityEntry<BurnerBlockEntity> BURNER = register("burner", BurnerBlockEntity::new, CDGBlocks.BURNER);

    public static final BlockEntityEntry<FluidPipeBlockEntity> CONCRETE_ENCASED_FLUID_PIPE = register("concrete_encased_fluid_pipe", FluidPipeBlockEntity::new,
            CDGBlocks.CONCRETE_ENCASED_FLUID_PIPES.values().toArray(new BlockEntry<?>[0]));

    public static final BlockEntityEntry<ChemicalTurretBlockEntity> CHEMICAL_TURRET = register("chemical_turret", ChemicalTurretBlockEntity::new, CDGBlocks.CHEMICAL_TURRET);

    public static final BlockEntityEntry<DieselEngineBlockEntity> DIESEL_ENGINE = register("diesel_engine_tile_entity", DieselEngineBlockEntity::new, CDGBlocks.DIESEL_ENGINE);

    public static final BlockEntityEntry<ModularDieselEngineBlockEntity> MODULAR_DIESEL_ENGINE = register("large_diesel_engine_tile_entity", ModularDieselEngineBlockEntity::new, CDGBlocks.MODULAR_DIESEL_ENGINE);

    public static final BlockEntityEntry<HugeDieselEngineBlockEntity> HUGE_DIESEL_ENGINE = register("huge_diesel_engine_block_entity", HugeDieselEngineBlockEntity::new, CDGBlocks.HUGE_DIESEL_ENGINE);

    public static final BlockEntityEntry<PoweredEngineShaftBlockEntity> POWERED_ENGINE_SHAFT = register("powered_engine_shaft_block_entity", PoweredEngineShaftBlockEntity::new, CDGBlocks.POWERED_ENGINE_SHAFT);

    public static final BlockEntityEntry<BasinLidBlockEntity> BASIN_LID = register("basin_lid_tile_entity", BasinLidBlockEntity::new, CDGBlocks.BASIN_LID);

    public static final BlockEntityEntry<PumpjackBearingBlockEntity> PUMPJACK_BEARING = register("pumpjack_bearing_block_entity", PumpjackBearingBlockEntity::new, CDGBlocks.PUMPJACK_BEARING);

    public static final BlockEntityEntry<CanisterBlockEntity> CANISTER = register("canister_block_entity", CanisterBlockEntity::new, CDGBlocks.CANISTER);

    public static final BlockEntityEntry<DistillationTankBlockEntity> DISTILLATION_TANK = register("distillation_tank_block_entity", DistillationTankBlockEntity::new, CDGBlocks.DISTILLATION_TANK);

    public static final BlockEntityEntry<BulkFermenterBlockEntity> BULK_FERMENTER = register("bulk_fermenter", BulkFermenterBlockEntity::new, CDGBlocks.BULK_FERMENTER);

    public static final BlockEntityEntry<OilBarrelBlockEntity> OIL_BARREL = register("oil_barrel_block_entity", OilBarrelBlockEntity::new, CDGBlocks.OIL_BARREL);

    public static final BlockEntityEntry<PumpjackHoleBlockEntity> PUMPJACK_HOLE = register("pumpjack_hole_block_entity", PumpjackHoleBlockEntity::new, CDGBlocks.PUMPJACK_HOLE);

    public static final BlockEntityEntry<PumpjackCrankBlockEntity> PUMPJACK_CRANK = register("pumpjack_crank_block_entity", PumpjackCrankBlockEntity::new, CDGBlocks.PUMPJACK_CRANK);

    public static final BlockEntityEntry<KineticBlockEntity> ENCASED_GIRDER = register("encased_girder", KineticBlockEntity::new, CDGBlocks.ANDESITE_GIRDER_ENCASED_SHAFT);

    private static <T extends BlockEntity> BlockEntityEntry<T> register(String name, Factory<T> factory, BlockEntry<?>... blocks) {
        BlockEntityEntry<T> entry = new BlockEntityEntry<>();
        Set<Block> set = new HashSet<>();
        for (BlockEntry<?> block : blocks)
            set.add(block.get());
        BlockEntityType<T> type = new BlockEntityType<>((pos, state) -> factory.create(entry.get(), pos, state), set);
        entry.set(Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, CreateDieselGenerators.rl(name), type));
        exposeFluids(type, set);
        return entry;
    }

    /** Blocks that hand out a fluid inventory to Create's pipes also offer it to Fabric's transfer API. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends BlockEntity> void exposeFluids(BlockEntityType<T> type, Set<Block> blocks) {
        for (Block block : blocks) {
            if (block instanceof FluidInventoryProvider provider) {
                CDGTransfer.registerFluidSide(type, (be, side) -> be.getLevel() == null ? null
                        : provider.getFluidInventory(be.getBlockState(), be.getLevel(), be.getBlockPos(), be, side));
                return;
            }
        }
    }

    public static void register() {
        DisplaySource.BY_BLOCK_ENTITY.add(PUMPJACK_HOLE.get(), CDGDisplaySources.PUMPJACK_OIL_AMOUNT);
    }
}
