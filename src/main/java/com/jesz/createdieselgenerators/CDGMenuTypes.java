package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.entity_filter.EntityFilterMenu;
import com.zurrtum.create.AllMenuTypes;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;

public class CDGMenuTypes {
    static {
        // Create Fly sends menu types by their raw id in its own (unsynced) registry, so Create's types must be
        // registered before ours on both sides, whatever order the loader runs the two mods' entrypoints in.
        AllMenuTypes.register();
    }

    public static final MenuType<ItemStack> ENTITY_FILTER = Registry.register(CreateRegistries.MENU_TYPE,
            CreateDieselGenerators.rl("entity_filter"), EntityFilterMenu::new);

    public static void register() {}
}
