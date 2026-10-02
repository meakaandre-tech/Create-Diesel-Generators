package com.jesz.createdieselgenerators.content.molds;

import com.jesz.createdieselgenerators.CDGRecipes;
import com.zurrtum.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.zurrtum.create.content.fluids.spout.SpoutBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.List;

public class BasinSpoutCasting implements BlockSpoutingBehaviour {
    @Override
    public int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
        BasinBlockEntity basin;
        if (level.getBlockEntity(pos) instanceof BasinBlockEntity be)
            basin = be;
        else
            return 0;
        if (!(level instanceof ServerLevel serverLevel))
            return 0;

        List<RecipeHolder<CastingRecipe>> all = CDGRecipes.CASTING.getAll(serverLevel);
        List<CastingRecipe> recipes = all.stream()
                .map(RecipeHolder::value)
                .filter(cr -> cr.matches(basin, availableFluid) && cr.fluidIngredient().amount() <= availableFluid.getAmount())
                .toList();

        if (recipes.isEmpty())
            return 0;

        CastingRecipe recipe = recipes.get(0);
        return recipe.execute(basin, simulate);
    }
}
