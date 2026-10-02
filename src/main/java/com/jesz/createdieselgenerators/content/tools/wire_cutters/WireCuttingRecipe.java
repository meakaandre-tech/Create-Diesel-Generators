package com.jesz.createdieselgenerators.content.tools.wire_cutters;

import com.jesz.createdieselgenerators.CDGRecipes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public record WireCuttingRecipe(Ingredient ingredient, List<ProcessingOutput> results) implements CreateRecipe<SingleRecipeInput> {
    public static final MapCodec<WireCuttingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(WireCuttingRecipe::ingredient),
            ProcessingOutput.CODEC.listOf(1, 4).fieldOf("results").forGetter(WireCuttingRecipe::results)
    ).apply(instance, WireCuttingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, WireCuttingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, WireCuttingRecipe::ingredient,
            ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), WireCuttingRecipe::results,
            WireCuttingRecipe::new
    );
    public static final RecipeSerializer<WireCuttingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public List<ItemStack> rollResults(RandomSource random) {
        List<ItemStack> list = new ArrayList<>();
        ProcessingOutput.rollOutput(random, results, list::add);
        return list;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return results.getFirst().create();
    }

    @Override
    public RecipeSerializer<WireCuttingRecipe> getSerializer() {
        return CDGRecipes.WIRE_CUTTING.getSerializer();
    }

    @Override
    public RecipeType<WireCuttingRecipe> getType() {
        return CDGRecipes.WIRE_CUTTING.getType();
    }
}
