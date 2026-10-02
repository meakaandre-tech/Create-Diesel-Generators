package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterUnpackingHandler;
import com.jesz.createdieselgenerators.content.canister.SpoutCanisterFilling;
import com.jesz.createdieselgenerators.content.molds.BasinSpoutCasting;
import com.jesz.createdieselgenerators.content.molds.MoldType;
import com.jesz.createdieselgenerators.content.turret.TurretData;
import com.jesz.createdieselgenerators.events.GameEvents;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.jesz.createdieselgenerators.packets.CDGPackets;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CreateDieselGenerators implements ModInitializer {
    public static final String ID = "createdieselgenerators";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    @Override
    public void onInitialize() {
        CDGConfig.loadCommon();

        CDGDataComponents.register();
        CDGMountedStorageTypes.register();
        CDGDisplaySources.register();
        CDGBlocks.register();
        CDGItems.register();
        CDGFluids.register();
        CDGBlockEntityTypes.register();
        CDGEntityTypes.register();
        CDGSoundEvents.register();
        CDGRecipes.register();
        CDGMenuTypes.register();
        MoldType.register();
        CDGCreativeTab.register();
        CDGPackets.register();
        TurretData.init();

        // data pack registry of fuel types, synced to clients with the tag-free codec
        DynamicRegistries.registerSynced(CDGRegistries.FUEL_TYPE, FuelType.CODEC, FuelType.NCODEC);

        BlockSpoutingBehaviour.BY_BLOCK_ENTITY.register(CDGBlockEntityTypes.CANISTER.get(), new SpoutCanisterFilling());
        BlockSpoutingBehaviour.BY_BLOCK_ENTITY.register(AllBlockEntityTypes.BASIN, new BasinSpoutCasting());
        BulkFermenterUnpackingHandler.register();

        GameEvents.register();
    }

    public static Identifier rl(String path){
        return Identifier.fromNamespaceAndPath(ID, path);
    }

    public static Component lang(String path, Object... args) {
        return Component.translatable(ID+"."+path, args);
    }
}
