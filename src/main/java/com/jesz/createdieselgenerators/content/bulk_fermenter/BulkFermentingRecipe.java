package com.jesz.createdieselgenerators.content.bulk_fermenter;

import com.jesz.createdieselgenerators.CDGRecipes;
import com.jesz.createdieselgenerators.fluid.FluidUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import com.zurrtum.create.foundation.recipe.TimedRecipe;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Items and fluids in, items and fluids out, inside a Bulk Fermenter. Fluid amounts are droplets (81 per mB).
 * Ingredients are listed one entry per consumed item, as in the original format.
 */
public record BulkFermentingRecipe(int time, List<ProcessingOutput> results, List<FluidStack> fluidResults,
                                   HeatCondition heat, List<FluidIngredient> fluidIngredients,
                                   List<Ingredient> ingredients) implements CreateRecipe<RecipeInput>, TimedRecipe {
    public static final MapCodec<BulkFermentingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("processing_time", 100).forGetter(BulkFermentingRecipe::time),
            ProcessingOutput.CODEC.listOf(0, 4).optionalFieldOf("results", List.of()).forGetter(BulkFermentingRecipe::results),
            FluidStack.CODEC.listOf(0, 2).optionalFieldOf("fluid_results", List.of()).forGetter(BulkFermentingRecipe::fluidResults),
            HeatCondition.CODEC.optionalFieldOf("heat_requirement", HeatCondition.NONE).forGetter(BulkFermentingRecipe::heat),
            FluidIngredient.CODEC.listOf(0, 2).optionalFieldOf("fluid_ingredients", List.of()).forGetter(BulkFermentingRecipe::fluidIngredients),
            Ingredient.CODEC.listOf(0, 9).optionalFieldOf("ingredients", List.of()).forGetter(BulkFermentingRecipe::ingredients)
    ).apply(instance, BulkFermentingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BulkFermentingRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, BulkFermentingRecipe::time,
            ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), BulkFermentingRecipe::results,
            FluidStack.PACKET_CODEC.apply(ByteBufCodecs.list()), BulkFermentingRecipe::fluidResults,
            HeatCondition.PACKET_CODEC, BulkFermentingRecipe::heat,
            FluidIngredient.PACKET_CODEC.apply(ByteBufCodecs.list()), BulkFermentingRecipe::fluidIngredients,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), BulkFermentingRecipe::ingredients,
            BulkFermentingRecipe::new
    );
    public static final RecipeSerializer<BulkFermentingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public int getProcessingDuration() {
        return time;
    }

    public HeatCondition getRequiredHeat() {
        return heat;
    }

    @Override
    public boolean matches(RecipeInput inventory, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input) {
        return ItemStack.EMPTY;
    }

    public boolean apply(BulkFermenterBlockEntity be, boolean test) {
        Container availableItems = be.itemHandler;

        if (availableItems == null ||
                !(be.fluidCapability instanceof BulkFermenterBlockEntity.BulkFermenterFluidHandler availableFluids))
            return false;


        BlazeBurnerBlock.HeatLevel heat = be.highestHeatLevel;
        if (!getRequiredHeat().testBlazeBurner(heat))
            return false;

        List<ItemStack> recipeOutputItems = new ArrayList<>();
        List<FluidStack> recipeOutputFluids = new ArrayList<>();

        for (boolean simulate : Iterate.trueAndFalse) {

            if (!simulate && test)
                return true;

            int[] extractedItemsFromSlot = new int[availableItems.getContainerSize()];
            int[] extractedFluidsFromTank = new int[availableFluids.size()];

            Ingredients:
            for (Ingredient ingredient : ingredients) {
                for (int slot = 0; slot < availableItems.getContainerSize(); slot++) {
                    ItemStack inSlot = availableItems.getItem(slot);
                    if (simulate && inSlot.getCount() <= extractedItemsFromSlot[slot])
                        continue;

                    if (inSlot.isEmpty() || !ingredient.test(inSlot))
                        continue;
                    if (!simulate)
                        availableItems.removeItem(slot, 1);
                    extractedItemsFromSlot[slot]++;
                    continue Ingredients;
                }

                return false;
            }

            boolean fluidsAffected = false;
            FluidIngredients:
            for (FluidIngredient fluidIngredient : fluidIngredients) {
                int amountRequired = fluidIngredient.amount();

                for (int tank = 0; tank < availableFluids.size(); tank++) {
                    FluidStack fluidStack = availableFluids.getStack(tank);
                    if (simulate && fluidStack.getAmount() <= extractedFluidsFromTank[tank])
                        continue;
                    if (fluidStack.isEmpty() || !fluidIngredient.test(fluidStack))
                        continue;
                    int drainedAmount = Math.min(amountRequired, fluidStack.getAmount());
                    if (!simulate) {
                        if (drainedAmount >= fluidStack.getAmount())
                            availableFluids.tanks.get(tank).setFluid(FluidStack.EMPTY);
                        else
                            fluidStack.setAmount(fluidStack.getAmount() - drainedAmount);
                        fluidsAffected = true;
                    }
                    amountRequired -= drainedAmount;
                    if (amountRequired != 0)
                        continue;
                    extractedFluidsFromTank[tank] += drainedAmount;
                    continue FluidIngredients;
                }

                return false;
            }

            if (fluidsAffected)
                be.onFluidStackChanged();

            if (simulate) {
                ProcessingOutput.rollOutput(be.getLevel().getRandom(), results, recipeOutputItems::add);

                for (FluidStack fluidStack : fluidResults)
                    if (!fluidStack.isEmpty())
                        recipeOutputFluids.add(fluidStack);
            }

            if (!applyOutputs(be, recipeOutputItems, recipeOutputFluids, simulate))
                return false;
        }

        return true;
    }

    private boolean applyOutputs(BulkFermenterBlockEntity be, List<ItemStack> outputItems, List<FluidStack> outputFluids, boolean test) {
        Container availableItems = be.itemHandler;

        if (availableItems == null || !(be.fluidCapability instanceof BulkFermenterBlockEntity.BulkFermenterFluidHandler availableFluids))
            return false;

        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < availableItems.getContainerSize(); i++)
            items.add(availableItems.getItem(i).copy());


        for (ProcessingOutput result : results) {
            ItemStack stack = result.create();

            int left = stack.getCount();
            for (ItemStack slot : items) {
                if (ItemStack.isSameItemSameComponents(slot, stack)) {
                    if ((availableItems.getMaxStackSize() - slot.getCount()) >= left) {
                        left = 0;
                        break;
                    } else
                        return false;
                }
            }

            if (left > 0) {
                for (ItemStack slot : items) {
                    if (slot.isEmpty()) {
                        left = 0;
                        break;
                    }
                }
                if (left > 0)
                    return false;
            }
        }

        boolean[] emptyTanksFilled = new boolean[availableFluids.size()];
        for (FluidStack result : fluidResults) {
            result = result.copy();

            boolean filled = false;
            for (FluidTank tank : availableFluids.tanks) {
                if (FluidUtil.isSameFluidSameComponents(tank.getFluid(), result)) {
                    if (FluidUtil.fill(tank, result, true) < result.getAmount())
                        return false;
                    else
                        filled = true;
                }
            }

            if (!filled) {
                List<FluidTank> tanks = availableFluids.tanks;
                for (int i = 0; i < tanks.size(); i++) {
                    FluidTank tank = tanks.get(i);

                    if (tank.getFluid().isEmpty() && !emptyTanksFilled[i]) {
                        if (FluidUtil.fill(tank, result, true) < result.getAmount())
                            return false;
                        else
                            emptyTanksFilled[i] = true;
                    }
                }
            }
        }

        if (test)
            return true;

        for (ItemStack stack : outputItems)
            be.insertStacked(stack.copy());

        for (FluidStack output : outputFluids)
            availableFluids.insert(output.copy());

        return true;
    }

    @Override
    public RecipeSerializer<BulkFermentingRecipe> getSerializer() {
        return CDGRecipes.BULK_FERMENTING.getSerializer();
    }

    @Override
    public RecipeType<BulkFermentingRecipe> getType() {
        return CDGRecipes.BULK_FERMENTING.getType();
    }
}
