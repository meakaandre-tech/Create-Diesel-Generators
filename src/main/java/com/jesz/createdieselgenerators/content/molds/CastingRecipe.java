package com.jesz.createdieselgenerators.content.molds;

import com.jesz.createdieselgenerators.CDGRecipes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * A spout pours a fluid into a basin holding a mold. Amounts are droplets (81 per mB).
 */
public record CastingRecipe(FluidIngredient fluidIngredient, Identifier mold,
                            List<ProcessingOutput> results) implements CreateRecipe<RecipeInput> {
    public static final MapCodec<CastingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FluidIngredient.CODEC.fieldOf("fluid_ingredient").forGetter(CastingRecipe::fluidIngredient),
            Identifier.CODEC.fieldOf("mold").forGetter(CastingRecipe::mold),
            ProcessingOutput.CODEC.listOf(1, 1).fieldOf("results").forGetter(CastingRecipe::results)
    ).apply(instance, CastingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastingRecipe> STREAM_CODEC = StreamCodec.composite(
            FluidIngredient.PACKET_CODEC, CastingRecipe::fluidIngredient,
            Identifier.STREAM_CODEC, CastingRecipe::mold,
            ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), CastingRecipe::results,
            CastingRecipe::new
    );
    public static final RecipeSerializer<CastingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public MoldType moldType() {
        return MoldType.findById(mold);
    }

    public List<FluidIngredient> getFluidIngredients() {
        return List.of(fluidIngredient);
    }

    private boolean hasMold(BasinBlockEntity basin) {
        MoldType moldType = moldType();
        if (moldType == null)
            return false;
        Container availableItems = basin.itemCapability;
        if (availableItems == null)
            return false;

        for (int i = 0; i < availableItems.getContainerSize(); i++) {
            ItemStack stack = availableItems.getItem(i);

            if (stack.getItem() instanceof MoldItem && MoldItem.getMold(stack) == moldType)
                return true;
        }
        return false;
    }

    public boolean matches(BasinBlockEntity basin, FluidStack fluidStack) {
        if (!hasMold(basin))
            return false;

        return fluidIngredient.test(fluidStack);
    }

    public int execute(BasinBlockEntity basin, boolean simulate) {
        if (!hasMold(basin))
            return 0;

        List<ItemStack> recipeOutputItems = new ArrayList<>();

        if (!simulate)
            ProcessingOutput.rollOutput(basin.getLevel().getRandom(), results, recipeOutputItems::add);

        if (!basin.acceptOutputs(recipeOutputItems, List.of(), false))
            return 0;

        return fluidIngredient.amount();
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<CastingRecipe> getSerializer() {
        return CDGRecipes.CASTING.getSerializer();
    }

    @Override
    public RecipeType<CastingRecipe> getType() {
        return CDGRecipes.CASTING.getType();
    }
}
