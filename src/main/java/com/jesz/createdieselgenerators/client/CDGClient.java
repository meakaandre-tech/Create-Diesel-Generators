package com.jesz.createdieselgenerators.client;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CDGFluids;
import com.jesz.createdieselgenerators.ClientHooks;
import com.jesz.createdieselgenerators.CDGEntityTypes;
import com.jesz.createdieselgenerators.CDGMenuTypes;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.client.gui.EntityFilterScreen;
import com.jesz.createdieselgenerators.client.gui.TrackLayersBagClientComponent;
import com.jesz.createdieselgenerators.client.handler.AndesiteGirderWrenchHandler;
import com.jesz.createdieselgenerators.client.handler.TrackLayersBagPlacementClient;
import com.jesz.createdieselgenerators.client.tooltip.CDGItemTooltips;
import com.jesz.createdieselgenerators.content.track_layers_bag.TrackLayersBagComponent;
import com.jesz.createdieselgenerators.client.ponder.CDGPonderPlugin;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import com.jesz.createdieselgenerators.CDGItems;
import com.zurrtum.create.client.AllExtensions;
import net.minecraft.client.model.HumanoidModel;
import com.zurrtum.create.client.AllItemTooltips;
import com.zurrtum.create.client.AllMenuScreens;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import com.jesz.createdieselgenerators.client.ct.BulkFermenterCTBehavior;
import com.jesz.createdieselgenerators.client.ct.ModularDieselEngineCTBehavior;
import com.jesz.createdieselgenerators.client.ct.OilBarrelCTBehavior;
import com.jesz.createdieselgenerators.client.model.ChemicalSprayerModel;
import com.jesz.createdieselgenerators.client.model.DistillationTankModel;
import com.jesz.createdieselgenerators.client.model.ProcessingToolModel;
import com.jesz.createdieselgenerators.client.model.SheetMetalPanelModel;
import com.jesz.createdieselgenerators.client.render.BasinLidRenderer;
import com.jesz.createdieselgenerators.client.render.BulkFermenterRenderer;
import com.jesz.createdieselgenerators.client.render.BurnerRenderer;
import com.jesz.createdieselgenerators.client.render.CanisterRenderer;
import com.jesz.createdieselgenerators.client.render.ChemicalTurretRenderer;
import com.jesz.createdieselgenerators.client.render.DieselEngineRenderer;
import com.jesz.createdieselgenerators.client.render.DistillationTankRenderer;
import com.jesz.createdieselgenerators.client.render.HugeDieselEngineRenderer;
import com.jesz.createdieselgenerators.client.render.ModularDieselEngineRenderer;
import com.jesz.createdieselgenerators.client.render.PumpjackBearingRenderer;
import com.jesz.createdieselgenerators.client.render.PumpjackCrankRenderer;
import com.jesz.createdieselgenerators.client.render.PumpjackHeadMovementRender;
import com.jesz.createdieselgenerators.client.render.PumpjackHoleRenderer;
import com.jesz.createdieselgenerators.client.scroll.CDGScrollBehaviours;
import com.jesz.createdieselgenerators.client.sound.CDGClientSounds;
import com.jesz.createdieselgenerators.client.tooltip.CDGTooltips;
import com.jesz.createdieselgenerators.registry.entry.FluidEntry;
import com.zurrtum.create.client.AllBlockEntityBehaviours;
import com.zurrtum.create.client.AllBlockEntityRenders;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.AllModels;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.fluids.FluidFX;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.ShaftRenderer;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.audio.KineticAudioBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.GeneratingKineticTooltipBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import com.zurrtum.create.client.infrastructure.model.CTModel;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.rl;

/**
 * Client entry point for the Fabric port: everything Registrate and the NeoForge client events
 * used to hook up (renderers, models, tooltips, value boxes, sounds, particles).
 */
