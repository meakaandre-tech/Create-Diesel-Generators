package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.entity_filter.EntityFilterMenu;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;

public class CDGMenuTypes {
    public static final MenuType<ItemStack> ENTITY_FILTER = Registry.register(CreateRegistries.MENU_TYPE,
            CreateDieselGenerators.rl("entity_filter"), EntityFilterMenu::new);

    public static void register() {}
}
