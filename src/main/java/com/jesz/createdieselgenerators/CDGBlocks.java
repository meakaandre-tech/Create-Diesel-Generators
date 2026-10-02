package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.andesite_girder.AndesiteGirderBlock;
import com.jesz.createdieselgenerators.content.andesite_girder.AndesiteGirderEncasedShaftBlock;
import com.jesz.createdieselgenerators.content.basin_lid.BasinLidBlock;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterBlock;
import com.jesz.createdieselgenerators.content.burner.BurnerBlock;
import com.jesz.createdieselgenerators.content.burner.BurnerBlockEntity;
import com.jesz.createdieselgenerators.content.canister.CanisterBlock;
import com.jesz.createdieselgenerators.content.canister.CanisterBlockItem;
import com.jesz.createdieselgenerators.content.concrete.ConcreteEncasedFluidPipeBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.PoweredEngineShaftBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock;
import com.jesz.createdieselgenerators.content.distillation.DistillationTankBlock;
import com.jesz.createdieselgenerators.content.items.MultiBlockContainerBlockItem;
import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelBlock;
import com.jesz.createdieselgenerators.content.pumpjack.*;
import com.jesz.createdieselgenerators.content.sheetmetal.SheetMetalPanelBlock;
import com.jesz.createdieselgenerators.content.turret.ChemicalTurretBlock;
import com.jesz.createdieselgenerators.contraption.DieselEngineMovementBehaviour;
import com.jesz.createdieselgenerators.contraption.PumpjackBearingBMovementBehaviour;
import com.jesz.createdieselgenerators.contraption.PumpjackHeadMovementBehaviour;
import com.jesz.createdieselgenerators.registry.entry.BlockEntry;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.api.boiler.BoilerHeater;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.stress.BlockStressValues;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class CDGBlocks {
    // Create's SharedProperties
    private static Properties copperMetal() { return Properties.ofFullCopy(Blocks.COPPER_BLOCK); }
    private static Properties softMetal() { return Properties.ofFullCopy(Blocks.GOLD_BLOCK); }
    private static Properties stone() { return Properties.ofFullCopy(Blocks.ANDESITE); }

    public static final MovementBehaviour DIESEL_ENGINE_MOVEMENT = new DieselEngineMovementBehaviour();
    public static final MovementBehaviour MODULAR_DIESEL_ENGINE_MOVEMENT = new DieselEngineMovementBehaviour();
    public static final MovementBehaviour PUMPJACK_HEAD_MOVEMENT = new PumpjackHeadMovementBehaviour();
    public static final MovementBehaviour PUMPJACK_BEARING_B_MOVEMENT = new PumpjackBearingBMovementBehaviour();

    public static final BlockEntry<BurnerBlock> BURNER = block("burner", BurnerBlock::new,
            () -> copperMetal().requiresCorrectToolForDrops(), BlockItem::new);

    public static final BlockEntry<ChemicalTurretBlock> CHEMICAL_TURRET = block("chemical_turret", ChemicalTurretBlock::new,
            () -> copperMetal().requiresCorrectToolForDrops(), BlockItem::new);

    public static final BlockEntry<DieselEngineBlock> DIESEL_ENGINE = block("diesel_engine", DieselEngineBlock::new,
            () -> softMetal().mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops(), BlockItem::new);

    public static final BlockEntry<ModularDieselEngineBlock> MODULAR_DIESEL_ENGINE = block("large_diesel_engine", ModularDieselEngineBlock::new,
            () -> softMetal().mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops(), BlockItem::new);

    public static final BlockEntry<HugeDieselEngineBlock> HUGE_DIESEL_ENGINE = block("huge_diesel_engine", HugeDieselEngineBlock::new,
            () -> softMetal().mapColor(MapColor.COLOR_YELLOW).noOcclusion().requiresCorrectToolForDrops(), BlockItem::new);

    public static final BlockEntry<PoweredEngineShaftBlock> POWERED_ENGINE_SHAFT = block("powered_engine_shaft", PoweredEngineShaftBlock::new,
            () -> stone().mapColor(MapColor.METAL).requiresCorrectToolForDrops(), null);

    public static final BlockEntry<BasinLidBlock> BASIN_LID = block("basin_lid", BasinLidBlock::new,
            () -> softMetal().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops(), BlockItem::new);

    public static final BlockEntry<PumpjackBearingBlock> PUMPJACK_BEARING = block("pumpjack_bearing", PumpjackBearingBlock::new,
            () -> softMetal().mapColor(MapColor.GLOW_LICHEN).noOcclusion().requiresCorrectToolForDrops(), BlockItem::new);

    public static final BlockEntry<PumpjackHeadBlock> PUMPJACK_HEAD = block("pumpjack_head", PumpjackHeadBlock::new,
            () -> softMetal().mapColor(MapColor.GLOW_LICHEN).requiresCorrectToolForDrops(), BlockItem::new);

    public static final BlockEntry<PumpjackBearingBBlock> PUMPJACK_BEARING_B = block("pumpjack_bearing_b", PumpjackBearingBBlock::new,
            () -> softMetal().mapColor(MapColor.GLOW_LICHEN).requiresCorrectToolForDrops(), null);

    public static final BlockEntry<PumpjackHoleBlock> PUMPJACK_HOLE = block("pumpjack_hole", PumpjackHoleBlock::new,
            () -> copperMetal().requiresCorrectToolForDrops(), BlockItem::new);

    public static final BlockEntry<PumpjackCrankBlock> PUMPJACK_CRANK = block("pumpjack_crank", PumpjackCrankBlock::new,
            () -> softMetal().mapColor(MapColor.GLOW_LICHEN).requiresCorrectToolForDrops(), BlockItem::new);

    public static final BlockEntry<CanisterBlock> CANISTER = block("canister", CanisterBlock::new,
            () -> softMetal().mapColor(MapColor.METAL).requiresCorrectToolForDrops(), CanisterBlockItem::new);

    public static final BlockEntry<DistillationTankBlock> DISTILLATION_TANK = block("distillation_tank", DistillationTankBlock::new,
            () -> copperMetal().noOcclusion().isRedstoneConductor((p1, p2, p3) -> true).requiresCorrectToolForDrops(), null);

    public static final BlockEntry<BulkFermenterBlock> BULK_FERMENTER = block("bulk_fermenter", BulkFermenterBlock::new,
            () -> softMetal().mapColor(MapColor.METAL).isRedstoneConductor((p1, p2, p3) -> true).noOcclusion().requiresCorrectToolForDrops(), MultiBlockContainerBlockItem::new);

    public static final BlockEntry<OilBarrelBlock> OIL_BARREL = block("oil_barrel", OilBarrelBlock::new,
            () -> softMetal().mapColor(MapColor.METAL).isRedstoneConductor((p1, p2, p3) -> true).requiresCorrectToolForDrops(), MultiBlockContainerBlockItem::new);

    public static final BlockEntry<RotatedPillarBlock> CHIP_WOOD_BLOCK = block("chip_wood_block", RotatedPillarBlock::new,
            () -> Properties.ofFullCopy(Blocks.OAK_PLANKS), BlockItem::new);

    public static final BlockEntry<RotatedPillarBlock> CHIP_WOOD_BEAM = block("chip_wood_beam", RotatedPillarBlock::new,
            () -> Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG), BlockItem::new);

    public static final BlockEntry<SlabBlock> CHIP_WOOD_SLAB = block("chip_wood_slab", SlabBlock::new,
            () -> Properties.ofFullCopy(Blocks.OAK_SLAB), BlockItem::new);

    public static final BlockEntry<StairBlock> CHIP_WOOD_STAIRS = block("chip_wood_stairs", p -> new StairBlock(Blocks.ANDESITE_STAIRS.defaultBlockState(), p),
            () -> Properties.ofFullCopy(Blocks.OAK_STAIRS), BlockItem::new);

    public static final BlockEntry<Block> ASPHALT_BLOCK = block("asphalt_block", Block::new,
            () -> stone().mapColor(MapColor.COLOR_BLACK).speedFactor(1.25f).requiresCorrectToolForDrops(), BlockItem::new);

    public static final BlockEntry<SlabBlock> ASPHALT_SLAB = block("asphalt_slab", SlabBlock::new,
            () -> stone().mapColor(MapColor.COLOR_BLACK).speedFactor(1.25f).requiresCorrectToolForDrops(), BlockItem::new);

    public static final BlockEntry<StairBlock> ASPHALT_STAIRS = block("asphalt_stairs", p -> new StairBlock(Blocks.ANDESITE_STAIRS.defaultBlockState(), p),
            () -> stone().mapColor(MapColor.COLOR_BLACK).speedFactor(1.25f).requiresCorrectToolForDrops(), BlockItem::new);

    public static final BlockEntry<AndesiteGirderBlock> ANDESITE_GIRDER = block("andesite_girder", AndesiteGirderBlock::new,
            () -> softMetal().mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops(), BlockItem::new);

    public static final BlockEntry<AndesiteGirderEncasedShaftBlock> ANDESITE_GIRDER_ENCASED_SHAFT = block("andesite_girder_encased_shaft", AndesiteGirderEncasedShaftBlock::new,
            () -> softMetal().mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops(), null);

    public static final BlockEntry<SheetMetalPanelBlock> SHEET_METAL_PANEL = block("sheet_metal_panel", SheetMetalPanelBlock::new,
            () -> softMetal().mapColor(MapColor.COLOR_LIGHT_GRAY).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops(), BlockItem::new);

    public static final Map<DyeColor, BlockEntry<ConcreteEncasedFluidPipeBlock>> CONCRETE_ENCASED_FLUID_PIPES = new HashMap<>();
    static {
        for (DyeColor color : DyeColor.values()) {
            CONCRETE_ENCASED_FLUID_PIPES.put(color,
                    block(color.getName() + "_concrete_encased_fluid_pipe", ConcreteEncasedFluidPipeBlock::new,
                            () -> Properties.of().mapColor(color.getMapColor()).sound(SoundType.STONE).requiresCorrectToolForDrops(), null));
        }
    }

    private static <T extends Block> BlockEntry<T> block(
            String name,
            Function<BlockBehaviour.Properties, T> factory,
            Supplier<BlockBehaviour.Properties> properties,
            BiFunction<Block, Item.Properties, ? extends Item> itemFactory
    ) {
        Identifier id = CreateDieselGenerators.rl(name);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        T block = Registry.register(BuiltInRegistries.BLOCK, blockKey, factory.apply(properties.get().setId(blockKey)));
        if (itemFactory != null) {
            ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
            Item item = itemFactory.apply(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
            if (item instanceof BlockItem blockItem) {
                blockItem.registerBlocks(Item.BY_BLOCK, item);
            }
            Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        }
        return new BlockEntry<>(block);
    }

    public static void register() {
        BoilerHeater.REGISTRY.register(BURNER.get(), (level, pos, state) -> {
            if (level.getBlockEntity(pos) instanceof BurnerBlockEntity be)
                return state.getValue(BurnerBlock.LIT) ? Math.min(2, be.heat) : -1;
            return -1;
        });
        BlockStressValues.IMPACTS.register(CHEMICAL_TURRET.get(), () -> 4);
        MovementBehaviour.REGISTRY.register(DIESEL_ENGINE.get(), DIESEL_ENGINE_MOVEMENT);
        MovementBehaviour.REGISTRY.register(MODULAR_DIESEL_ENGINE.get(), MODULAR_DIESEL_ENGINE_MOVEMENT);
        MovementBehaviour.REGISTRY.register(PUMPJACK_HEAD.get(), PUMPJACK_HEAD_MOVEMENT);
        MovementBehaviour.REGISTRY.register(PUMPJACK_BEARING_B.get(), PUMPJACK_BEARING_B_MOVEMENT);
        MountedFluidStorageType.REGISTRY.register(OIL_BARREL.get(), CDGMountedStorageTypes.OIL_BARREL);
    }
}
