package com.jesz.createdieselgenerators.content.tools.hammer;

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

public record HammerRecipe(Ingredient ingredient, List<ProcessingOutput> results) implements CreateRecipe<SingleRecipeInput> {
    public static final MapCodec<HammerRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(HammerRecipe::ingredient),
            ProcessingOutput.CODEC.listOf(1, 4).fieldOf("results").forGetter(HammerRecipe::results)
    ).apply(instance, HammerRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, HammerRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, HammerRecipe::ingredient,
            ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), HammerRecipe::results,
            HammerRecipe::new
    );
    public static final RecipeSerializer<HammerRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

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
    public RecipeSerializer<HammerRecipe> getSerializer() {
        return CDGRecipes.HAMMERING.getSerializer();
    }

    @Override
    public RecipeType<HammerRecipe> getType() {
        return CDGRecipes.HAMMERING.getType();
    }
}
