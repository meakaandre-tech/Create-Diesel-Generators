package com.jesz.createdieselgenerators.content.bulk_fermenter;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGRecipes;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.recipe.RecipeFinder;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.jesz.createdieselgenerators.CDGFluids;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.items.CombinedInvWrapper;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.foundation.fluid.FluidTank;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BulkFermenterBlockEntity extends SmartBlockEntity implements IMultiBlockEntityContainerFluidItem {

    private static final int MAX_SIZE = 3;
    public Container itemHandler;
    public ItemStackHandler inventory;
    public FluidInventory fluidCapability;
    BulkFermenterFluidHandler tankInventory;
    BlockPos controller;
    BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    protected boolean updateCapability;
    int width = 1;
    int height = 1;

    private static final int SYNC_RATE = 8;
    int syncCooldown;
    boolean queuedSync;

    public int processingTime = -1;
    BulkFermentingRecipe currentRecipe;

    public boolean packagerMode;

    BlazeBurnerBlock.HeatLevel highestHeatLevel = BlazeBurnerBlock.HeatLevel.NONE;
    public BulkFermenterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tankInventory = createInventory();
        updateConnectivity = false;
        updateCapability = false;
        inventory = new ItemStackHandler(5) {
            @Override
            public void setChanged() {
                if (level == null)
                    return;

                List<Recipe<?>> r = getMatchingRecipes();
                if (!r.contains(currentRecipe)) {
                    processingTime = -1;
                }
                if (processingTime == -1 && !r.isEmpty()) {
                    currentRecipe = (BulkFermentingRecipe) r.get(0);
                    startProcessing();
                }

                if (!level.isClientSide()) {
                    BulkFermenterBlockEntity.this.setChanged();
                    sendData();
                }
            }
        };
        refreshCapability();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {

    }

    protected BulkFermenterFluidHandler createInventory() {
        return new BulkFermenterFluidHandler(6, getCapacityMultiplier(), this::onFluidStackChanged);
    }

    public void updateConnectivity() {
        assert level != null;
        updateConnectivity = false;
        if (level.isClientSide())
            return;
        if (!isController())
            return;
        ConnectivityHandler.formMulti(this);
    }

    private void startProcessing() {
        if(currentRecipe == null)
            return;
        processingTime = (currentRecipe.getProcessingDuration());
        sendData();
    }
    @Override
    public void tick() {
        assert level != null;

        if (isController()) {
            if (processingTime >= 0) {
                if (!level.isClientSide() && processingTime % 20 == 0 && new Random().nextInt() % 4 == 0)
                    level.playSound(null, worldPosition.offset(width / 2, height/2, width / 2), SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT,
                        SoundSource.BLOCKS, .15f, .75f);

                if (processingTime == 1)
                    level.playSound(null, worldPosition.offset(width / 2, height / 2, width / 2), SoundEvents.BREWING_STAND_BREW,
                            SoundSource.BLOCKS, .15f, .75f);

                if (currentRecipe == null) {
                    List<Recipe<?>> r = getMatchingRecipes();
                    if (r.isEmpty())
                        processingTime = -1;
                    else
                        currentRecipe = (BulkFermentingRecipe) r.get(0);
                } else {
                   if (processingTime == 0 && !level.isClientSide()) {
                       for (int i = 0; i < width * width; i++) {
                            if (!currentRecipe.apply(this, true))
                                break;
                           currentRecipe.apply(this, false);
                       }

                       processingTime = -1;
                   } else {
                       processingTime = (int) Math.max(0, processingTime - Math.sqrt(width * height));
                   }
                }
            }
            if (processingTime == -1) {
                if (currentRecipe != null) {
                    List<Recipe<?>> r = getMatchingRecipes();
                    if (!r.contains(currentRecipe)) {
                        processingTime = -1;
                    }
                    currentRecipe = null;

                    if (processingTime == -1 && !r.isEmpty()) {
                        currentRecipe = (BulkFermentingRecipe) r.get(0);
                        startProcessing();
                    }

                    if (!level.isClientSide()) {
                        setChanged();
                        sendData();
                    }
                }
            }
        }
        super.tick();
        if (syncCooldown > 0) {
            syncCooldown--;
            if (syncCooldown == 0 && queuedSync) {
                sendData();
            }
        }

        if (lastKnownPos == null)
            lastKnownPos = getBlockPos();
        else if (!lastKnownPos.equals(worldPosition)) {
            onPositionChanged();
            return;
        }

        if (updateConnectivity)
            updateConnectivity();

        if (updateCapability) {
            updateCapability = false;
            refreshCapability();
        }
    }

    protected List<Recipe<?>> getMatchingRecipes() {
        // recipes only exist on the server
        if (!(level instanceof ServerLevel serverLevel))
            return new ArrayList<>();
        initCapability();
        List<RecipeHolder<? extends Recipe<?>>> list = RecipeFinder.get(RECIPE_CACHE_KEY, serverLevel, recipe -> recipe.value().getType() == CDGRecipes.BULK_FERMENTING.getType());
        return list.stream()
                .map(RecipeHolder::value)
                .sorted((r1, r2) -> {
                    if (r1 instanceof BulkFermentingRecipe recipe1 && r2 instanceof BulkFermentingRecipe recipe2)
                        return recipe2.getRequiredHeat().ordinal() - recipe1.getRequiredHeat().ordinal();
                    return 0;
                })
                .filter(r -> r instanceof BulkFermentingRecipe fr && fr.apply(this, true))
                .collect(Collectors.toList());

    }

    /** Adds a recipe result to the multiblock's item slots. */
    public void insertStacked(ItemStack stack) {
        initCapability();
        if (itemHandler != null)
            itemHandler.insert(stack, stack.getCount(), null);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        Containers.dropContents(level, pos, inventory);
        level.removeBlockEntity(pos);
        ConnectivityHandler.splitMulti(this);
    }

    static final Object RECIPE_CACHE_KEY = new Object();

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public boolean isController() {
        return controller == null || worldPosition.getX() == controller.getX()
                && worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
    }

    @Override
    public void initialize() {
        super.initialize();
        sendData();
    }

    private void onPositionChanged() {
        removeController(true);
        lastKnownPos = worldPosition;
    }

    protected void onFluidStackChanged() {
        assert level != null;
        if (!hasLevel())
            return;

        List<Recipe<?>> r = getMatchingRecipes();
        if (!r.contains(currentRecipe)) {
            processingTime = -1;
        }
        if (processingTime == -1 && !r.isEmpty()) {
            currentRecipe = (BulkFermentingRecipe) r.get(0);
            startProcessing();
        }


        for (int yOffset = 0; yOffset < height; yOffset++) {
            for (int xOffset = 0; xOffset < width; xOffset++) {
                for (int zOffset = 0; zOffset < width; zOffset++) {
                    BlockPos pos = this.worldPosition.offset(xOffset, yOffset, zOffset);
                    BulkFermenterBlockEntity tankAt = ConnectivityHandler.partAt(getType(), level, pos);
                    if (tankAt == null)
                        continue;
                    level.updateNeighbourForOutputSignal(pos, tankAt.getBlockState()
                            .getBlock());
                }
            }
        }

        if (!level.isClientSide()) {
            setChanged();
            sendData();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public BulkFermenterBlockEntity getControllerBE() {
        assert level != null;
        if (isController())
            return this;
        BlockEntity blockEntity = level.getBlockEntity(controller);
        if (blockEntity instanceof BulkFermenterBlockEntity)
            return (BulkFermenterBlockEntity) blockEntity;
        return null;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        if (isController())
            return super.createRenderBoundingBox().expandTowards(width - 1, 0, width - 1);
        else
            return super.createRenderBoundingBox();
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public void applyFluidTankSize(int blocks) {
        tankInventory.setCapacity(blocks * getCapacityMultiplier());
    }

    @Override
    public void removeController(boolean keepContents) {
        assert level != null;
        if (level.isClientSide())
            return;
        updateConnectivity = true;
        if (!keepContents)
            applyFluidTankSize(1);
        controller = null;
        width = 1;
        height = 1;

        onFluidStackChanged();
        refreshCapability();
        setChanged();
        sendData();
    }

    @Override
    public void sendData() {
        if (syncCooldown > 0) {
            queuedSync = true;
            return;
        }
        super.sendData();
        queuedSync = false;
        syncCooldown = SYNC_RATE;
    }

    @Override
    public void setController(BlockPos controller) {
        assert level != null;
        if (level.isClientSide() && !isVirtual())
            return;
        if (controller.equals(this.controller))
            return;
        this.controller = controller;
        refreshCapability();
        setChanged();
        sendData();
    }

    public void refreshCapability() {
        fluidCapability = handlerForCapability();
    }

    public void initCapability() {
        assert level != null;
        if (!isController()) {
            BulkFermenterBlockEntity controllerBE = getControllerBE();
            if (controllerBE == null)
                return;
            controllerBE.initCapability();
            itemHandler = controllerBE.itemHandler;
            return;
        }

        Container[] inventories = new Container[height * width * width];
        for (int yOffset = 0; yOffset < height; yOffset++) {
            for (int xOffset = 0; xOffset < width; xOffset++) {
                for (int zOffset = 0; zOffset < width; zOffset++) {
                    BlockPos vaultPos = worldPosition.offset(xOffset, yOffset, zOffset);
                    BulkFermenterBlockEntity tankAt =
                            ConnectivityHandler.partAt(CDGBlockEntityTypes.BULK_FERMENTER.get(), level, vaultPos);
                    inventories[yOffset * width * width + xOffset * width + zOffset] =
                            tankAt != null ? tankAt.inventory : new ItemStackHandler();
                }
            }
        }

        itemHandler = new CombinedInvWrapper(inventories) {
            @Override
            public int insert(ItemStack stack, int maxAmount) {
                return insert(stack, maxAmount, null);
            }

            @Override
            public int insert(ItemStack stack, int maxAmount, @Nullable Direction side) {
                if (stack.isEmpty() || maxAmount <= 0)
                    return 0;

                for (int i = 0; i < getContainerSize(); i++) {
                    ItemStack existing = getItem(i);
                    if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack)) {
                        int limit = Math.min(stack.getMaxStackSize(), getMaxStackSize());
                        int space = limit - existing.getCount();
                        if (space <= 0) {
                            if (!packagerMode)
                                return 0;
                            continue;
                        }

                        int inserted = Math.min(maxAmount, space);
                        existing.grow(inserted);
                        setChanged();
                        return inserted;
                    }
                }

                for (int i = 0; i < getContainerSize(); i++) {
                    if (getItem(i).isEmpty()) {
                        int inserted = Math.min(maxAmount, Math.min(stack.getMaxStackSize(), getMaxStackSize()));
                        setItem(i, stack.copyWithCount(inserted));
                        setChanged();
                        return inserted;
                    }
                }

                return 0;
            }
        };
    }

    private FluidInventory handlerForCapability() {
        return isController() ?
                tankInventory :
                getControllerBE() != null ? getControllerBE().handlerForCapability() : new BulkFermenterFluidHandler(0, 0, () -> {});
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    protected void read(ValueInput tag, boolean clientPacket) {
        super.read(tag, clientPacket);

        BlockPos controllerBefore = controller;
        int prevSize = width;
        int prevHeight = height;

        updateConnectivity = tag.getBooleanOr("Uninitialized", false);
        lastKnownPos = tag.read("LastKnownPos", BlockPos.CODEC).orElse(null);
        controller = tag.read("Controller", BlockPos.CODEC).orElse(null);

        if (isController()) {
            width = tag.getIntOr("Size", 1);
            height = tag.getIntOr("Height", 1);
            highestHeatLevel = BlazeBurnerBlock.HeatLevel.values()[tag.getIntOr("Heat", 0)];
            tankInventory.setCapacity(getTotalTankSize() * getCapacityMultiplier());
            tankInventory.readFrom(tag, "TankContent");

            processingTime = tag.getIntOr("ProcessingTime", -1);
        }

        inventory.read(tag);
        itemHandler = null;

        updateCapability = true;

        if (!clientPacket)
            return;

        boolean changeOfController =
                !Objects.equals(controllerBefore, controller);

        if (hasLevel() && (changeOfController || prevSize != width || prevHeight != height)) {
            level.setBlocksDirty(getBlockPos(), Blocks.AIR.defaultBlockState(), getBlockState());

            if (isController()) {
                tankInventory.setCapacity(getCapacityMultiplier() * getTotalTankSize());
                invalidateRenderBoundingBox();
            }
        }

    }

    @Override
    protected void write(ValueOutput tag, boolean clientPacket) {
        super.write(tag, clientPacket);

        if (updateConnectivity)
            tag.putBoolean("Uninitialized", true);
        if (lastKnownPos != null)
            tag.store("LastKnownPos", BlockPos.CODEC, lastKnownPos);
        if (!isController())
            tag.store("Controller", BlockPos.CODEC, controller);
        if (isController()) {
            tankInventory.writeTo(tag, "TankContent");
            tag.putInt("Size", width);
            tag.putInt("Height", height);
            tag.putInt("ProcessingTime", processingTime);
            tag.putInt("Heat", highestHeatLevel.ordinal());
        }
        inventory.write(tag);

        if (!clientPacket)
            return;

        if (queuedSync)
            tag.putBoolean("LazySync", true);
    }

    public int getTotalTankSize() {
        return width * width * height;
    }

    public static int getCapacityMultiplier() {
        return 1000 * CDGFluids.MB;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        onFluidStackChanged();
        setChanged();
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return Direction.Axis.Y;
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        return AllConfigs.server().fluids.fluidTankCapacity.get();
    }

    @Override
    public int getMaxWidth() {
        return MAX_SIZE;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public boolean hasTank() {
        return true;
    }

    @Override
    public int getTankSize(int tank) {
        return getCapacityMultiplier();
    }

    @Override
    public void setTankSize(int tank, int blocks) {
        applyFluidTankSize(blocks);
    }

    @Override
    public FluidTank getTank(int tank) {
        return tankInventory.tanks.get(tank);
    }

    @Override
    public FluidStack getFluid(int tank) {
        return tankInventory.getStack(tank)
                .copy();
    }

    public BulkFermentingRecipe getRecipe() {
        return currentRecipe;
    }

    public void updateHeat() {
        assert level != null;
        BulkFermenterBlockEntity controller = getControllerBE();
        int width;
        if (controller == null)
            width = 1;
        else {
            if (controller != this) {
                controller.updateHeat();
                return;
            }
            width = controller.width;
        }

        BlazeBurnerBlock.HeatLevel highestHeat = BlazeBurnerBlock.HeatLevel.NONE;

        for (int xOffset = 0; xOffset < width; xOffset++) {
            for (int zOffset = 0; zOffset < width; zOffset++) {
                BlockPos pos = getController().offset(xOffset, -1, zOffset);
                BlockState blockState = level.getBlockState(pos);
                BlazeBurnerBlock.HeatLevel heat = BasinBlockEntity.getHeatLevelOf(blockState);
                if(!highestHeat.isAtLeast(heat))
                    highestHeat = heat;
            }
        }
        highestHeatLevel = highestHeat;

        List<Recipe<?>> r = getMatchingRecipes();
        if (!r.contains(currentRecipe)) {
            processingTime = -1;
        }
        if (processingTime == -1 && !r.isEmpty()) {
            currentRecipe = (BulkFermentingRecipe) r.get(0);
            startProcessing();
        }

        if (!level.isClientSide()) {
            setChanged();
            sendData();
        }
    }

    /** Six independent tanks; a fluid only goes into the tank already holding it, otherwise into the first empty one. */
    public static class BulkFermenterFluidHandler implements FluidInventory {
        int tankCount;
        public NonNullList<FluidTank> tanks = NonNullList.create();
        private final Runnable updateCallback;
        private int capacity;

        public BulkFermenterFluidHandler(int tankCount, int capacity, Runnable updateCallback) {
            this.updateCallback = updateCallback;
            this.capacity = capacity;
            for (int i = 0; i < tankCount; i++)
                tanks.add(new FluidTank(capacity));

            this.tankCount = tankCount;
        }

        @Override
        public int size() {
            return tankCount;
        }

        @Override
        public FluidStack getStack(int slot) {
            if (slot < 0 || slot >= tankCount)
                return FluidStack.EMPTY;
            return tanks.get(slot).getFluid();
        }

        @Override
        public void setStack(int slot, FluidStack stack) {
            if (slot < 0 || slot >= tankCount)
                return;
            tanks.get(slot).setFluid(stack);
        }

        @Override
        public int getMaxAmountPerStack() {
            return capacity;
        }

        @Override
        public FluidStack onExtract(FluidStack stack) {
            return removeMaxSize(stack, Optional.of(capacity));
        }

        @Override
        public void markDirty() {
            updateCallback.run();
        }

        @Override
        public int insert(FluidStack resource, int maxAmount) {
            if (resource.isEmpty() || maxAmount <= 0)
                return 0;
            for (FluidTank tank : tanks) {
                if (!tank.getFluid().isEmpty() && FluidStack.areFluidsAndComponentsEqualIgnoreCapacity(tank.getFluid(), resource)) {
                    int result = tank.insert(resource, maxAmount);
                    if (result > 0)
                        markDirty();
                    return result;
                }
            }

            for (FluidTank tank : tanks) {
                if (tank.getFluid().isEmpty()) {
                    int result = tank.insert(resource, maxAmount);
                    if (result > 0)
                        markDirty();
                    return result;
                }
            }
            return 0;
        }

        @Override
        public int countSpace(FluidStack resource, int maxAmount) {
            if (resource.isEmpty() || maxAmount <= 0)
                return 0;
            for (FluidTank tank : tanks)
                if (!tank.getFluid().isEmpty() && FluidStack.areFluidsAndComponentsEqualIgnoreCapacity(tank.getFluid(), resource))
                    return Math.min(maxAmount, Math.max(0, capacity - tank.getFluid().getAmount()));
            for (FluidTank tank : tanks)
                if (tank.getFluid().isEmpty())
                    return Math.min(maxAmount, capacity);
            return 0;
        }

        public void writeTo(ValueOutput view, String key) {
            ValueOutput.ValueOutputList list = view.child(key).childrenList("Tanks");
            for (FluidTank tank : tanks)
                tank.write(list.addChild());
        }

        public void readFrom(ValueInput view, String key) {
            int i = 0;
            for (ValueInput child : view.childOrEmpty(key).childrenListOrEmpty("Tanks")) {
                if (i >= tanks.size())
                    break;
                tanks.get(i++).read(child);
            }
            for (; i < tanks.size(); i++)
                tanks.get(i).setFluid(FluidStack.EMPTY);
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
            for (FluidTank tank : tanks)
                tank.setCapacity(capacity);
        }
    }
}
