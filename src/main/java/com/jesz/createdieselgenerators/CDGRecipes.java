package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.basin_lid.BasinFermentingRecipe;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermentingRecipe;
import com.jesz.createdieselgenerators.content.distillation.DistillationRecipe;
import com.jesz.createdieselgenerators.content.molds.CastingRecipe;
import com.jesz.createdieselgenerators.content.molds.CompressionMoldingRecipe;
import com.jesz.createdieselgenerators.content.tools.hammer.HammerRecipe;
import com.jesz.createdieselgenerators.content.tools.wire_cutters.WireCuttingRecipe;
import com.jesz.createdieselgenerators.content.tools.hammer.HammerItem;
import com.jesz.createdieselgenerators.content.tools.wire_cutters.WireCuttersItem;
import com.zurrtum.create.AllRecipeSets;
import com.zurrtum.create.foundation.recipe.RecipeFinder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipePropertySet;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum CDGRecipes {

    BASIN_FERMENTING(BasinFermentingRecipe.SERIALIZER),
    BULK_FERMENTING(BulkFermentingRecipe.SERIALIZER),
    DISTILLATION(DistillationRecipe.SERIALIZER),
    COMPRESSION_MOLDING(CompressionMoldingRecipe.SERIALIZER),
    CASTING(CastingRecipe.SERIALIZER),
    WIRE_CUTTING(WireCuttingRecipe.SERIALIZER),
    HAMMERING(HammerRecipe.SERIALIZER);

    private final Identifier id;
    private final RecipeSerializer<?> serializer;
    private final RecipeType<?> type;

    CDGRecipes(RecipeSerializer<?> serializer) {
        String name = name().toLowerCase(Locale.ROOT);
        id = CreateDieselGenerators.rl(name);
        this.serializer = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializer);
        this.type = Registry.register(BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<Recipe<?>>() {
            @Override
            public String toString() {
                return id.toString();
            }
        });
    }

    public static void register() {
        AllRecipeSets.ALL.put(HammerItem.INPUTS, recipe -> recipe instanceof HammerRecipe r ? Optional.of(r.ingredient()) : Optional.empty());
        AllRecipeSets.ALL.put(WireCuttersItem.INPUTS, recipe -> recipe instanceof WireCuttingRecipe r ? Optional.of(r.ingredient()) : Optional.empty());
    }

    public static ResourceKey<RecipePropertySet> propertySet(String name) {
        return ResourceKey.create(RecipePropertySet.TYPE_KEY, CreateDieselGenerators.rl(name));
    }

    public Identifier getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    public <T extends RecipeSerializer<?>> T getSerializer() {
        return (T) serializer;
    }

    @SuppressWarnings("unchecked")
    public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
        return (RecipeType<R>) type;
    }

    /** All loaded recipes of this type. Recipes only exist on the server in this game version. */
    @SuppressWarnings("unchecked")
    public <R extends Recipe<?>> List<RecipeHolder<R>> getAll(ServerLevel level) {
        List<RecipeHolder<R>> list = new ArrayList<>();
        for (RecipeHolder<?> holder : RecipeFinder.get(this, level, r -> r.value().getType() == type))
            list.add((RecipeHolder<R>) holder);
        return list;
    }
}
