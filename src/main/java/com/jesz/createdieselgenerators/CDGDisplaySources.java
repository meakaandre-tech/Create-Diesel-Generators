package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.pumpjack.PumpjackOilAmountDisplaySource;
import com.zurrtum.create.api.registry.CreateRegistries;
import net.minecraft.core.Registry;

public class CDGDisplaySources {
    public static final PumpjackOilAmountDisplaySource PUMPJACK_OIL_AMOUNT = Registry.register(
            CreateRegistries.DISPLAY_SOURCE, CreateDieselGenerators.rl("pumpjack_oil_amount"), new PumpjackOilAmountDisplaySource());

    public static void register() {
    }
}
