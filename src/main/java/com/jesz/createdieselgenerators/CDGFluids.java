package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.concrete.ConcreteBucketItem;
import com.jesz.createdieselgenerators.content.concrete.ConcreteFluid;
import com.jesz.createdieselgenerators.registry.entry.FluidEntry;
import com.zurrtum.create.AllFluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class CDGFluids {
    /** Create Fly counts fluids in droplets (81000 per bucket); the mod's numbers are millibuckets. */
    public static final int MB = 81;

    public static final List<FluidEntry> ALL = new ArrayList<>();

    public static final FluidEntry PLANT_OIL = register("plant_oil", new FluidEntry(2, 25, 3), BucketItem::new);
    public static final FluidEntry CRUDE_OIL = register("crude_oil", new FluidEntry(3, 25, 2), BucketItem::new);
    public static final FluidEntry BIODIESEL = register("biodiesel", new FluidEntry(2, 25, 3), BucketItem::new);
    public static final FluidEntry DIESEL = register("diesel", new FluidEntry(2, 25, 3), BucketItem::new);
    public static final FluidEntry GASOLINE = register("gasoline", new FluidEntry(2, 25, 3), BucketItem::new);
    public static final FluidEntry ETHANOL = register("ethanol", new FluidEntry(2, 25, 5), BucketItem::new);

    public static final FluidEntry[] CONCRETE = new FluidEntry[DyeColor.values().length];

    static {
        for (DyeColor color : DyeColor.values()) {
            CONCRETE[color.ordinal()] = register(color.getName() + "_cement", new FluidEntry(8, 12, 1) {
                @Override
                protected FlowableFluid createStill() {
                    return new ConcreteFluid(this, color);
                }
            }, (f, p) -> new ConcreteBucketItem(color, f, p));
        }
    }

    private static FluidEntry register(String name, FluidEntry entry, BiFunction<FlowableFluid, Item.Properties, BucketItem> bucketFactory) {
        Identifier id = CreateDieselGenerators.rl(name);
        Registry.register(BuiltInRegistries.FLUID, id, entry.still);
        Registry.register(BuiltInRegistries.FLUID, CreateDieselGenerators.rl("flowing_" + name), entry.flowing);

        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        entry.block = Registry.register(BuiltInRegistries.BLOCK, blockKey,
                new FluidBlock(entry.still, BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable().setId(blockKey)));

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, CreateDieselGenerators.rl(name + "_bucket"));
        entry.bucket = Registry.register(BuiltInRegistries.ITEM, itemKey,
                bucketFactory.apply(entry.still, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).setId(itemKey)));
        DispenserBlock.registerBehavior(entry.bucket, DISPENSE_FLUID);
        AllFluidItemInventory.ALL.put(entry.bucket, new AllFluidItemInventory.Entry(BucketFluidInventory::new));
        ALL.add(entry);
        return entry;
    }

    public static void register() {}

    // from Create

    private static final DispenseItemBehavior DEFAULT = new DefaultDispenseItemBehavior();
    private static final DispenseItemBehavior DISPENSE_FLUID = new DefaultDispenseItemBehavior() {
        @Override
        protected ItemStack execute(BlockSource pSource, ItemStack pStack) {
            DispensibleContainerItem dispensibleContainerItem = (DispensibleContainerItem) pStack.getItem();
            BlockPos pos = pSource.pos().relative(pSource.state().getValue(DispenserBlock.FACING));
            Level level = pSource.level();
            if (dispensibleContainerItem.emptyContents(null, level, pos, null)) {
                return new ItemStack(Items.BUCKET);
            }
            return DEFAULT.dispense(pSource, pStack);
        }
    };
}
