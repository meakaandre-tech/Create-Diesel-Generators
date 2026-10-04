package com.jesz.createdieselgenerators.content.tools.wire_cutters;

import net.minecraft.util.Prediction;
import com.jesz.createdieselgenerators.CDGDataComponents;
import com.jesz.createdieselgenerators.CDGRecipes;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.infrastructure.component.SandPaperItemComponent;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class WireCuttersItem extends Item {
    /** Items that have a WireCuttingRecipe recipe; synced to clients, which do not know recipes themselves. */
    public static final ResourceKey<RecipePropertySet> INPUTS = CDGRecipes.propertySet("wire_cutting");

    public WireCuttersItem(Properties properties) {
        super(properties.stacksTo(1).durability(32));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return use(context.getLevel(), context.getPlayer(), context.getHand());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        InteractionHand otherHand = InteractionHand.values()[(~hand.ordinal()) & 1];
        ItemStack itemInHand = player.getItemInHand(hand);
        ItemStack itemInOtherHand = player.getItemInHand(otherHand);

        if (itemInHand.has(CDGDataComponents.PROCESSING_ITEM)) {
            player.startUsingItem(hand);
            return InteractionResult.PASS;
        }

        if (level.recipeAccess().propertySet(INPUTS).test(itemInOtherHand)) {
            ItemStack processingItem = itemInOtherHand.copy();
            itemInOtherHand.shrink(1);
            processingItem.setCount(1);

            itemInHand.set(CDGDataComponents.PROCESSING_ITEM,  new SandPaperItemComponent(processingItem));
            player.startUsingItem(hand);
            return InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player))
            return stack;
        if (!stack.has(CDGDataComponents.PROCESSING_ITEM))
            return stack;
        if (!(level instanceof ServerLevel sl))
            return stack;
        ItemStack processingItem = stack.get(CDGDataComponents.PROCESSING_ITEM).item();

        SingleRecipeInput inv = new SingleRecipeInput(processingItem);
        Optional<RecipeHolder<WireCuttingRecipe>> recipe = sl.recipeAccess().getRecipeFor(CDGRecipes.WIRE_CUTTING.getType(), inv, level);

        stack.remove(CDGDataComponents.PROCESSING_ITEM);

        if (recipe.isEmpty()) {
            player.getInventory().placeItemBackInInventory(processingItem, Prediction.SERVER_ONLY);
            return stack;
        }
        for (ItemStack result : recipe.get().value().rollResults(level.getRandom()))
            player.getInventory().placeItemBackInInventory(result, Prediction.SERVER_ONLY);
        stack.hurtAndBreak(1, entity, entity.getUsedItemHand().asEquipmentSlot());
        return stack;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int tick) {
        if (entity.getTicksUsingItem() % 10 == 0) {
            level.playLocalSound(entity.xo, entity.yo, entity.zo, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 0.3f, 1f, true);

            if (!stack.has(CDGDataComponents.PROCESSING_ITEM)) {
                super.onUseTick(level, entity, stack, tick);
                return;
            }

            ItemStack processingItem = stack.get(CDGDataComponents.PROCESSING_ITEM).item();
            if (!processingItem.isEmpty()) {
                ItemParticleOption option = new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(processingItem));
                for (int i = 0; i < 30; i++) {
                    Vec3 offset = VecHelper.offsetRandomly(entity.position().add(Math.sin(-entity.getYRot() / 180 * Math.PI) / 2, 1.3, Math.cos(-entity.getYRot() / 180 * Math.PI) / 2), level.getRandom(), .3f);
                    Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, level.getRandom(), .1f);

                    level.addParticle(option, offset.x(), offset.y(),
                            offset.z(), motion.x(), motion.y(), motion.z());
                }
            }
        }
        super.onUseTick(level, entity, stack, tick);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int tick) {
        if (!(entity instanceof Player player))
            return false;
        if (!stack.has(CDGDataComponents.PROCESSING_ITEM))
            return false;

        ItemStack processingItem = stack.get(CDGDataComponents.PROCESSING_ITEM).item();
        player.getInventory().placeItemBackInInventory(processingItem, Prediction.SERVER_ONLY);
        stack.remove(CDGDataComponents.PROCESSING_ITEM);
        return false;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 90;
    }
}