public class CDGClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CDGConfig.loadClient();
        CDGPartialModels.init();
        CDGSpriteShifts.init();

        registerFluids();
        registerModels();
        registerRenderers();
        registerBehaviours();
        registerHooks();
        registerEvents();
    }

    private static void registerEvents() {
        EntityRenderers.register(CDGEntityTypes.CHEMICAL_SPRAYER_PROJECTILE.get(), NoopRenderer::new);
        AllMenuScreens.register(CDGMenuTypes.ENTITY_FILTER, EntityFilterScreen::create);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player == null || mc.level == null)
                return;
            AndesiteGirderWrenchHandler.tick(mc);
            TrackLayersBagPlacementClient.clientTick(mc);
        });
        ClientTooltipComponentCallback.EVENT.register(data ->
                data instanceof TrackLayersBagComponent component ? new TrackLayersBagClientComponent(component) : null);
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> CDGItemTooltips.addToItemTooltip(stack, lines));

        PonderIndex.addPlugin(new CDGPonderPlugin());

        AllExtensions.ARM_POSE.put(CDGItems.CHEMICAL_SPRAYER.get(), HumanoidModel.ArmPose.CROSSBOW_HOLD);
        AllExtensions.ARM_POSE.put(CDGItems.CHEMICAL_SPRAYER_LIGHTER.get(), HumanoidModel.ArmPose.CROSSBOW_HOLD);

        // item descriptions (hold Shift) and kinetic stats, as Registrate attached to every item of the mod
        for (Item item : BuiltInRegistries.ITEM)
            if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(CreateDieselGenerators.ID))
                AllItemTooltips.register(item);
    }

    private static void fluid(FluidEntry entry, Identifier still, Identifier flow) {
        AllFluidConfigs.MODEL.put(entry.still, new FluidModel.Unbaked(new Material(still), new Material(flow), null, null));
    }

    private static void registerFluids() {
        fluid(CDGFluids.PLANT_OIL, rl("block/fluid/plant_oil_still"), rl("block/fluid/plant_oil_flow"));
        fluid(CDGFluids.CRUDE_OIL, rl("block/crude_oil_still"), rl("block/crude_oil_flow"));
        fluid(CDGFluids.BIODIESEL, rl("block/biodiesel_still"), rl("block/biodiesel_flow"));
        fluid(CDGFluids.DIESEL, rl("block/diesel_still"), rl("block/diesel_flow"));
        fluid(CDGFluids.GASOLINE, rl("block/gasoline_still"), rl("block/gasoline_flow"));
        fluid(CDGFluids.ETHANOL, rl("block/fluid/ethanol_still"), rl("block/fluid/ethanol_flow"));
        for (DyeColor color : DyeColor.values())
            fluid(CDGFluids.CONCRETE[color.ordinal()], rl("block/cement/" + color.getName() + "_still"),
                    rl("block/cement/" + color.getName() + "_flow"));
    }

    private static void registerModels() {
        AllModels.register(ChemicalSprayerModel.ID, ChemicalSprayerModel.Unbaked.CODEC);
        AllModels.register(ProcessingToolModel.ID, ProcessingToolModel.Unbaked.CODEC);
        AllModels.register(CDGBlocks.MODULAR_DIESEL_ENGINE.get(), CTModel.of(new ModularDieselEngineCTBehavior()));
        AllModels.register(CDGBlocks.BULK_FERMENTER.get(), CTModel.of(new BulkFermenterCTBehavior()));
        AllModels.register(CDGBlocks.OIL_BARREL.get(), CTModel.of(new OilBarrelCTBehavior()));
        AllModels.register(CDGBlocks.DISTILLATION_TANK.get(), DistillationTankModel::new);
        AllModels.register(CDGBlocks.SHEET_METAL_PANEL.get(), SheetMetalPanelModel::new);
    }

    private static void registerRenderers() {
        BlockEntityRenderers.register(CDGBlockEntityTypes.BURNER.get(), BurnerRenderer::new);
        BlockEntityRenderers.register(CDGBlockEntityTypes.CHEMICAL_TURRET.get(), ChemicalTurretRenderer::new);
        BlockEntityRenderers.register(CDGBlockEntityTypes.DIESEL_ENGINE.get(), DieselEngineRenderer::new);
        BlockEntityRenderers.register(CDGBlockEntityTypes.MODULAR_DIESEL_ENGINE.get(), ModularDieselEngineRenderer::new);
        BlockEntityRenderers.register(CDGBlockEntityTypes.HUGE_DIESEL_ENGINE.get(), HugeDieselEngineRenderer::new);
        AllBlockEntityRenders.visual(CDGBlockEntityTypes.POWERED_ENGINE_SHAFT.get(), KineticBlockEntityRenderer::new,
                SingleAxisRotatingVisual.of(AllPartialModels.POWERED_SHAFT));
        BlockEntityRenderers.register(CDGBlockEntityTypes.BASIN_LID.get(), BasinLidRenderer::new);
        BlockEntityRenderers.register(CDGBlockEntityTypes.PUMPJACK_BEARING.get(), PumpjackBearingRenderer::new);
        BlockEntityRenderers.register(CDGBlockEntityTypes.CANISTER.get(), CanisterRenderer::new);
        BlockEntityRenderers.register(CDGBlockEntityTypes.DISTILLATION_TANK.get(), DistillationTankRenderer::new);
        BlockEntityRenderers.register(CDGBlockEntityTypes.BULK_FERMENTER.get(), BulkFermenterRenderer::new);
        BlockEntityRenderers.register(CDGBlockEntityTypes.PUMPJACK_HOLE.get(), PumpjackHoleRenderer::new);
        BlockEntityRenderers.register(CDGBlockEntityTypes.PUMPJACK_CRANK.get(), PumpjackCrankRenderer::new);
        AllBlockEntityRenders.visual(CDGBlockEntityTypes.ENCASED_GIRDER.get(), ShaftRenderer::new, SingleAxisRotatingVisual::shaft);

        CDGBlocks.PUMPJACK_HEAD_MOVEMENT.attachRender = new PumpjackHeadMovementRender();
    }

    private static void registerBehaviours() {
        AllBlockEntityBehaviours.add(CDGBlockEntityTypes.BURNER.get(), KineticAudioBehaviour::new, CDGTooltips.Burner::new);
        AllBlockEntityBehaviours.add(CDGBlockEntityTypes.CHEMICAL_TURRET.get(), KineticAudioBehaviour::new,
                CDGTooltips.ChemicalTurret::new, CDGScrollBehaviours::turretFilter);
        AllBlockEntityBehaviours.add(CDGBlockEntityTypes.DIESEL_ENGINE.get(), KineticAudioBehaviour::new,
                CDGTooltips.DieselEngine::new, CDGScrollBehaviours::engine);
        AllBlockEntityBehaviours.add(CDGBlockEntityTypes.MODULAR_DIESEL_ENGINE.get(), KineticAudioBehaviour::new,
                CDGTooltips.ModularDieselEngine::new, CDGScrollBehaviours::modularEngine);
        AllBlockEntityBehaviours.add(CDGBlockEntityTypes.HUGE_DIESEL_ENGINE.get(),
                CDGTooltips.HugeDieselEngine::new, CDGScrollBehaviours::hugeEngine);
        AllBlockEntityBehaviours.add(CDGBlockEntityTypes.POWERED_ENGINE_SHAFT.get(), KineticAudioBehaviour::new,
                GeneratingKineticTooltipBehaviour::new);
        AllBlockEntityBehaviours.add(CDGBlockEntityTypes.BASIN_LID.get(), KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        AllBlockEntityBehaviours.add(CDGBlockEntityTypes.PUMPJACK_BEARING.get(), KineticAudioBehaviour::new);
        AllBlockEntityBehaviours.add(CDGBlockEntityTypes.CANISTER.get(), CDGTooltips.Canister::new);
        AllBlockEntityBehaviours.add(CDGBlockEntityTypes.DISTILLATION_TANK.get(), CDGTooltips.DistillationTank::new);
        AllBlockEntityBehaviours.add(CDGBlockEntityTypes.BULK_FERMENTER.get(), CDGTooltips.BulkFermenter::new);
        AllBlockEntityBehaviours.add(CDGBlockEntityTypes.OIL_BARREL.get(), CDGTooltips.OilBarrel::new);
        AllBlockEntityBehaviours.add(CDGBlockEntityTypes.PUMPJACK_HOLE.get(), CDGTooltips.PumpjackHole::new);
        AllBlockEntityBehaviours.add(CDGBlockEntityTypes.PUMPJACK_CRANK.get(), KineticAudioBehaviour::new,
                KineticTooltipBehaviour::new, CDGScrollBehaviours::crank);
        AllBlockEntityBehaviours.add(CDGBlockEntityTypes.ENCASED_GIRDER.get(), KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
    }

    private static void registerHooks() {
        ClientHooks.ENGINE_SOUND_TICK = CDGClientSounds::engineTick;
        ClientHooks.PUMPJACK_SOUND_TICK = CDGClientSounds::pumpjackHoleTick;
        ClientHooks.DISTILLATION_SOUND_TICK = CDGClientSounds::distillationTick;
        ClientHooks.CRANK_SOUND_TICK = CDGClientSounds::crankTick;
        ClientHooks.TRAIN_ENGINE_TICK = CDGClientSounds::trainEngineTick;
        ClientHooks.PUMPJACK_POURING = (level, pos, fluid) ->
                FluidFX.spawnPouringLiquid(level, pos, 20, FluidFX.getFluidParticle(fluid), 0.3f, new Vec3(0.1, 1, 0.1), true);
        ClientHooks.SPRAY_PARTICLE = (level, fluid, x, y, z, dx, dy, dz) ->
                level.addParticle(FluidFX.getFluidParticle(fluid), x, y, z, dx, dy, dz);
    }
}
