package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.entity_filter.EntityFilterMenu;
import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;

public class CDGMenuTypes {
    static {
        // Create Fly sends menu types by their raw id in its own (unsynced) registry, so Create's types must be
        // registered before ours on both sides, whatever order the loader runs the two mods' entrypoints in.
        AllMenuTypes.register();
        // That only fixes the order against Create itself. With a second add-on that has menus (Diesel Generators and
        // Create Nuclear together) the loader may run the add-ons in a different order on the server and on the client,
        // the raw ids differ, and the client opens the wrong screen and disconnects with a protocol error.
        // Marking the registry as synced makes Fabric API send the server's ids to the client on join.
        RegistryAttributeHolder.get(CreateRegistries.MENU_TYPE).addAttribute(RegistryAttribute.SYNCED);
    }

    public static final MenuType<ItemStack> ENTITY_FILTER = Registry.register(CreateRegistries.MENU_TYPE,
            CreateDieselGenerators.rl("entity_filter"), EntityFilterMenu::new);

    public static void register() {}
}
