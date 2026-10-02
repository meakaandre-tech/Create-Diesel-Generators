package com.jesz.createdieselgenerators.client.tooltip;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterBlockEntity;
import com.jesz.createdieselgenerators.content.burner.BurnerBlockEntity;
import com.jesz.createdieselgenerators.content.canister.CanisterBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.PoweredEngineShaftBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.distillation.DistillationTankBlockEntity;
import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelBlockEntity;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackHoleBlockEntity;
import com.jesz.createdieselgenerators.content.turret.ChemicalTurretBlockEntity;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.api.goggles.IHaveHoveringInformation;
import com.zurrtum.create.client.catnip.lang.FontHelper;
import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.catnip.lang.LangBuilder;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.GeneratingKineticTooltipBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Goggle and hover overlays of the mod's block entities. Create Fly keeps these in client-side
 * behaviours instead of on the block entity.
 */
public class CDGTooltips {

    public static class Burner extends KineticTooltipBehaviour<BurnerBlockEntity> {
        public Burner(BurnerBlockEntity be) {
            super(be);
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            return containedFluidTooltip(tooltip, isPlayerSneaking, blockEntity.tank);
        }
    }

    public static class Canister extends TooltipBehaviour<CanisterBlockEntity> implements IHaveGoggleInformation {
        public Canister(CanisterBlockEntity be) {
            super(be);
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            return containedFluidTooltip(tooltip, isPlayerSneaking, blockEntity.tank);
        }
    }

    public static class ChemicalTurret extends KineticTooltipBehaviour<ChemicalTurretBlockEntity> {
        public ChemicalTurret(ChemicalTurretBlockEntity be) {
            super(be);
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            super.addToGoggleTooltip(tooltip, isPlayerSneaking);
            return containedFluidTooltip(tooltip, isPlayerSneaking, blockEntity.tank.getCapability());
        }
    }

    public static class DieselEngine extends GeneratingKineticTooltipBehaviour<DieselEngineBlockEntity> {
        public DieselEngine(DieselEngineBlockEntity be) {
            super(be);
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            if (blockEntity.getGeneratedSpeed() != 0)
                super.addToGoggleTooltip(tooltip, isPlayerSneaking);
            containedFluidTooltip(tooltip, isPlayerSneaking, blockEntity.tank.getCapability());
            return true;
        }
    }

    public static class ModularDieselEngine extends GeneratingKineticTooltipBehaviour<ModularDieselEngineBlockEntity> {
        public ModularDieselEngine(ModularDieselEngineBlockEntity be) {
            super(be);
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            if (!blockEntity.isController()) {
                ModularDieselEngineBlockEntity controller = blockEntity.getControllerBE();
                if (controller == null)
                    return false;
                ModularDieselEngine other = controller.getBehaviour(TYPE) instanceof ModularDieselEngine m ? m : null;
                if (other == null)
                    return false;
                return other.addToGoggleTooltip(tooltip, isPlayerSneaking);
            }
            if (blockEntity.getGeneratedSpeed() != 0)
                super.addToGoggleTooltip(tooltip, isPlayerSneaking);
            return containedFluidTooltip(tooltip, isPlayerSneaking, blockEntity.fluidCapability);
        }
    }

