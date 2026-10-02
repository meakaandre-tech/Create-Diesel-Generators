package com.jesz.createdieselgenerators.client.ponder;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.zurrtum.create.client.infrastructure.ponder.AllCreatePonderTags;
import com.zurrtum.create.client.ponder.api.registration.PonderPlugin;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;

public class CDGPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return CreateDieselGenerators.ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<Identifier> rawHelper) {
        PonderSceneRegistrationHelper<ItemLike> helper = rawHelper.withKeyFunction(item -> BuiltInRegistries.ITEM.getKey(item.asItem()));

        helper.forComponents(CDGItems.DISTILLATION_CONTROLLER)
                .addStoryBoard("distillation_tower", DistillationScene::scene);
        helper.forComponents(CDGBlocks.DIESEL_ENGINE)
                .addStoryBoard("diesel_engine", DieselEngineScenes::small);
        helper.forComponents(CDGBlocks.MODULAR_DIESEL_ENGINE)
                .addStoryBoard("large_diesel_engine", DieselEngineScenes::modular);
        helper.forComponents(CDGBlocks.BASIN_LID)
                .addStoryBoard("basin_fermenting_station", BasinScenes::basin_lid);
        helper.forComponents(CDGBlocks.HUGE_DIESEL_ENGINE)
                .addStoryBoard("huge_diesel_engine", DieselEngineScenes::huge);
        helper.forComponents(CDGBlocks.PUMPJACK_BEARING, CDGBlocks.PUMPJACK_CRANK, CDGBlocks.PUMPJACK_HEAD)
                .addStoryBoard("pumpjack", PumpjackScene::scene);
        helper.forComponents(CDGBlocks.PUMPJACK_BEARING, CDGBlocks.PUMPJACK_CRANK, CDGBlocks.PUMPJACK_HEAD, CDGItems.OIL_SCANNER)
                .addStoryBoard("pumpjack", OilChunkScene::scene);
        helper.forComponents(CDGBlocks.BURNER)
                .addStoryBoard("burner", BurnerScenes::scene);
        helper.forComponents(CDGBlocks.CHEMICAL_TURRET)
                .addStoryBoard("chemical_turret", TurretScenes::chemical)
                .addStoryBoard("automatic_turret", TurretScenes::automatic);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<Identifier> rawHelper) {
        PonderTagRegistrationHelper<ItemLike> helper = rawHelper.withKeyFunction(item -> BuiltInRegistries.ITEM.getKey(item.asItem()));

        helper.addToTag(AllCreatePonderTags.KINETIC_SOURCES)
                .add(CDGBlocks.DIESEL_ENGINE)
                .add(CDGBlocks.MODULAR_DIESEL_ENGINE)
                .add(CDGBlocks.HUGE_DIESEL_ENGINE);
        helper.addToTag(AllCreatePonderTags.KINETIC_APPLIANCES)
                .add(CDGBlocks.BASIN_LID)
                .add(CDGBlocks.PUMPJACK_BEARING)
                .add(CDGBlocks.CHEMICAL_TURRET);
        helper.addToTag(AllCreatePonderTags.DISPLAY_SOURCES)
                .add(CDGBlocks.PUMPJACK_HOLE);
        helper.addToTag(AllCreatePonderTags.DECORATION)
                .add(CDGBlocks.ANDESITE_GIRDER)
                .add(CDGBlocks.SHEET_METAL_PANEL);
    }
}
