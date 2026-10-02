package com.jesz.createdieselgenerators.content.basin_lid;

import com.jesz.createdieselgenerators.CDGRecipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.zurrtum.create.content.processing.basin.BasinInput;
import com.zurrtum.create.content.processing.basin.BasinRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.recipe.TimedRecipe;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public record BasinFermentingRecipe(int time, List<ProcessingOutput> results, List<FluidStack> fluidResults, HeatCondition heat,
                           List<FluidIngredient> fluidIngredients,
                           List<SizedIngredient> ingredients) implements BasinRecipe, TimedRecipe {
    public static final MapCodec<BasinFermentingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec((Instance<BasinFermentingRecipe> instance) -> instance.group(
        Codec.INT.optionalFieldOf("processing_time", 100).forGetter(BasinFermentingRecipe::time),
        ProcessingOutput.CODEC.listOf(1, 4).optionalFieldOf("results", List.of()).forGetter(BasinFermentingRecipe::results),
        FluidStack.CODEC.listOf(1, 2).optionalFieldOf("fluid_results", List.of()).forGetter(BasinFermentingRecipe::fluidResults),
        HeatCondition.CODEC.optionalFieldOf("heat_requirement", HeatCondition.NONE).forGetter(BasinFermentingRecipe::heat),
        FluidIngredient.CODEC.listOf(1, 2).optionalFieldOf("fluid_ingredients", List.of())
            .forGetter(BasinFermentingRecipe::fluidIngredients),
        SizedIngredient.LIST_CODEC.optionalFieldOf("ingredients", List.of()).forGetter(BasinFermentingRecipe::ingredients)
    ).apply(instance, BasinFermentingRecipe::new)).validate(recipe -> {
        if (recipe.results.isEmpty() && recipe.fluidResults.isEmpty()) {
            return DataResult.error(() -> "BasinFermentingRecipe must have a result or a fluid result");
        }
        if (recipe.fluidIngredients.isEmpty() && recipe.ingredients.isEmpty()) {
            return DataResult.error(() -> "BasinFermentingRecipe must have a ingredient or a fluid ingredient");
        }
        if (recipe.ingredients.size() > 9) {
            return DataResult.error(() -> "Ingredients type is too many: " + recipe.ingredients.size() + ", expected range [0-9]");
        }
        return DataResult.success(recipe);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, BasinFermentingRecipe> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        BasinFermentingRecipe::time,
        ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()),
        BasinFermentingRecipe::results,
        FluidStack.PACKET_CODEC.apply(ByteBufCodecs.list()),
        BasinFermentingRecipe::fluidResults,
        HeatCondition.PACKET_CODEC,
        BasinFermentingRecipe::heat,
        FluidIngredient.PACKET_CODEC.apply(ByteBufCodecs.list()),
        BasinFermentingRecipe::fluidIngredients,
        SizedIngredient.PACKET_CODEC.apply(ByteBufCodecs.list()),
        BasinFermentingRecipe::ingredients,
        BasinFermentingRecipe::new
    );
    public static final RecipeSerializer<BasinFermentingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public int getIngredientSize() {
        return fluidIngredients.size() + ingredients().size();
    }

    @Override
    public boolean matches(BasinInput input, Level world) {
        if (!heat.testBlazeBurner(input.heat())) {
            return false;
        }
        ServerFilteringBehaviour filter = input.filter();
        if (filter == null) {
            return false;
        }
        if (results.isEmpty()) {
            if (!filter.test(fluidResults.getFirst())) {
                return false;
            }
        } else if (!filter.test(results.getFirst().create())) {
            return false;
        }
        List<ItemStack> outputs = BasinRecipe.tryCraft(input, ingredients);
        if (outputs == null) {
            return false;
        }
        if (!BasinRecipe.matchFluidIngredient(input, fluidIngredients)) {
            return false;
        }
        ProcessingOutput.rollOutput(input.random(), results, outputs::add);
        return input.acceptOutputs(outputs, fluidResults, true);
    }

    @Override
    public boolean apply(BasinInput input) {
        if (!heat.testBlazeBurner(input.heat())) {
            return false;
        }
        Deque<Runnable> changes = new ArrayDeque<>();
        List<ItemStack> outputs = BasinRecipe.prepareCraft(input, ingredients, changes);
        if (outputs == null) {
            return false;
        }
        if (!BasinRecipe.prepareFluidCraft(input, fluidIngredients, changes)) {
            return false;
        }
        ProcessingOutput.rollOutput(input.random(), results, outputs::add);
        if (!input.acceptOutputs(outputs, fluidResults, true)) {
            return false;
        }
        changes.forEach(Runnable::run);
        return input.acceptOutputs(outputs, fluidResults, false);
    }

    @Override
    public RecipeSerializer<BasinFermentingRecipe> getSerializer() {
        return CDGRecipes.BASIN_FERMENTING.getSerializer();
    }

    @Override
    public RecipeType<BasinFermentingRecipe> getType() {
        return CDGRecipes.BASIN_FERMENTING.getType();
    }
}