    public static class HugeDieselEngine extends TooltipBehaviour<HugeDieselEngineBlockEntity> implements IHaveGoggleInformation {
        public HugeDieselEngine(HugeDieselEngineBlockEntity be) {
            super(be);
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            HugeDieselEngineBlockEntity be = blockEntity;
            if (be.overStressed) {
                CreateLang.translate("gui.stressometer.overstressed")
                        .style(ChatFormatting.GOLD)
                        .forGoggles(tooltip);
                Component hint = CreateLang.translateDirect("gui.contraptions.network_overstressed");
                List<Component> cutString = TooltipHelper.cutTextComponent(hint, FontHelper.Palette.GRAY_AND_WHITE);
                for (Component component : cutString)
                    CreateLang.builder().add(component.copy()).forGoggles(tooltip);
                return containedFluidTooltip(tooltip, isPlayerSneaking, be.tank.getCapability());
            }

            if (IRotate.StressImpact.isEnabled() && be.enabled() && be.getThrottle() > 0) {
                PoweredEngineShaftBlockEntity shaft = be.getShaft();
                if (shaft != null) {
                    float stressBase = be.upgrade.getCapacity(be.getFuelCapacity(), be) *
                            be.upgrade.getSpeed(be.getFuelSpeed(), be) * be.getThrottle();
                    if (!Mth.equal(stressBase, 0)) {
                        CreateLang.translate("gui.goggles.generator_stats").forGoggles(tooltip);
                        CreateLang.translate("tooltip.capacityProvided")
                                .style(ChatFormatting.GRAY).forGoggles(tooltip);
                        CreateLang.number(Math.abs(stressBase))
                                .translate("generic.unit.stress")
                                .style(ChatFormatting.AQUA)
                                .space()
                                .add(CreateLang.translate("gui.goggles.at_current_speed")
                                        .style(ChatFormatting.DARK_GRAY))
                                .forGoggles(tooltip, 1);
                    }
                }
            }

            return containedFluidTooltip(tooltip, isPlayerSneaking, be.tank.getCapability());
        }
    }

    public static class OilBarrel extends TooltipBehaviour<OilBarrelBlockEntity> implements IHaveGoggleInformation {
        public OilBarrel(OilBarrelBlockEntity be) {
            super(be);
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            OilBarrelBlockEntity controllerBE = blockEntity.getControllerBE();
            if (controllerBE == null)
                return false;
            return containedFluidTooltip(tooltip, isPlayerSneaking, controllerBE.tankInventory);
        }
    }

    public static class DistillationTank extends TooltipBehaviour<DistillationTankBlockEntity> implements IHaveGoggleInformation, IHaveHoveringInformation {
        public DistillationTank(DistillationTankBlockEntity be) {
            super(be);
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            DistillationTankBlockEntity controllerBE = blockEntity.getControllerBE();
            if (controllerBE == null)
                return false;
            return containedFluidTooltip(tooltip, isPlayerSneaking, controllerBE.tankInventory);
        }

        @Override
        public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            return addHint(blockEntity, tooltip, 0);
        }

