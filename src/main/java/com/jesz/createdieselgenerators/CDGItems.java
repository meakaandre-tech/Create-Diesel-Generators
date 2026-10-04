package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.distillation.DistillationControllerItem;
import com.jesz.createdieselgenerators.content.entity_filter.EntityFilterItem;
import com.jesz.createdieselgenerators.content.items.FurnaceBurnItem;
import com.jesz.createdieselgenerators.content.molds.MoldItem;
import com.jesz.createdieselgenerators.content.tools.ChemicalSprayerItem;
import com.jesz.createdieselgenerators.content.tools.OilScannerItem;
import com.jesz.createdieselgenerators.content.tools.hammer.HammerItem;
import com.jesz.createdieselgenerators.content.tools.lighter.LighterItem;
import com.jesz.createdieselgenerators.content.tools.wire_cutters.WireCuttersItem;
import com.jesz.createdieselgenerators.content.track_layers_bag.TrackLayersBagItem;
import com.jesz.createdieselgenerators.fluid.ItemFluidInventory;
import com.jesz.createdieselgenerators.registry.entry.ItemEntry;
import com.zurrtum.create.AllEnchantments;
import com.zurrtum.create.AllFluidItemInventory;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.function.Function;

public class CDGItems {

    public static final ItemEntry<Item> KELP_HANDLE = item("kelp_handle", Item::new);

    public static final ItemEntry<FurnaceBurnItem> WOOD_CHIPS = item("wood_chip", p -> new FurnaceBurnItem(p, "time_wood_chip"));

    public static final ItemEntry<Item> ENGINE_PISTON = item("engine_piston", Item::new);

    public static final ItemEntry<Item> ENGINE_SILENCER = item("engine_silencer", Item::new);

    public static final ItemEntry<Item> ENGINE_TURBO = item("engine_turbocharger", Item::new);

    public static final ItemEntry<DistillationControllerItem> DISTILLATION_CONTROLLER = item("distillation_controller", DistillationControllerItem::new);

    public static final ItemEntry<LighterItem> LIGHTER = item("lighter", p -> new LighterItem(p.stacksTo(1).enchantable(1)));

    public static final ItemEntry<ChemicalSprayerItem> CHEMICAL_SPRAYER = item("chemical_sprayer", p -> new ChemicalSprayerItem(p.stacksTo(1).enchantable(1), false));

    public static final ItemEntry<ChemicalSprayerItem> CHEMICAL_SPRAYER_LIGHTER = item("chemical_sprayer_lighter", p -> new ChemicalSprayerItem(p.stacksTo(1).enchantable(1), true));

    public static final ItemEntry<OilScannerItem> OIL_SCANNER = item("oil_scanner", OilScannerItem::new);

    public static final ItemEntry<TrackLayersBagItem> TRACK_LAYERS_BAG = item("track_layers_bag", TrackLayersBagItem::new);

    public static final ItemEntry<MoldItem> MOLD = item("mold", MoldItem::new);

    // the hammer's own attribute values (9 damage, -1.5 speed) replaced the registered axe attributes upstream
    public static final ItemEntry<HammerItem> HAMMER = item("hammer", p -> new HammerItem(p.durability(128).attributes(ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 9, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -1.5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build())));

    public static final ItemEntry<WireCuttersItem> WIRE_CUTTERS = item("wire_cutters", WireCuttersItem::new);

    public static final ItemEntry<EntityFilterItem> ENTITY_FILTER = item("entity_filter", EntityFilterItem::new);

    private static <T extends Item> ItemEntry<T> item(String name, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, CreateDieselGenerators.rl(name));
        T item = Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key)));
        return new ItemEntry<>(item);
    }

    public static void register() {
        // fluid-holding items, visible to spouts, item drains and engines
        for (Item item : new Item[]{LIGHTER.get(), CHEMICAL_SPRAYER.get(), CHEMICAL_SPRAYER_LIGHTER.get(), CDGBlocks.CANISTER.asItem()})
            AllFluidItemInventory.ALL.put(item, new AllFluidItemInventory.Entry(ItemFluidInventory::new));

        // replaces the items' supportsEnchantment overrides: Capacity can go on the sprayers and the canister
        EnchantmentEvents.ALLOW_ENCHANTING.register((enchantment, stack, context) -> {
            if (enchantment.is(AllEnchantments.CAPACITY)
                    && (stack.is(CHEMICAL_SPRAYER.get()) || stack.is(CHEMICAL_SPRAYER_LIGHTER.get()) || stack.is(CDGBlocks.CANISTER.asItem())))
                return TriState.TRUE;
            return TriState.DEFAULT;
        });
    }
}
