package com.jesz.createdieselgenerators.content.diesel_engine;

import com.jesz.createdieselgenerators.CDGConfig;

import java.util.function.Supplier;

public enum EngineTypes {
    NORMAL(CDGConfig.NORMAL_ENGINES::get), MODULAR(CDGConfig.MODULAR_ENGINES::get), HUGE(CDGConfig.HUGE_ENGINES::get);

    final Supplier<Boolean> isEnabled;

    EngineTypes(Supplier<Boolean> isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean enabled() {
        return isEnabled.get();
    }
}
