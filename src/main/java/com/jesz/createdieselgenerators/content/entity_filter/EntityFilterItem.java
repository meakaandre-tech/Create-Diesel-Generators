package com.jesz.createdieselgenerators.content.entity_filter;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;

import com.jesz.createdieselgenerators.CDGDataComponents;
import com.jesz.createdieselgenerators.CDGMenuTypes;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.logistics.filter.AttributeFilterMenu;
import com.zurrtum.create.infrastructure.component.AttributeFilterWhitelistMode;
import com.zurrtum.create.content.logistics.filter.FilterItem;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntityFilterItem extends Item implements MenuProvider {
    public EntityFilterItem(Properties properties) {
        super(properties);
    }
    public static List<EntityAttribute.EntityAttributeEntry> getEntries(ItemStack stack){
        return stack.getOrDefault(CDGDataComponents.ENTITY_FILTER_MATCHED_ATTRIBUTES, Collections.emptyList());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        if (AllClientHandle.INSTANCE.shiftDown())
            return;
        List<Component> makeSummary = makeSummary(stack);
        if (makeSummary.isEmpty())
            return;
        tooltipComponents.accept(CommonComponents.SPACE);
        makeSummary.forEach(tooltipComponents);
    }

    private List<Component> makeSummary(ItemStack stack) {
        List<Component> list = new ArrayList<>();

        AttributeFilterWhitelistMode whitelistMode = stack.get(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE);

        list.add((whitelistMode == AttributeFilterWhitelistMode.WHITELIST_CONJ
                ? Component.translatable("create.gui.attribute_filter.allow_list_conjunctive")
                : whitelistMode == AttributeFilterWhitelistMode.WHITELIST_DISJ
                ? Component.translatable("create.gui.attribute_filter.allow_list_disjunctive")
                : Component.translatable("create.gui.attribute_filter.deny_list")).withStyle(ChatFormatting.GOLD));

        int count = 0;

        for (EntityAttribute.EntityAttributeEntry entry : getEntries(stack)) {


            if (count > 5) {
                list.add(Component.literal("- ...")
                        .withStyle(ChatFormatting.DARK_GRAY));
                break;
            }
            list.add(Component.literal("- ")
                    .append(entry.attribute().format(entry.inverted())));
            count++;
        }

        if (count == 0)
            return Collections.emptyList();


        return list;
    }

    public static boolean test(ItemStack stack, Entity entity) {

        AttributeFilterWhitelistMode whitelistMode = stack.getOrDefault(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, AttributeFilterWhitelistMode.WHITELIST_DISJ);
        AtomicBoolean passed = new AtomicBoolean(false);
        if (whitelistMode == AttributeFilterWhitelistMode.WHITELIST_CONJ)
            passed.set(true);
        getEntries(stack).forEach((entry) -> {
            boolean currentAttributePassed = entry.attribute().test(entity) ^ entry.inverted();
            if (whitelistMode != AttributeFilterWhitelistMode.WHITELIST_CONJ)
                if (!passed.get())
                    passed.set(currentAttributePassed);
            if (whitelistMode == AttributeFilterWhitelistMode.WHITELIST_CONJ)
                passed.set(currentAttributePassed && passed.get());
        });
        return passed.get()^whitelistMode == AttributeFilterWhitelistMode.BLACKLIST;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown() || hand != InteractionHand.MAIN_HAND)
            return InteractionResult.PASS;
        if (level.isClientSide() || !(player instanceof ServerPlayer sp))
            return InteractionResult.SUCCESS;

        openHandledScreen(sp);
        return InteractionResult.SUCCESS;
    }

    @Override
    public MenuBase<?> createMenu(int id, Inventory inv, Player player, RegistryFriendlyByteBuf extraData) {
        ItemStack heldItem = player.getMainHandItem();
        ItemStack.STREAM_CODEC.encode(extraData, heldItem);
        return new EntityFilterMenu(id, inv, heldItem);
    }

    @Override
    public Component getDisplayName() {
        return components().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY);
    }
}
