package com.jesz.createdieselgenerators.content.entity_filter;

import net.minecraft.util.Prediction;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.logistics.filter.FilterItem;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Server half of the turret's entity filter slot. The value box and its rendering are attached on the client
 * with Create Fly's FilteringBehaviour.
 */
public class EntityFilteringBehaviour extends ServerFilteringBehaviour {

    public EntityFilteringBehaviour(SmartBlockEntity be) {
        super(be);
    }

    @Override
    public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
        Level level = getLevel();
        BlockPos pos = getPos();
        ItemStack itemInHand = player.getItemInHand(hand);
        ItemStack toApply = itemInHand.copy();

        if (!canShortInteract(toApply))
            return;
        if (level.isClientSide())
            return;

        ItemStack filter = getFilter(side);
        if (filter.getItem() instanceof EntityFilterItem) {
            Inventory inventory = player.getInventory();
            if (!player.isCreative() || inventory.count(filter, 1) == 0)
                inventory.placeItemBackInInventory(filter.copy(), Prediction.SERVER_ONLY);
        }

        if (toApply.getItem() instanceof EntityFilterItem)
            toApply.setCount(1);

        if (!setFilter(side, toApply)) {
            player.sendOverlayMessage(Component.translatable("create.logistics.filter.invalid_item"));
            AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
            return;
        }

        if (!player.isCreative()) {
            if (toApply.getItem() instanceof EntityFilterItem) {
                if (itemInHand.getCount() == 1)
                    player.setItemInHand(hand, ItemStack.EMPTY);
                else
                    itemInHand.shrink(1);
            }
        }

        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
    }

    @Override
    public boolean setFilter(ItemStack stack) {
        if(stack.getItem() instanceof FilterItem)
            return false;
        if(stack.getItem() instanceof EntityFilterItem || stack.isEmpty())
            return super.setFilter(stack);
        return false;
    }

    @Override
    public void destroy() {
        if (getFilter().getItem() instanceof EntityFilterItem) {
            Vec3 pos = VecHelper.getCenterOf(getPos());
            Level level = getLevel();
            level.addFreshEntity(new ItemEntity(level, pos.x, pos.y, pos.z, getFilter().copy()));
        }
        super.destroy();
    }
}
