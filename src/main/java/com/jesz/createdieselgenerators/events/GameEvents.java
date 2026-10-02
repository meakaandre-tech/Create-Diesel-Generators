package com.jesz.createdieselgenerators.events;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CDGRegistries;
import com.jesz.createdieselgenerators.commands.CDGCommands;
import com.jesz.createdieselgenerators.content.entity_filter.ReverseLootTable;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.jesz.createdieselgenerators.mixins.LootItemAccessor;
import com.jesz.createdieselgenerators.mixins.LootPoolAccessor;
import com.jesz.createdieselgenerators.mixins.LootTableAccessor;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Server/common game events, on Fabric API callbacks instead of the NeoForge event bus.
 */
public class GameEvents {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> new CDGCommands(dispatcher));

        LootTableEvents.ALL_LOADED.register((resourceManager, registry) -> {
            ReverseLootTable.ALL.clear();
            registry.listElements().forEach(holder -> {
                Identifier tableId = holder.key().identifier();
                if (!tableId.getPath().startsWith("entities/"))
                    return;
                String path = tableId.getPath().replaceAll("entities/", "");
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath(tableId.getNamespace(), path));
                for (LootPool pool : ((LootTableAccessor) holder.value()).getPools())
                    for (LootPoolEntryContainer c : ((LootPoolAccessor) pool).getEntries())
                        if (c instanceof LootItemAccessor lootItem)
                            ReverseLootTable.ALL.computeIfAbsent(lootItem.getItem().value(), s -> new ArrayList<>()).add(type);
            });
        });

        ServerTickEvents.END_LEVEL_TICK.register(GameEvents::onServerTick);
    }

    private static void onServerTick(ServerLevel level) {
        if (toExplode.containsKey(level)) {
            List<BlockPos> list = toExplode.get(level).stream().toList();
            if (list.isEmpty())
                return;
            for (BlockPos pos : list) {
                level.explode(null, null, null, pos.getX(), pos.getY(), pos.getZ(), 1, true, Level.ExplosionInteraction.BLOCK);
                toExplode.get(level).remove(pos);
            }
        }
    }

    static Map<Level, Set<BlockPos>> toExplode = new HashMap<>();

    /** Called from ServerExplosionMixin when an explosion goes off. */
    public static void onExplosion(ServerLevel level, Vec3 center) {
        if (CDGConfig.COMBUSTIBLES_BLOW_UP.get())
            for (int x = -2; x < 2; x++) {
                for (int y = -2; y < 2; y++) {
                    for (int z = -2; z < 2; z++) {
                        BlockPos pos = new BlockPos((int) (x+center.x), (int) (y+center.y), (int) (z+center.z));

                        if (!level.isInWorldBounds(pos)) continue;
                        if (Math.abs(Math.sqrt(x*x+y*y+z*z)) < 2) {
                            FluidState fluidState = level.getFluidState(pos);
                            boolean flammable = FuelType.getTypeFor(level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fluidState.getType()).normal().speed() != 0;

                            if (flammable) {
                                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                                if (!toExplode.containsKey(level))
                                    toExplode.put(level, new HashSet<>());
                                toExplode.get(level).add(pos);
                                return;
                            }
                        }
                    }
                }
            }
    }
}
