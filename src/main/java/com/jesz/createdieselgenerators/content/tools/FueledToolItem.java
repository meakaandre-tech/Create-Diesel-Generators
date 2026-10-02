package com.jesz.createdieselgenerators.content.tools;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CDGDataComponents;
import com.jesz.createdieselgenerators.CDGFluids;
import com.jesz.createdieselgenerators.fluid.SimpleFluidContent;
import com.zurrtum.create.AllEnchantments;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.Consumer;

/**
 * Items holding fluid in the fluid_contents component. Config values are millibuckets;
 * everything returned from here is in droplets (81 per millibucket), like the rest of Create Fly.
 */
public interface FueledToolItem {

    default int getBaseCapacity(ItemStack stack){
        return CDGConfig.TOOL_CAPACITY.get();
    }

    default int getCapacityEnchantmentAddition(ItemStack stack){
        return CDGConfig.TOOL_CAPACITY_ENCHANTMENT.get();
    }

    default int getCapacity(ItemStack stack){
        int enchantmentLevel = 0;
        if (stack.has(DataComponents.ENCHANTMENTS))
            for (Object2IntMap.Entry<Holder<Enchantment>> enchantment : stack.get(DataComponents.ENCHANTMENTS).entrySet())
                if (enchantment.getKey().is(AllEnchantments.CAPACITY))
                    enchantmentLevel = enchantment.getIntValue();

        return (getBaseCapacity(stack) + (getCapacityEnchantmentAddition(stack) * enchantmentLevel)) * CDGFluids.MB;
    }

    default FluidStack readFluid(ItemStack stack){
        SimpleFluidContent content = stack.get(CDGDataComponents.FLUID_CONTENTS);
        return content != null ? content.copy() : FluidStack.EMPTY;
    }

    default void writeFluid(ItemStack stack, FluidStack fluid){
        stack.set(CDGDataComponents.FLUID_CONTENTS, SimpleFluidContent.copyOf(fluid));
    }

    default int getCurrentFillLevel(ItemStack stack){
        return readFluid(stack).getAmount();
    }

    default void createTooltip(Consumer<Component> tooltip, ItemStack stack){
        if(stack.has(CDGDataComponents.FLUID_CONTENTS)) {
            FluidStack fluid = readFluid(stack);
            if(fluid.isEmpty()){
                tooltip.accept(Component.translatable("createdieselgenerators.tooltip.empty").withStyle(ChatFormatting.GRAY));
                return;
            }
            tooltip.accept(fluid.getName().copy()
                    .withStyle(ChatFormatting.GRAY)
                    .append(" ")
                    .append(Component.literal(String.format("%,d", fluid.getAmount() / CDGFluids.MB)).withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("create.generic.unit.millibuckets").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" / "))
                    .append(Component.literal(String.format("%,d", getCapacity(stack) / CDGFluids.MB)).withStyle(ChatFormatting.GRAY))
                    .append(Component.translatable("create.generic.unit.millibuckets").withStyle(ChatFormatting.GRAY)));
            return;
        }
        tooltip.accept(Component.translatable("createdieselgenerators.tooltip.empty").withStyle(ChatFormatting.GRAY));
    }
}
