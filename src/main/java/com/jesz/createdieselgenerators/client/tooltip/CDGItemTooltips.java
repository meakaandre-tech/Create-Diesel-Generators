package com.jesz.createdieselgenerators.client.tooltip;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CDGRegistries;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineTypes;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.zurrtum.create.AllFluids;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.lang.FontHelper;
import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.catnip.lang.LangBuilder;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.infrastructure.config.CKinetics;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;

import java.util.Arrays;
import java.util.List;

import static net.minecraft.ChatFormatting.DARK_GRAY;
import static net.minecraft.ChatFormatting.GRAY;

/** Fuel statistics on buckets (hold Alt) and the generated stress on engine items. */
public class CDGItemTooltips {
    public static void addToItemTooltip(ItemStack stack, List<Component> tooltip) {
        Minecraft mc = Minecraft.getInstance();
        if (!AllConfigs.client().tooltips.get())
            return;
        if (mc.player == null || mc.level == null)
            return;

        Item item = stack.getItem();
        if ((item instanceof BucketItem || item == Items.MILK_BUCKET) && CDGConfig.FUEL_TOOLTIPS.get() && !tooltip.isEmpty()) {
            Fluid fluid = AllFluids.MILK;
            if (item instanceof BucketItem bi)
                fluid = bi.content;

            FuelType type = FuelType.getTypeFor(mc.level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fluid);

            if (mc.hasAltDown() && type.normal().speed() != 0) {

                tooltip.add(1, Component.translatable("createdieselgenerators.tooltip.holdForFuelStats", Component.translatable("createdieselgenerators.tooltip.keyAlt").withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(2, Component.empty());

                byte enginesEnabled = (byte) ((EngineTypes.NORMAL.enabled() ? 1 : 0) + (EngineTypes.MODULAR.enabled() ? 1 : 0) + (EngineTypes.HUGE.enabled() ? 1 : 0));
                if (enginesEnabled == 0)
                    return;
                int currentEngineIndex = (AnimationTickHolder.getTicks() % (120)) / 20;
                List<EngineTypes> enabledEngines = Arrays.stream(EngineTypes.values()).filter(EngineTypes::enabled).toList();
                EngineTypes currentEngine = enabledEngines.get(currentEngineIndex % enginesEnabled);
                float currentSpeed = type.getGenerated(currentEngine).speed();
                float currentCapacity = type.getGenerated(currentEngine).strength();
                float currentBurn = type.getGenerated(currentEngine).burn();

                if (enginesEnabled != 1)
                    tooltip.add(3, Component.translatable("block.createdieselgenerators." +
                            (currentEngine == EngineTypes.MODULAR ? "large_" : currentEngine == EngineTypes.HUGE ? "huge_" : "") + "diesel_engine").withStyle(ChatFormatting.GRAY));
                tooltip.add(enginesEnabled != 1 ? 4 : 3, Component.translatable("createdieselgenerators.tooltip.fuelSpeed", CreateLang.number(currentSpeed).component().withStyle(FontHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(enginesEnabled != 1 ? 5 : 4, Component.translatable("createdieselgenerators.tooltip.fuelStress", CreateLang.number(currentCapacity).component().withStyle(FontHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(enginesEnabled != 1 ? 6 : 5, Component.translatable("createdieselgenerators.tooltip.fuelBurnRate", CreateLang.number(currentBurn * 20).component().withStyle(FontHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(enginesEnabled != 1 ? 7 : 6, Component.empty());
                tooltip.add(enginesEnabled != 1 ? 8 : 7, Component.translatable("createdieselgenerators.tooltip.burnerStrength", CreateLang.number(type.burnerStrength() * 100).text(" %").component().withStyle(FontHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(enginesEnabled != 1 ? 9 : 8, Component.empty());
            } else if (type.normal().speed() != 0) {
                tooltip.add(1, Component.translatable("createdieselgenerators.tooltip.holdForFuelStats", Component.translatable("createdieselgenerators.tooltip.keyAlt").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        CKinetics config = com.zurrtum.create.infrastructure.config.AllConfigs.server().kinetics;

        if (!(item instanceof BlockItem bi) ||
                !IRotate.StressImpact.isEnabled() ||
                !(CDGBlocks.DIESEL_ENGINE.is(bi) ||
                        CDGBlocks.MODULAR_DIESEL_ENGINE.is(bi) ||
                        CDGBlocks.HUGE_DIESEL_ENGINE.is(bi)))
            return;

        tooltip.add(Component.empty());

        int highestRPM = 0;
        int highestCapacity = 0;
        int highestStressCapacity = 0;

        for (var r : mc.level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE).listElements().toList()) {
            FuelType type = r.value();
            if (CDGBlocks.DIESEL_ENGINE.is(bi)) {
                highestRPM = (int) Math.max(highestRPM, type.normal().speed());
                highestCapacity = (int) Math.max(highestCapacity, type.normal().strength() / type.normal().speed());
                highestStressCapacity = (int) Math.max(highestStressCapacity, type.normal().strength());
            } else if (CDGBlocks.MODULAR_DIESEL_ENGINE.is(bi)) {
                highestRPM = (int) Math.max(highestRPM, type.modular().speed());
                highestCapacity = (int) Math.max(highestCapacity, type.modular().strength() / type.modular().speed());
                highestStressCapacity = (int) Math.max(highestStressCapacity, type.modular().strength());
            } else if (CDGBlocks.HUGE_DIESEL_ENGINE.is(bi)) {
                highestRPM = (int) Math.max(highestRPM, type.huge().speed());
                highestCapacity = (int) Math.max(highestCapacity, type.huge().strength() / type.huge().speed());
                highestStressCapacity = (int) Math.max(highestStressCapacity, type.huge().strength());
            }
        }
        boolean hasGoggles = GogglesItem.isWearingGoggles(mc.player);

        LangBuilder rpmUnit = CreateLang.translate("generic.unit.rpm");
        LangBuilder suUnit = CreateLang.translate("generic.unit.stress");

        CreateLang.translate("tooltip.capacityProvided")
                .style(GRAY)
                .addTo(tooltip);

        IRotate.StressImpact impactId = highestCapacity >= config.highCapacity.get() ? IRotate.StressImpact.HIGH
                : (highestCapacity >= config.mediumCapacity.get() ? IRotate.StressImpact.MEDIUM : IRotate.StressImpact.LOW);
        IRotate.StressImpact opposite = IRotate.StressImpact.values()[IRotate.StressImpact.values().length - 2 - impactId.ordinal()];
        LangBuilder builder = CreateLang.builder()
                .add(CreateLang.text(TooltipHelper.makeProgressBar(3, impactId.ordinal() + 1))
                        .style(opposite.getAbsoluteColor()));

        if (hasGoggles) {
            builder.add(CreateLang.number(highestCapacity))
                    .text("x ")
                    .add(rpmUnit)
                    .addTo(tooltip);
            LangBuilder amount = CreateLang.number(highestStressCapacity)
                    .add(suUnit);
            CreateLang.text(" -> ")
                    .add(CreateLang.translate("tooltip.up_to", amount))
                    .style(DARK_GRAY)
                    .addTo(tooltip);

        } else
            builder.translate("tooltip.capacityProvided." + Lang.asId(impactId.name()))
                    .addTo(tooltip);
    }
}