        private static boolean addHint(DistillationTankBlockEntity be, List<Component> tooltip, int depth) {
            if (depth > 64 || be.getLevel() == null)
                return false;
            if (!be.isController()) {
                DistillationTankBlockEntity controller = be.getControllerBE();
                if (controller == null)
                    return false;
                return addHint(controller, tooltip, depth + 1);
            }

            DistillationTankBlockEntity bottomBe = be.getLevel().getBlockEntity(be.getBlockPos().below(), CDGBlockEntityTypes.DISTILLATION_TANK.get()).orElse(null);

            if (bottomBe != null && bottomBe.getWidth() == be.getWidth() && bottomBe.getController().equals(be.getController().below()))
                return addHint(bottomBe, tooltip, depth + 1);

            // the recipe itself is only known to the server; a running recipe shows as synced progress
            if (be.processingTime < 0 || !be.tanksFull)
                return false;

            Lang.builder(CreateDieselGenerators.ID)
                    .translate("hint.distiller_full.title")
                    .style(ChatFormatting.GOLD)
                    .forGoggles(tooltip);
            Component hint =
                    Lang.builder(CreateDieselGenerators.ID)
                            .translate("hint.distiller_full")
                            .component();
            List<Component> cutComponent = TooltipHelper.cutTextComponent(hint, FontHelper.Palette.GRAY_AND_WHITE);
            for (Component component : cutComponent)
                CreateLang.builder().add(component).forGoggles(tooltip);
            return true;
        }
    }

    public static class PumpjackHole extends TooltipBehaviour<PumpjackHoleBlockEntity> implements IHaveGoggleInformation, IHaveHoveringInformation {
        public PumpjackHole(PumpjackHoleBlockEntity be) {
            super(be);
        }

        @Override
        public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            if (blockEntity.valid)
                return false;

            Lang.builder(CreateDieselGenerators.ID).translate("hint.pumpjack_hole_no_pipe.title").style(ChatFormatting.GOLD).forGoggles(tooltip);
            Component hint = Lang.builder(CreateDieselGenerators.ID).translate("hint.pumpjack_hole_no_pipe").component();
            List<Component> cutComponent = TooltipHelper.cutTextComponent(hint, FontHelper.Palette.GRAY_AND_WHITE);
            for (Component component : cutComponent)
                CreateLang.builder().add(component).forGoggles(tooltip);
            return true;
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            if (!blockEntity.valid || !blockEntity.started)
                return false;
            if (blockEntity.oilAmount == Integer.MAX_VALUE) {
                TooltipHelper.addHint(tooltip, "hint.hose_pulley");
                return true;
            }

            CreateLang.builder().add(Component.translatable("createdieselgenerators.goggle.oil_amount")).style(ChatFormatting.GRAY).forGoggles(tooltip);
            CreateLang.text(String.format("%,d", blockEntity.oilAmount)).add(CreateLang.translate("generic.unit.millibuckets")).style(ChatFormatting.GOLD).forGoggles(tooltip);

            return true;
        }
    }

    public static class BulkFermenter extends TooltipBehaviour<BulkFermenterBlockEntity> implements IHaveGoggleInformation {
        public BulkFermenter(BulkFermenterBlockEntity be) {
            super(be);
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            BulkFermenterBlockEntity controller = blockEntity.getControllerBE();

            if (controller == null)
                return false;

            controller.initCapability();
            if (controller.fluidCapability == null)
                controller.refreshCapability();
            Container items = controller.itemHandler;
            FluidInventory fluids = controller.fluidCapability;

            if (items == null || fluids == null)
                return false;

            boolean isEmpty = true;

            CreateLang.translate("gui.goggles.basin_contents")
                    .forGoggles(tooltip);

            Map<Item, Integer> allItems = new HashMap<>();
            for (int i = 0; i < items.getContainerSize(); i++) {
                ItemStack stackInSlot = items.getItem(i);
                if (stackInSlot.isEmpty())
                    continue;
                if (allItems.containsKey(stackInSlot.getItem()))
                    allItems.replace(stackInSlot.getItem(), stackInSlot.getCount() + allItems.get(stackInSlot.getItem()));
                else
                    allItems.put(stackInSlot.getItem(), stackInSlot.getCount());
                isEmpty = false;
            }

            for (Map.Entry<Item, Integer> e : allItems.entrySet()) {
                CreateLang.text("")
                        .add(Component.translatable(e.getKey().getDescriptionId())
                                .withStyle(ChatFormatting.GRAY))
                        .add(CreateLang.text(" x" + e.getValue())
                                .style(ChatFormatting.GREEN))
                        .forGoggles(tooltip, 1);
            }

            LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
            for (int i = 0; i < fluids.size(); i++) {
                FluidStack fluidStack = fluids.getStack(i);
                if (fluidStack.isEmpty())
                    continue;
                CreateLang.text("")
                        .add(CreateLang.fluidName(fluidStack)
                                .add(CreateLang.text(" "))
                                .style(ChatFormatting.GRAY)
                                .add(CreateLang.number((double) fluidStack.getAmount() / 81)
                                        .add(mb)
                                        .style(ChatFormatting.BLUE)))
                        .forGoggles(tooltip, 1);
                isEmpty = false;
            }

            if (isEmpty)
                tooltip.remove(0);

            return true;
        }
    }
}
